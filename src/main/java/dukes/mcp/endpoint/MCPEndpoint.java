package dukes.mcp.endpoint;

import dukes.mcp.model.JsonRpcRequest;
import dukes.mcp.model.JsonRpcResponse;
import dukes.mcp.service.MCPProtocolHandler;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JAX-RS endpoint that exposes the MCP protocol over HTTP.
 * 
 * <p>This endpoint accepts JSON-RPC 2.0 requests at the "/mcp" path and delegates
 * processing to the MCPProtocolHandler. All requests are handled via HTTP POST
 * with JSON content, and all responses are returned with HTTP 200 status code
 * (errors are communicated through JSON-RPC error responses, not HTTP status codes).</p>
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li>Accepts POST requests with application/json content type</li>
 *   <li>Returns JSON responses with application/json content type</li>
 *   <li>Always returns HTTP 200 status (per JSON-RPC 2.0 specification)</li>
 *   <li>Delegates all protocol logic to MCPProtocolHandler</li>
 *   <li>Handles JSON parsing errors gracefully</li>
 * </ul>
 * 
 * <p>Example request:</p>
 * <pre>{@code
 * POST /mcp
 * Content-Type: application/json
 * 
 * {
 *   "jsonrpc": "2.0",
 *   "id": "1",
 *   "method": "tools/list"
 * }
 * }</pre>
 * 
 * <p>Example response:</p>
 * <pre>{@code
 * HTTP/1.1 200 OK
 * Content-Type: application/json
 * 
 * {
 *   "jsonrpc": "2.0",
 *   "id": "1",
 *   "result": {
 *     "tools": [...]
 *   }
 * }
 * }</pre>
 * 
 * @see MCPProtocolHandler
 * @see JsonRpcRequest
 * @see JsonRpcResponse
 */
@Path("/mcp")
@RequestScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN, MediaType.WILDCARD})
public class MCPEndpoint {
    
    private static final Logger LOGGER = Logger.getLogger(MCPEndpoint.class.getName());
    
    @Inject
    private MCPProtocolHandler protocolHandler;
    
    /**
     * Handles MCP protocol requests via HTTP POST.
     * 
     * <p>This method:</p>
     * <ol>
     *   <li>Receives a JSON-RPC request from the client</li>
     *   <li>Delegates processing to MCPProtocolHandler</li>
     *   <li>Returns the JSON-RPC response with HTTP 200 status</li>
     * </ol>
     * 
     * <p>All errors (including JSON parsing errors) are caught and returned as
     * valid JSON-RPC error responses with HTTP 200 status. This follows the
     * JSON-RPC 2.0 specification which states that HTTP-level errors should
     * only be used for transport-level issues, not protocol-level errors.</p>
     * 
     * @param request the JSON-RPC request from the client
     * @return HTTP response with JSON-RPC response body and status 200
     */
    /**
     * Handles unsupported GET requests to the MCP endpoint.
     *
     * <p>MCP clients using the Streamable HTTP transport send a GET probe to discover
     * the server. This endpoint returns 405 with an {@code Allow: POST} header so the
     * client understands only POST is supported, without routing through the generic
     * exception mapper (which would produce a malformed JSON-RPC error body).</p>
     *
     * @return HTTP 405 with Allow header
     */
    @GET
    public Response rejectGet() {
        return Response.status(Response.Status.METHOD_NOT_ALLOWED)
                .header("Allow", "POST")
                .build();
    }

    @POST
    public Response handleRequest(JsonRpcRequest request) {
        LOGGER.log(Level.FINE, "Received MCP request: {0}", request);
        
        try {
            // Delegate to protocol handler
            JsonRpcResponse jsonRpcResponse = protocolHandler.processRequest(request);

            // Notifications (no id) produce a null response — return empty 200 per JSON-RPC 2.0 spec
            if (jsonRpcResponse == null) {
                return Response.ok().build();
            }

            LOGGER.log(Level.FINE, "Returning MCP response: {0}", jsonRpcResponse);
            
            // Return JSON-RPC response with HTTP 200 status
            return Response.ok(jsonRpcResponse).build();
            
        } catch (Exception e) {
            // Catch any unexpected exceptions and return as JSON-RPC error
            LOGGER.log(Level.SEVERE, "Unexpected error processing MCP request", e);
            
            // Build error response
            JsonRpcResponse errorResponse = JsonRpcResponse.error(
                request != null ? request.getId() : null,
                -32603,
                "Internal error",
                e.getMessage()
            );
            
            // Always return HTTP 200 with JSON-RPC error
            return Response.ok(errorResponse).build();
        }
    }
}
