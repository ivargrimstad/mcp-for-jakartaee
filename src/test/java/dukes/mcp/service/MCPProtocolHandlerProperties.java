package dukes.mcp.service;

import dukes.mcp.model.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.NotBlank;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for MCPProtocolHandler.
 * 
 * These tests validate:
 * - Property 3: Invalid Request Rejection
 * - Property 4: Unknown Method Rejection
 * - Property 5: Pre-Initialization Method Rejection
 * - Property 28: Exception Handling Completeness
 * - Property 34: Post-Initialization Method Acceptance
 * 
 * Validates Requirements: 2.1, 2.2, 2.5, 2.6, 3.3, 20.3, 13.1, 13.2, 20.4
 * 
 * Note: These tests use real service instances instead of mocks to avoid
 * Mockito compatibility issues with Java 25.
 */
class MCPProtocolHandlerProperties {
    
    /**
     * Property 3: Invalid Request Rejection
     * 
     * For any JSON-RPC request with invalid structure (missing jsonrpc field,
     * invalid jsonrpc version, missing method field), the server must return
     * error code -32600.
     * 
     * Validates: Requirements 2.1, 2.2, 2.5
     */
    @Property
    void invalidRequestStructureReturnsError32600(
            @ForAll("invalidJsonRpcRequests") JsonRpcRequest invalidRequest) {
        
        // Given: A protocol handler with mocked dependencies
        MCPProtocolHandler handler = createHandlerWithMocks();
        
        // When: Processing an invalid request
        JsonRpcResponse response = handler.processRequest(invalidRequest);
        
        // Then: Response must be an error with code -32600
        assertNotNull(response, "Response must not be null");
        assertTrue(response.isError(), "Response must be an error");
        assertEquals(-32600, response.getError().getCode(),
                "Invalid request must return error code -32600");
        assertNotNull(response.getError().getMessage(),
                "Error must have a message");
    }
    
    /**
     * Property 4: Unknown Method Rejection
     * 
     * For any JSON-RPC request with a method name not in the supported set,
     * the server must return error code -32601.
     * 
     * Validates: Requirement 2.6
     */
    @Property
    void unknownMethodReturnsError32601(
            @ForAll("unknownMethods") String unknownMethod,
            @ForAll("requestIds") Object requestId) {
        
        // Given: A protocol handler with mocked dependencies
        MCPProtocolHandler handler = createHandlerWithMocks();
        
        // Initialize the server first
        handler.processRequest(new JsonRpcRequest("initialize", 
                new InitializeParams("2024-11-05", new ClientCapabilities()), "init"));
        
        // Create request with unknown method
        JsonRpcRequest request = new JsonRpcRequest(unknownMethod, null, requestId);
        
        // When: Processing request with unknown method
        JsonRpcResponse response = handler.processRequest(request);
        
        // Then: Response must be an error with code -32601
        assertNotNull(response, "Response must not be null");
        assertTrue(response.isError(), "Response must be an error");
        assertEquals(-32601, response.getError().getCode(),
                "Unknown method must return error code -32601");
        assertTrue(response.getError().getMessage().contains("Unknown method") ||
                   response.getError().getMessage().contains("not found"),
                "Error message must indicate method not found");
        assertEquals(requestId, response.getId(),
                "Response ID must match request ID");
    }
    
    /**
     * Property 5: Pre-Initialization Method Rejection
     * 
     * For any method call other than "initialize" when the server is in
     * uninitialized state, the server must return error code -32002.
     * 
     * Validates: Requirements 3.3, 20.3
     */
    @Property
    void preInitializationMethodCallReturnsError32002(
            @ForAll("nonInitializeMethods") String method,
            @ForAll("requestIds") Object requestId) {
        
        // Given: A fresh protocol handler (uninitialized)
        MCPProtocolHandler handler = createHandlerWithMocks();
        
        // Create request with non-initialize method
        JsonRpcRequest request = new JsonRpcRequest(method, null, requestId);
        
        // When: Processing request before initialization
        JsonRpcResponse response = handler.processRequest(request);
        
        // Then: Response must be an error with code -32002
        assertNotNull(response, "Response must not be null");
        assertTrue(response.isError(), "Response must be an error");
        assertEquals(-32002, response.getError().getCode(),
                "Pre-initialization method call must return error code -32002");
        assertTrue(response.getError().getMessage().contains("not initialized") ||
                   response.getError().getMessage().contains("initialize"),
                "Error message must indicate server not initialized");
        assertEquals(requestId, response.getId(),
                "Response ID must match request ID");
    }
    
