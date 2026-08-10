package dukes.mcp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import dukes.mcp.model.ToolDefinition;
import dukes.mcp.model.ToolResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * ApplicationScoped CDI bean that manages the registration, discovery, and execution of MCP tools.
 * 
 * <p>The ToolRegistry provides a centralized registry for tools that can be discovered and invoked
 * through the MCP protocol. It supports dynamic tool registration/unregistration, thread-safe
 * concurrent access, and JSON Schema validation of tool arguments.</p>
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li>Thread-safe tool storage using ConcurrentHashMap</li>
 *   <li>Dynamic tool registration and unregistration</li>
 *   <li>JSON Schema validation of tool arguments before execution</li>
 *   <li>Graceful error handling for tool execution failures</li>
 *   <li>Tool discovery through listTools()</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * @Inject
 * private ToolRegistry toolRegistry;
 * 
 * public void init() {
 *     toolRegistry.registerTool(new SystemInfoTool());
 *     List<ToolDefinition> tools = toolRegistry.listTools();
 *     ToolResult result = toolRegistry.executeTool("system_info", Map.of());
 * }
 * }</pre>
 * 
 * @see Tool
 * @see ToolDefinition
 * @see ToolResult
 */
@ApplicationScoped
public class ToolRegistry {
    
    private static final Logger LOGGER = Logger.getLogger(ToolRegistry.class.getName());

    /**
     * Shared, thread-safe JSON-B instance used for schema/argument serialisation.
     */
    private static final Jsonb JSONB = JsonbBuilder.create();

    /**
     * Shared, thread-safe Jackson ObjectMapper used only for JSON Schema validation
     * (networknt json-schema-validator requires a Jackson JsonNode).
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Thread-safe map storing registered tools by name.
     */
    private final ConcurrentHashMap<String, Tool> tools = new ConcurrentHashMap<>();
    
    /**
     * JSON Schema factory for creating schema validators.
     */
    private final JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
    
    /**
     * Registers a tool in the registry.
     * 
     * <p>If a tool with the same name already exists, it will be replaced with the new tool.
     * This allows for dynamic tool updates at runtime.</p>
     * 
     * @param tool the tool to register, must not be null
     * @throws NullPointerException if tool is null
     * @throws IllegalArgumentException if tool name is null or empty
     */
    public void registerTool(Tool tool) {
        if (tool == null) {
            throw new NullPointerException("Tool cannot be null");
        }
        
        String toolName = tool.getName();
        if (toolName == null || toolName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tool name cannot be null or empty");
        }
        
        Tool previousTool = tools.put(toolName, tool);
        if (previousTool != null) {
            LOGGER.log(Level.INFO, "Replaced existing tool: {0}", toolName);
        } else {
            LOGGER.log(Level.INFO, "Registered new tool: {0}", toolName);
        }
    }
    
    /**
     * Unregisters a tool from the registry by name.
     * 
     * <p>If the tool does not exist, this method does nothing.</p>
     * 
     * @param toolName the name of the tool to unregister, must not be null
     * @throws NullPointerException if toolName is null
     */
    public void unregisterTool(String toolName) {
        if (toolName == null) {
            throw new NullPointerException("Tool name cannot be null");
        }
        
        Tool removedTool = tools.remove(toolName);
        if (removedTool != null) {
            LOGGER.log(Level.INFO, "Unregistered tool: {0}", toolName);
        }
    }
    
    /**
     * Retrieves a tool by name.
     * 
     * @param toolName the name of the tool to retrieve
     * @return an Optional containing the tool if found, or empty if not found
     */
    public Optional<Tool> getTool(String toolName) {
        return Optional.ofNullable(tools.get(toolName));
    }
    
    /**
     * Lists all registered tools.
     * 
     * <p>Returns a list of ToolDefinition objects containing the name, description,
     * and input schema for each registered tool. The list is a snapshot of the current
     * registry state and is safe to iterate even if tools are registered/unregistered
     * concurrently.</p>
     * 
     * @return a list of all registered tool definitions, never null
     */
    public List<ToolDefinition> listTools() {
        return tools.values().stream()
                .map(tool -> new ToolDefinition(
                        tool.getName(),
                        tool.getDescription(),
                        tool.getInputSchema()
                ))
                .collect(Collectors.toList());
    }
    
    /**
     * Executes a tool with the provided arguments.
     * 
     * <p>This method performs the following steps:</p>
     * <ol>
     *   <li>Looks up the tool by name</li>
     *   <li>Validates arguments against the tool's input schema</li>
     *   <li>Executes the tool if validation passes</li>
     *   <li>Returns the tool result or error information</li>
     * </ol>
     * 
     * <p>All errors are caught and returned as ToolResult with isError flag set to true.
     * This ensures consistent error handling across the MCP protocol.</p>
     * 
     * @param toolName the name of the tool to execute
     * @param arguments the tool arguments to validate and pass to the tool
     * @return a ToolResult containing either the execution result or error information
     */
    public ToolResult executeTool(String toolName, Map<String, Object> arguments) {
        // Step 1: Lookup tool in registry
        Optional<Tool> toolOpt = getTool(toolName);
        if (toolOpt.isEmpty()) {
            LOGGER.log(Level.WARNING, "Tool not found: {0}", toolName);
            return ToolResult.error("Tool not found: " + toolName);
        }
        Tool tool = toolOpt.get();
        
        // Step 2: Validate arguments against tool's input schema
        try {
            Object schemaObject = tool.getInputSchema();
            if (schemaObject != null) {
                String validationError = validateArguments(schemaObject, arguments);
                if (validationError != null) {
                    LOGGER.log(Level.WARNING, "Invalid arguments for tool {0}: {1}", 
                            new Object[]{toolName, validationError});
                    return ToolResult.error("Invalid arguments: " + validationError);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Schema validation error for tool " + toolName, e);
            return ToolResult.error("Schema validation error: " + e.getMessage());
        }
        
        // Step 3: Execute tool with validated arguments
        try {
            LOGGER.log(Level.FINE, "Executing tool: {0}", toolName);
            ToolResult result = tool.execute(arguments);
            return result;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Tool execution failed for " + toolName, e);
            return ToolResult.error("Tool execution failed: " + e.getMessage());
        }
    }
    
    /**
     * Validates arguments against a JSON Schema.
     * 
     * <p>Converts the schema object to JSON, creates a validator, and validates
     * the arguments. Returns null if validation passes, or an error message if
     * validation fails.</p>
     * 
     * @param schemaObject the JSON Schema as a Java object (Map, List, etc.)
     * @param arguments the arguments to validate
     * @return null if validation passes, or an error message describing validation failures
     */
    private String validateArguments(Object schemaObject, Map<String, Object> arguments) {
        try {
            // Convert schema object to JSON string using shared Jsonb instance
            String schemaJson = JSONB.toJson(schemaObject);

            // Create JSON Schema validator
            JsonSchema schema = schemaFactory.getSchema(schemaJson);

            // Convert arguments to JSON string, then to a Jackson JsonNode for the validator
            String argumentsJson = JSONB.toJson(arguments);
            JsonNode jsonNode = OBJECT_MAPPER.readTree(argumentsJson);

            // Validate arguments
            Set<ValidationMessage> errors = schema.validate(jsonNode);
            
            if (errors.isEmpty()) {
                return null;
            }
            
            // Collect validation error messages
            return errors.stream()
                    .map(ValidationMessage::getMessage)
                    .collect(Collectors.joining("; "));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during argument validation", e);
            return "Validation error: " + e.getMessage();
        }
    }
}
