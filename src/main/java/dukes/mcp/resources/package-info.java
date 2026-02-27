/**
 * Example resource implementations for the MCP Server.
 * 
 * <p>This package contains concrete implementations of the {@link dukes.mcp.service.Resource}
 * interface that demonstrate how to expose various types of data through the MCP protocol.</p>
 * 
 * <p>Resources in this package serve as examples for developers creating custom resources
 * for their Jakarta EE applications. They demonstrate best practices for:</p>
 * <ul>
 *   <li>Reading configuration files from the classpath</li>
 *   <li>Accessing database metadata and schema information</li>
 *   <li>Handling I/O errors gracefully</li>
 *   <li>Returning appropriate MIME types for different content formats</li>
 * </ul>
 * 
 * <p>Example resources included:</p>
 * <ul>
 *   <li>{@link dukes.mcp.resources.ConfigurationResource} - Reads application.properties file</li>
 * </ul>
 * 
 * @see dukes.mcp.service.Resource
 * @see dukes.mcp.service.ResourceManager
 */
package dukes.mcp.resources;