    /**
     * Property 28: Exception Handling Completeness
     * 
     * For any request that causes an exception during processing, the server
     * must catch it and return a JSON-RPC response with an error object,
     * never allowing exceptions to propagate to the HTTP layer.
     * 
     * Validates: Requirements 13.1, 13.2
     */
    @Property
    void allExceptionsAreCaughtAndReturnedAsErrors(
            @ForAll("executionMethods") String method,
            @ForAll("requestIds") Object requestId) {
        
        // Given: A protocol handler with mocked dependencies that throw exceptions
        MCPProtocolHandler handler = createHandlerWithThrowingMocks();
        
        // Initialize the server first
        handler.processRequest(new JsonRpcRequest("initialize", 
                new InitializeParams("2024-11-05", new ClientCapabilities()), "init"));
        
        // Create request
        JsonRpcRequest request = new JsonRpcRequest(method, createParamsForMethod(method), requestId);
        
        // When: Processing request that causes exception
        JsonRpcResponse response = null;
        Exception caughtException = null;
        try {
            response = handler.processRequest(request);
        } catch (Exception e) {
            caughtException = e;
        }
        
        // Then: No exception should propagate
        assertNull(caughtException, 
                "No exception should propagate from processRequest");
        assertNotNull(response, "Response must not be null even when exception occurs");
        
        // Then: Response must indicate an error (either JSON-RPC error or error in result)
        // For tools/call: successful response with ToolResult.isError=true
        // For resources/read and prompts/get: JSON-RPC error response
        boolean hasError = response.isError() || 
                          (method.equals("tools/call") && response.getResult() instanceof ToolResult && 
                           Boolean.TRUE.equals(((ToolResult)response.getResult()).getIsError()));
        assertTrue(hasError, "Response must indicate an error when exception occurs");
        assertEquals(requestId, response.getId(),
                "Response ID must match request ID even on error");
        
        // Then: Response must be valid JSON-RPC
        final JsonRpcResponse finalResponse = response;
        assertDoesNotThrow(() -> finalResponse.validate(),
                "Error response must be valid JSON-RPC");
    }
    
    /**
     * Property 34: Post-Initialization Method Acceptance
     * 
     * For any valid method call after the server has been initialized,
     * the server must accept and process the request.
     * 
     * Validates: Requirement 20.4
     */
    @Property
    void postInitializationMethodsAreAccepted(
            @ForAll("validMethods") String method,
            @ForAll("requestIds") Object requestId) {
        
        // Given: A protocol handler with mocked dependencies
        MCPProtocolHandler handler = createHandlerWithMocks();
        
        // Initialize the server first
        JsonRpcResponse initResponse = handler.processRequest(new JsonRpcRequest("initialize", 
                new InitializeParams("2024-11-05", new ClientCapabilities()), "init"));
        assertTrue(initResponse.isSuccess(), "Initialization must succeed");
        
        // Create request with valid method
        JsonRpcRequest request = new JsonRpcRequest(method, createParamsForMethod(method), requestId);
        
        // When: Processing request after initialization
        JsonRpcResponse response = handler.processRequest(request);
        
        // Then: Response must not be a "not initialized" error
        assertNotNull(response, "Response must not be null");
        if (response.isError()) {
            assertNotEquals(-32002, response.getError().getCode(),
                    "Post-initialization request must not return 'not initialized' error");
        }
        
        // Then: Response must have matching ID
        assertEquals(requestId, response.getId(),
                "Response ID must match request ID");
        
        // Then: Response must be valid JSON-RPC
        assertDoesNotThrow(() -> response.validate(),
                "Response must be valid JSON-RPC");
    }
    
