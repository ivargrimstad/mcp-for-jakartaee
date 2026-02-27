package dukes.mcp.service;

import dukes.mcp.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ApplicationScoped CDI bean that handles MCP protocol requests and manages protocol state.
 * 
 * <p>The MCPProtocolHandler is the core component that processes MCP protocol requests,
 * manages the protocol state machine (uninitialized/initialized), and coordinates between
 * ToolRegistry, ResourceManager, and PromptManager.</p>
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li>Protocol state management (uninitialized/initialized)</li>
 *   <li>JSON-RPC 2.0 request validation and routing</li>
 *   <li>Method routing to appropriate handlers</li>
 *   <li>Comprehensive error handling with proper JSON-RPC error codes</li>
 *   <li>Coordination between service components</li>
 * </ul>
 * 
 * <p>Supported MCP methods:</p>
 * <ul>
 *   <li>initialize - Initialize the server and negotiate capabilities</li>
 *   <li>tools/list - List all available tools</li>
 *   <li>tools/call - Execute a tool with arguments</li>
 *   <li>resources/list - List all available resources</li>
 *   <li>resources/read - Read a resource by URI</li>
 *   <li>prompts/list - List all available prompts</li>
 *   <li>prompts/get - Get a prompt with arguments</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * @Inject
 * private MCPProtocolHandler protocolHandler;
 * 
 * public JsonRpcResponse handleRequest(JsonRpcRequest request) {
 *     return protocolHandler.processRequest(request);
 * }
 * }</pre>
 * 
 * @see ToolRegistry
 * @see ResourceManager
 * @see PromptManager
 */
@ApplicationScoped
public class MCPProtocolHandler {
    
    private static final Logger LOGGER = Logger.getLogger(MCPProtocolHandler.class.getName());
    
    /**
     * MCP protocol version supported by this server.
     */
    private static final String PROTOCOL_VERSION = "2024-11-05";
    
    /**
     * Server name and version information.
     */
    private static final String SERVER_NAME = "MCP Server for Jakarta EE";
    private static final String SERVER_VERSION = "1.0.0";
    
    /**
     * JSON-RPC error codes.
     */
    private static final int ERROR_PARSE_ERROR = -32700;
    private static final int ERROR_INVALID_REQUEST = -32600;
    private static final int ERROR_METHOD_NOT_FOUND = -32601;
    private static final int ERROR_INVALID_PARAMS = -32602;
    private static final int ERROR_INTERNAL_ERROR = -32603;
    private static final int ERROR_SERVER_NOT_INITIALIZED = -32002;
    
    /**
     * Protocol state flag indicating whether the server has been initialized.
     */
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    
    @Inject
    private ToolRegistry toolRegistry;
    
    @Inject
    private ResourceManager resourceManager;
    
    @Inject
    private PromptManager promptManager;
    
