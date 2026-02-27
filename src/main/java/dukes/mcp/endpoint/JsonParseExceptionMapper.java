package dukes.mcp.endpoint;

import dukes.mcp.model.JsonRpcResponse;
import jakarta.json.bind.JsonbException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JAX-RS exception mapper that handles JSON parsing errors.
 * 
 * <p>This mapper catches JSON-B exceptions that occur during request deserialization
 * and converts them to valid JSON-RPC error responses with error code -32700 (Parse error).
 * This ensures that malformed JSON is handled gracefully and returns a proper JSON-RPC
 * error response instead of a generic HTTP error.</p>
 * 
 * <p>Per JSON-RPC 2.0 specification:</p>
 * <ul>
 *   <li>Error code -32700 indicates a parse error (invalid JSON)</li>
 *   <li>All responses should use HTTP 200 status code</li>
 *   <li>Errors are communicated through the JSON-RPC error object, not HTTP status</li>
 * </ul>
 * 
 * <p>Example malformed JSON that triggers this mapper:</p>
 * <pre>{@code
 * POST /mcp
 * Content-Type: application/json
 * 
 * {invalid json}
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
 *     "code": -32700,
 *     "message": "Parse error",
 *     "data": "Invalid JSON: ..."
 *   }
 * }
 * }</pre>
 * 
 * @see MCPEndpoint
 * @see JsonRpcResponse
 */
@Provider
public class JsonParseExceptionMapper implements ExceptionMapper<JsonbException> {
    
    private static final Logger LOGGER = Logger.getLogger(JsonParseExceptionMapper.class.getName());
    
    /**
     * JSON-RPC error code for parse errors.
     */
    private static final int ERROR_PARSE_ERROR = -32700;
    
    /**
     * Converts a JSON-B exception to a JSON-RPC error response.
     * 
     * <p>This method is automatically called by JAX-RS when a JsonbException
     * occurs during request processing (typically during JSON deserialization).</p>
     * 
     * @param exception the JSON-B exception that occurred
     * @return HTTP response with JSON-RPC error and status 200
     */
    @Override
    public Response toResponse(JsonbException exception) {
        LOGGER.log(Level.WARNING, "JSON parse error: " + exception.getMessage(), exception);
        
        // Build JSON-RPC error response
        // Note: id is null because we couldn't parse the request to extract it
        JsonRpcResponse errorResponse = JsonRpcResponse.error(
            null,
            ERROR_PARSE_ERROR,
            "Parse error",
            "Invalid JSON: " + exception.getMessage()
        );
        
        // Return HTTP 200 with JSON-RPC error (per JSON-RPC 2.0 spec)
        return Response.ok(errorResponse)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
