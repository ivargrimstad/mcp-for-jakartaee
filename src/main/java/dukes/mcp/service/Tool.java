package dukes.mcp.service;

import dukes.mcp.model.ToolResult;
import java.util.Map;

/**
 * Interface defining the contract for MCP tool implementations.
 * 
 * <p>Tools are callable functions exposed through the MCP protocol that AI clients
 * can discover and execute. Each tool must provide metadata (name, description, 
 * input schema) and implement the execution logic.</p>
 * 
 * <p>Tool implementations should:</p>
 * <ul>
 *   <li>Return a unique name that identifies the tool</li>
 *   <li>Provide a clear, actionable description for AI clients</li>
 *   <li>Define a JSON Schema for input validation</li>
 *   <li>Handle execution errors gracefully and return appropriate ToolResult</li>
 * </ul>
 * 
 * <p>Example implementation:</p>
 * <pre>{@code
 * @ApplicationScoped
 * public class SystemInfoTool implements Tool {
 *     @Override
 *     public String getName() {
 *         return "system_info";
 *     }
 *     
 *     @Override
 *     public String getDescription() {
 *         return "Returns system information including Java version and OS details";
 *     }
 *     
 *     @Override
 *     public Object getInputSchema() {
 *         return Map.of(
 *             "type", "object",
 *             "properties", Map.of()
 *         );
 *     }
 *     
 *     @Override
 *     public ToolResult execute(Map<String, Object> arguments) {
 *         try {
 *             String info = "Java: " + System.getProperty("java.version");
 *             return ToolResult.success(info);
 *         } catch (Exception e) {
 *             return ToolResult.error("Failed to get system info: " + e.getMessage());
 *         }
 *     }
 * }
 * }</pre>
 * 
 * @see ToolResult
 * @see dukes.mcp.model.ToolDefinition
 */
public interface Tool {
    
    /**
     * Returns the unique name of this tool.
     * 
     * <p>The name is used to identify and invoke the tool through the MCP protocol.
     * It must be unique within the tool registry and should follow naming conventions
     * (e.g., snake_case like "database_query" or "system_info").</p>
     * 
     * @return the tool name, must be non-null and non-empty
     */
    String getName();
    
    /**
     * Returns a human-readable description of what this tool does.
     * 
     * <p>The description should be clear and actionable, helping AI clients understand
     * when and how to use the tool. It should describe the tool's purpose, expected
     * inputs, and what kind of output it produces.</p>
     * 
     * @return the tool description, must be non-null and non-empty
     */
    String getDescription();
    
    /**
     * Returns the JSON Schema defining the structure and validation rules for tool arguments.
     * 
     * <p>The input schema is used to validate arguments before tool execution. It should
     * be a valid JSON Schema (draft 2020-12) represented as a Java object (typically a Map).
     * The schema defines required fields, data types, constraints, and validation rules.</p>
     * 
     * <p>Example schema for a tool that accepts a query string:</p>
     * <pre>{@code
     * Map.of(
     *     "type", "object",
     *     "properties", Map.of(
     *         "query", Map.of(
     *             "type", "string",
     *             "description", "The query to execute"
     *         )
     *     ),
     *     "required", List.of("query")
     * )
     * }</pre>
     * 
     * @return the JSON Schema for tool arguments, must be non-null
     */
    Object getInputSchema();
    
    /**
     * Executes the tool with the provided arguments.
     * 
     * <p>This method performs the actual tool operation. Arguments have already been
     * validated against the input schema before this method is called. The implementation
     * should handle any execution errors gracefully and return an appropriate ToolResult.</p>
     * 
     * <p>For successful execution, return {@link ToolResult#success(String)} with the
     * result content. For failures, return {@link ToolResult#error(String)} with an
     * error message.</p>
     * 
     * <p>The method should not throw exceptions - all errors should be caught and
     * returned as error ToolResults. This ensures consistent error handling across
     * the MCP protocol.</p>
     * 
     * @param arguments the validated tool arguments as a map of parameter names to values
     * @return a ToolResult containing either the execution result or error information
     * @throws NullPointerException if arguments is null
     */
    ToolResult execute(Map<String, Object> arguments);
}