    /**
     * Processes a JSON-RPC request and returns a JSON-RPC response.
     * 
     * <p>This method performs the following steps:</p>
     * <ol>
     *   <li>Validates the JSON-RPC request structure</li>
     *   <li>Routes the request to the appropriate handler based on method</li>
     *   <li>Validates protocol state (initialized/uninitialized)</li>
     *   <li>Executes the handler and returns the result</li>
     *   <li>Catches all exceptions and converts them to JSON-RPC error responses</li>
     * </ol>
     * 
     * <p>All errors are caught and returned as valid JSON-RPC error responses,
     * ensuring that exceptions never propagate to the HTTP layer.</p>
     * 
     * @param request the JSON-RPC request to process
     * @return a JSON-RPC response containing either a result or an error
     */
    public JsonRpcResponse processRequest(JsonRpcRequest request) {
        try {
            // Step 1: Validate JSON-RPC structure
            validateJsonRpcRequest(request);
            
            String method = request.getMethod();
            Object id = request.getId();
            
            LOGGER.log(Level.FINE, "Processing request: method={0}, id={1}", new Object[]{method, id});
            
            // Step 2: Check if server needs to be initialized (except for initialize method)
            if (!initialized.get() && !"initialize".equals(method)) {
                LOGGER.log(Level.WARNING, "Server not initialized, rejecting method: {0}", method);
                return JsonRpcResponse.error(id, ERROR_SERVER_NOT_INITIALIZED, 
                        "Server not initialized. Call 'initialize' method first.");
            }
            
            // Step 3: Route to appropriate handler based on method
            Object result = switch (method) {
                case "initialize" -> handleInitialize(parseParams(request.getParams(), InitializeParams.class));
                case "tools/list" -> handleToolsList();
                case "tools/call" -> {
                    ToolCallParams params = parseParams(request.getParams(), ToolCallParams.class);
                    yield handleToolCall(params.getName(), params.getArguments());
                }
                case "resources/list" -> handleResourcesList();
                case "resources/read" -> {
                    ResourceReadParams params = parseParams(request.getParams(), ResourceReadParams.class);
                    yield handleResourceRead(params.getUri());
                }
                case "prompts/list" -> handlePromptsList();
                case "prompts/get" -> {
                    PromptGetParams params = parseParams(request.getParams(), PromptGetParams.class);
                    yield handlePromptGet(params.getName(), params.getArguments());
                }
                default -> throw new MethodNotFoundException("Unknown method: " + method);
            };
            
            // Step 4: Build success response
            LOGGER.log(Level.FINE, "Request processed successfully: method={0}", method);
            return JsonRpcResponse.success(id, result);
            
        } catch (JsonRpcException e) {
            // Step 5: Handle protocol errors
            LOGGER.log(Level.WARNING, "JSON-RPC error: " + e.getMessage(), e);
            return JsonRpcResponse.error(request.getId(), e.getCode(), e.getMessage(), e.getData());
        } catch (MethodNotFoundException e) {
            // Step 6: Handle method not found
            LOGGER.log(Level.WARNING, "Method not found: " + e.getMessage(), e);
            return JsonRpcResponse.error(request.getId(), ERROR_METHOD_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            // Step 7: Handle unexpected errors
            LOGGER.log(Level.SEVERE, "Internal error processing request", e);
            return JsonRpcResponse.error(request.getId(), ERROR_INTERNAL_ERROR, 
                    "Internal error", e.getMessage());
        }
    }
    
    /**
     * Handles the initialize method.
     * 
     * <p>Negotiates protocol version and capabilities with the client, and transitions
     * the server to initialized state. Returns server capabilities and information.</p>
     * 
     * @param params the initialize parameters from the client
     * @return the initialize result with server capabilities
     */
    public InitializeResult handleInitialize(InitializeParams params) {
        LOGGER.log(Level.INFO, "Initializing server with protocol version: {0}", 
                params != null ? params.getProtocolVersion() : "null");
        
        // Transition to initialized state
        initialized.set(true);
        
        // Build server capabilities
        ServerCapabilities capabilities = ServerCapabilities.allEnabled();
        
        // Build server info
        ServerInfo serverInfo = new ServerInfo(SERVER_NAME, SERVER_VERSION);
        
        // Return initialize result
        InitializeResult result = new InitializeResult(PROTOCOL_VERSION, capabilities, serverInfo);
        
        LOGGER.log(Level.INFO, "Server initialized successfully");
        return result;
    }
    
    /**
     * Handles the tools/list method.
     * 
     * <p>Delegates to ToolRegistry to retrieve all registered tools.</p>
     * 
     * @return the tool list result containing all available tools
     */
    public ToolListResult handleToolsList() {
        LOGGER.log(Level.FINE, "Listing tools");
        return new ToolListResult(toolRegistry.listTools());
    }
    
    /**
     * Handles the tools/call method.
     * 
     * <p>Delegates to ToolRegistry to execute the specified tool with the provided arguments.
     * All tool execution errors are caught and returned as ToolResult with isError flag.</p>
     * 
     * @param toolName the name of the tool to execute
     * @param arguments the arguments to pass to the tool
     * @return the tool result containing execution outcome
     */
    public ToolResult handleToolCall(String toolName, Map<String, Object> arguments) {
        LOGGER.log(Level.FINE, "Calling tool: {0}", toolName);
        
        // Ensure arguments is not null
        Map<String, Object> safeArguments = (arguments != null) ? arguments : Map.of();
        
        // Delegate to ToolRegistry
        return toolRegistry.executeTool(toolName, safeArguments);
    }
    
    /**
     * Handles the resources/list method.
     * 
     * <p>Delegates to ResourceManager to retrieve all registered resources.</p>
     * 
     * @return the resource list result containing all available resources
     */
    public ResourceListResult handleResourcesList() {
        LOGGER.log(Level.FINE, "Listing resources");
        return new ResourceListResult(resourceManager.listResources());
    }
    
    /**
     * Handles the resources/read method.
     * 
     * <p>Delegates to ResourceManager to read the specified resource.
     * Catches ResourceNotFoundException and ResourceReadException and converts them
     * to JSON-RPC errors.</p>
     * 
     * @param uri the URI of the resource to read
     * @return the resource read result containing the resource content
     * @throws JsonRpcException if the resource is not found or cannot be read
     */
    public ResourceReadResult handleResourceRead(String uri) {
        LOGGER.log(Level.FINE, "Reading resource: {0}", uri);
        
        try {
            // Delegate to ResourceManager
            ResourceContent content = resourceManager.readResource(uri);
            return new ResourceReadResult(List.of(content));
            
        } catch (ResourceManager.ResourceNotFoundException e) {
            // Resource not found - return JSON-RPC error
            throw new JsonRpcException(ERROR_SERVER_NOT_INITIALIZED, 
                    "Resource not found: " + uri, null);
        } catch (ResourceManager.ResourceReadException e) {
            // Resource read error - return JSON-RPC error
            throw new JsonRpcException(ERROR_INTERNAL_ERROR, 
                    "Failed to read resource: " + uri, e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        }
    }
    
    /**
     * Handles the prompts/list method.
     * 
     * <p>Delegates to PromptManager to retrieve all registered prompts.</p>
     * 
     * @return the prompt list result containing all available prompts
     */
    public PromptListResult handlePromptsList() {
        LOGGER.log(Level.FINE, "Listing prompts");
        return new PromptListResult(promptManager.listPrompts());
    }
    
    /**
     * Handles the prompts/get method.
     * 
     * <p>Delegates to PromptManager to retrieve and render the specified prompt.
     * Catches PromptNotFoundException and PromptArgumentException and converts them
     * to JSON-RPC errors.</p>
     * 
     * @param promptName the name of the prompt to retrieve
     * @param arguments the arguments to pass to the prompt
     * @return the prompt get result containing the rendered prompt
     * @throws JsonRpcException if the prompt is not found or arguments are invalid
     */
    public PromptGetResult handlePromptGet(String promptName, Map<String, String> arguments) {
        LOGGER.log(Level.FINE, "Getting prompt: {0}", promptName);
        
        try {
            // Ensure arguments is not null
            Map<String, String> safeArguments = (arguments != null) ? arguments : Map.of();
            
            // Delegate to PromptManager
            return promptManager.getPromptContent(promptName, safeArguments);
            
        } catch (PromptManager.PromptNotFoundException e) {
            // Prompt not found - return JSON-RPC error
            throw new JsonRpcException(ERROR_SERVER_NOT_INITIALIZED, 
                    "Prompt not found: " + promptName, null);
        } catch (PromptManager.PromptArgumentException e) {
            // Invalid arguments - return JSON-RPC error
            throw new JsonRpcException(ERROR_INVALID_PARAMS, 
                    "Invalid prompt arguments: " + e.getMessage(), null);
        }
    }
    
    /**
     * Validates that a JSON-RPC request conforms to the specification.
     * 
     * <p>Checks that:</p>
     * <ul>
     *   <li>Request is not null</li>
     *   <li>jsonrpc field equals "2.0"</li>
     *   <li>method field is present and non-empty</li>
     * </ul>
     * 
     * @param request the request to validate
     * @throws JsonRpcException if validation fails
     */
    private void validateJsonRpcRequest(JsonRpcRequest request) throws JsonRpcException {
        if (request == null) {
            throw new JsonRpcException(ERROR_INVALID_REQUEST, "Request cannot be null", null);
        }
        
        try {
            request.validate();
        } catch (IllegalArgumentException e) {
            throw new JsonRpcException(ERROR_INVALID_REQUEST, e.getMessage(), null);
        }
    }
    
    /**
     * Parses request parameters to the specified target class.
     * 
     * <p>Uses JSON-B to convert the params object to the target class.
     * Returns null if params is null and the method doesn't require parameters.</p>
     * 
     * @param params the parameters object from the request
     * @param targetClass the target class to parse to
     * @param <T> the type of the target class
     * @return the parsed parameters object
     * @throws JsonRpcException if parsing fails or required parameters are missing
     */
    private <T> T parseParams(Object params, Class<T> targetClass) throws JsonRpcException {
        if (params == null) {
            // Return null for methods that don't require parameters
            return null;
        }
        
        try (Jsonb jsonb = JsonbBuilder.create()) {
            // Convert params to JSON string and then to target class
            String paramsJson = jsonb.toJson(params);
            return jsonb.fromJson(paramsJson, targetClass);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to parse parameters", e);
            throw new JsonRpcException(ERROR_INVALID_PARAMS, 
                    "Invalid parameters: " + e.getMessage(), null);
        }
    }
    
    /**
     * Exception thrown when a JSON-RPC error occurs.
     */
    private static class JsonRpcException extends RuntimeException {
        private final int code;
        private final Object data;
        
        public JsonRpcException(int code, String message, Object data) {
            super(message);
            this.code = code;
            this.data = data;
        }
        
        public int getCode() {
            return code;
        }
        
        public Object getData() {
            return data;
        }
    }
    
    /**
     * Exception thrown when a method is not found.
     */
    private static class MethodNotFoundException extends RuntimeException {
        public MethodNotFoundException(String message) {
            super(message);
        }
    }
}
