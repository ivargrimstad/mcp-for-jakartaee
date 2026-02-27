/**
 * JAX-RS endpoints for exposing the MCP protocol over HTTP.
 * 
 * <p>This package contains the REST endpoints that provide HTTP access to the
 * Model Context Protocol (MCP) server. The endpoints accept JSON-RPC 2.0 requests
 * and return JSON-RPC 2.0 responses, following the MCP specification.</p>
 * 
 * <p>Key components:</p>
 * <ul>
 *   <li>{@link dukes.mcp.endpoint.MCPEndpoint} - Main MCP protocol endpoint at /mcp</li>
 * </ul>
 * 
 * @see dukes.mcp.service.MCPProtocolHandler
 */
package dukes.mcp.endpoint;
