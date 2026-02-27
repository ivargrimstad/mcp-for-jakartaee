package dukes.mcp.endpoint;

import dukes.mcp.model.JsonRpcResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JAX-RS exception mapper that handles all uncaught exceptions.
 * 
 * <p>This mapper serves as a catch-all for any exceptions that are not handled
 * by more specific exception mappers. It ensures that no exceptions propagate
 * to the HTTP layer and that all errors are returned as valid JSON-RPC error
 * responses with HTTP 200 status.</p>
 * 
 * <p>Per JSON-RPC 2.0 specification:</p>
 * <ul>
 *   <li>Error code -32603 indicates an internal error</li>
 *   <li>All responses should use HTTP 200 status code</li>
 *   <li>Errors are communicated through the JSON-RPC error object, not HTTP status</li>
 * </ul>
 * 
 * <p>This mapper has lower priority than specific exception mappers (like
 * JsonParseExceptionMapper) and will only be invoked if no other mapper
 * handles the exception.</p>
 * 
 * <p>Example exception that triggers this mapper:</p>
 * <pre>{@code
 * // Any uncaught RuntimeException, IOException, etc.
 * throw new RuntimeException("Unexpected error");
 * }</pre>
 * 
 * <p>Response:</p>
 * <pre>{@code
 * HTTP/1.1 200 OK
 * Content-Type: application/json
 * 
 * {
 *   "jsonrpc": "2.0",
 *   "id": null,
 *   "error": {
 *     "code": -32603,
 *     "message": "Internal error",
 *     "data": "Unexpected error"
 *   }
 * }
 * }</pre>
 * 
 * @see MCPEndpoint
 * @see JsonRpcResponse
 * @see JsonParseExceptionMapper
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {
    
    private static final Logger LOGGER = Logger.getLogger(GenericExceptionMapper.class.getName());
    
    /**
     * JSON-RPC error code for internal errors.
     */
    private static final int ERROR_INTERNAL_ERROR = -32603;
    
    /**
     * Converts any uncaught exception to a JSON-RPC error response.
     * 
     * <p>This method is automatically called by JAX-RS when an exception
     * occurs during request processing and no more specific exception mapper
     * is available to handle it.</p>
     * 
     * <p>The method ensures that:</p>
     * <ul>
     *   <li>No exceptions propagate to the HTTP layer</li>
     *   <li>All errors are returned as valid JSON-RPC responses</li>
     *   <li>HTTP 200 status is always returned (per JSON-RPC 2.0 spec)</li>
     *   <li>Error details are included in the response data field</li>
     * </ul>
     * 
     * @param exception the exception that occurred
     * @return HTTP response with JSON-RPC error and status 200
     */
    @Override
    public Response toResponse(Throwable exception) {
        LOGGER.log(Level.SEVERE, "Uncaught exception in MCP endpoint: " + exception.getMessage(), exception);
        
        // Build JSON-RPC error response
        // Note: id is null because we don't have access to the request at this point
        JsonRpcResponse errorResponse = JsonRpcResponse.error(
            null,
            ERROR_INTERNAL_ERROR,
            "Internal error",
            exception.getMessage()
        );
        
        // Return HTTP 200 with JSON-RPC error (per JSON-RPC 2.0 spec)
        return Response.ok(errorResponse)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