    /**
     * Property: Initialize method can be called multiple times
     * 
     * Validates: Requirement 3.2
     */
    @Property
    void initializeCanBeCalledMultipleTimes(@ForAll("requestIds") Object requestId) {
        
        // Given: A protocol handler
        MCPProtocolHandler handler = createHandlerWithMocks();
        
        // When: Calling initialize multiple times
        JsonRpcResponse response1 = handler.processRequest(new JsonRpcRequest("initialize", 
                new InitializeParams("2024-11-05", new ClientCapabilities()), requestId));
        JsonRpcResponse response2 = handler.processRequest(new JsonRpcRequest("initialize", 
                new InitializeParams("2024-11-05", new ClientCapabilities()), requestId));
        
        // Then: Both calls should succeed
        assertTrue(response1.isSuccess(), "First initialize must succeed");
        assertTrue(response2.isSuccess(), "Second initialize must succeed");
        assertEquals(requestId, response1.getId());
        assertEquals(requestId, response2.getId());
    }
    
    // ========== Arbitraries (Data Generators) ==========
    
    /**
     * Generates invalid JSON-RPC requests.
     */
    @Provide
    Arbitrary<JsonRpcRequest> invalidJsonRpcRequests() {
        return Arbitraries.oneOf(
                // Invalid jsonrpc version
                Combinators.combine(
                        Arbitraries.of("1.0", "2.1", "3.0", "", null),
                        Arbitraries.strings().alpha().ofMinLength(1),
                        requestIds()
                ).as((version, method, id) -> {
                    JsonRpcRequest req = new JsonRpcRequest(method, null, id);
                    req.setJsonrpc(version);
                    return req;
                }),
                
                // Missing or empty method
                Combinators.combine(
                        Arbitraries.of(null, "", "   ", "\t"),
                        requestIds()
                ).as((method, id) -> {
                    JsonRpcRequest req = new JsonRpcRequest();
                    req.setJsonrpc("2.0");
                    req.setMethod(method);
                    req.setId(id);
                    return req;
                })
        );
    }
    
    /**
     * Generates unknown method names (not in the MCP specification).
     */
    @Provide
    Arbitrary<String> unknownMethods() {
        return Arbitraries.of(
                "unknown/method",
                "invalid/method",
                "test/unknown",
                "foo/bar",
                "custom/unknown",
                "notamethod",
                "tools/unknown",
                "resources/unknown",
                "prompts/unknown"
        );
    }
    
    /**
     * Generates valid MCP method names (excluding initialize).
     */
    @Provide
    Arbitrary<String> nonInitializeMethods() {
        return Arbitraries.of(
                "tools/list",
                "tools/call",
                "resources/list",
                "resources/read",
                "prompts/list",
                "prompts/get"
        );
    }
    
    /**
     * Generates valid MCP method names (including initialize).
     */
    @Provide
    Arbitrary<String> validMethods() {
        return Arbitraries.of(
                "tools/list",
                "tools/call",
                "resources/list",
                "resources/read",
                "prompts/list",
                "prompts/get"
        );
    }
    
    /**
     * Generates MCP method names that actually execute/read/render
     * (methods that can throw exceptions during execution).
     */
    @Provide
    Arbitrary<String> executionMethods() {
        return Arbitraries.of(
                "tools/call",
                "resources/read",
                "prompts/get"
        );
    }
    
