/**
 * Example tool implementations for the MCP Server.
 * 
 * <p>This package contains concrete implementations of the {@link dukes.mcp.service.Tool}
 * interface that demonstrate how to create custom tools for the MCP protocol.</p>
 * 
 * <p>Example tools include:</p>
 * <ul>
 *   <li>{@link dukes.mcp.tools.DatabaseQueryTool} - Execute database queries using JPQL</li>
 * </ul>
 * 
 * <p>These examples demonstrate:</p>
 * <ul>
 *   <li>Proper JSON Schema definition for input validation</li>
 *   <li>Integration with Jakarta EE services (EntityManager, etc.)</li>
 *   <li>Graceful error handling and exception management</li>
 *   <li>CDI integration with @ApplicationScoped beans</li>
 * </ul>
 */
package dukes.mcp.tools;