    /**
     * Generates valid request IDs (string, number).
     */
    @Provide
    Arbitrary<Object> requestIds() {
        return Arbitraries.oneOf(
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50),
                Arbitraries.integers(),
                Arbitraries.longs()
        );
    }
    
    // ========== Helper Methods ==========
    
    /**
     * Creates a protocol handler with real service instances.
     */
    private MCPProtocolHandler createHandlerWithMocks() {
        ToolRegistry toolRegistry = new ToolRegistry();
        ResourceManager resourceManager = new ResourceManager();
        PromptManager promptManager = new PromptManager();
        
        // Register a test tool
        toolRegistry.registerTool(new Tool() {
            @Override
            public String getName() { return "test_tool"; }
            @Override
            public String getDescription() { return "Test tool"; }
            @Override
            public Map<String, Object> getInputSchema() { return Map.of(); }
            @Override
            public ToolResult execute(Map<String, Object> arguments) {
                return ToolResult.success("result");
            }
        });
        
        // Register a test resource
        resourceManager.registerResource(new Resource() {
            @Override
            public String getUri() { return "test://resource"; }
            @Override
            public String getName() { return "Test Resource"; }
            @Override
            public String getDescription() { return "Test"; }
            @Override
            public String getMimeType() { return "text/plain"; }
            @Override
            public String read() { return "content"; }
        });
        
        // Register a test prompt
        promptManager.registerPrompt(new Prompt() {
            @Override
            public String getName() { return "test_prompt"; }
            @Override
            public String getDescription() { return "Test Prompt"; }
            @Override
            public List<PromptArgument> getArguments() { return List.of(); }
            @Override
            public List<PromptMessage> render(Map<String, String> arguments) {
                return List.of(
                    new PromptMessage("user", new ContentItem("text", "prompt"))
                );
            }
        });
        
        MCPProtocolHandler handler = new MCPProtocolHandler();
        injectField(handler, "toolRegistry", toolRegistry);
        injectField(handler, "resourceManager", resourceManager);
        injectField(handler, "promptManager", promptManager);
        return handler;
    }
    
    /**
     * Creates a protocol handler with services that throw exceptions.
     */
    private MCPProtocolHandler createHandlerWithThrowingMocks() {
        ToolRegistry toolRegistry = new ToolRegistry();
        ResourceManager resourceManager = new ResourceManager();
        PromptManager promptManager = new PromptManager();
        
        // Register a tool that throws exceptions
        toolRegistry.registerTool(new Tool() {
            @Override
            public String getName() { return "test_tool"; }
            @Override
            public String getDescription() { return "Test tool"; }
            @Override
            public Map<String, Object> getInputSchema() { return Map.of(); }
            @Override
            public ToolResult execute(Map<String, Object> arguments) {
                throw new RuntimeException("Tool execution error");
            }
        });
        
        // Register a resource that throws exceptions
        resourceManager.registerResource(new Resource() {
            @Override
            public String getUri() { return "test://resource"; }
            @Override
            public String getName() { return "Test Resource"; }
            @Override
            public String getDescription() { return "Test"; }
            @Override
            public String getMimeType() { return "text/plain"; }
            @Override
            public String read() { throw new RuntimeException("Resource read error"); }
        });
        
        // Register a prompt that throws exceptions
        promptManager.registerPrompt(new Prompt() {
            @Override
            public String getName() { return "test_prompt"; }
            @Override
            public String getDescription() { return "Test Prompt"; }
            @Override
            public List<PromptArgument> getArguments() { return List.of(); }
            @Override
            public List<PromptMessage> render(Map<String, String> arguments) {
                throw new RuntimeException("Prompt get error");
            }
        });
        
        MCPProtocolHandler handler = new MCPProtocolHandler();
        injectField(handler, "toolRegistry", toolRegistry);
        injectField(handler, "resourceManager", resourceManager);
        injectField(handler, "promptManager", promptManager);
        return handler;
    }
    
    /**
     * Creates appropriate params object for a given method.
     */
    private Object createParamsForMethod(String method) {
        return switch (method) {
            case "tools/call" -> new ToolCallParams("test_tool", Map.of());
            case "resources/read" -> new ResourceReadParams("test://resource");
            case "prompts/get" -> new PromptGetParams("test_prompt", Map.of());
            default -> null;
        };
    }
    
    /**
     * Injects a field value using reflection.
     */
    private void injectField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            fail("Failed to inject field " + fieldName + ": " + e.getMessage());
        }
    }
}
