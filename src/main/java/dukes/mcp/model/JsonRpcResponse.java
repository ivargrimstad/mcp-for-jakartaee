package dukes.mcp.model;

import jakarta.json.bind.annotation.JsonbProperty;
import java.util.Objects;

/**
 * Represents a JSON-RPC 2.0 response.
 * 
 * A response object contains the protocol version, either a result or error
 * (but not both), and an identifier matching the corresponding request.
 */
public class JsonRpcResponse {
    
    @JsonbProperty("jsonrpc")
    private String jsonrpc;
    
    @JsonbProperty("result")
    private Object result;
    
    @JsonbProperty("error")
    private JsonRpcError error;
    
    @JsonbProperty("id")
    private Object id;
    
    /**
     * Default constructor for JSON-B deserialization.
     */
    public JsonRpcResponse() {
    }
    
    /**
     * Creates a JSON-RPC response with id.
     * 
     * @param id the request identifier
     */
    public JsonRpcResponse(Object id) {
        this.jsonrpc = "2.0";
        this.id = id;
    }
    
    /**
     * Creates a successful JSON-RPC response.
     * 
     * @param id the request identifier
     * @param result the result object
     * @return a success response
     */
    public static JsonRpcResponse success(Object id, Object result) {
        JsonRpcResponse response = new JsonRpcResponse(id);
        response.setResult(result);
        return response;
    }
    
    /**
     * Creates an error JSON-RPC response.
     * 
     * @param id the request identifier
     * @param error the error object
     * @return an error response
     */
    public static JsonRpcResponse error(Object id, JsonRpcError error) {
        JsonRpcResponse response = new JsonRpcResponse(id);
        response.setError(error);
        return response;
    }
    
    /**
     * Creates an error JSON-RPC response with code and message.
     * 
     * @param id the request identifier
     * @param code the error code
     * @param message the error message
     * @return an error response
     */
    public static JsonRpcResponse error(Object id, int code, String message) {
        return error(id, new JsonRpcError(code, message));
    }
    
    /**
     * Creates an error JSON-RPC response with code, message, and data.
     * 
     * @param id the request identifier
     * @param code the error code
     * @param message the error message
     * @param data additional error details
     * @return an error response
     */
    public static JsonRpcResponse error(Object id, int code, String message, Object data) {
        return error(id, new JsonRpcError(code, message, data));
    }
    
    /**
     * Validates that this response conforms to JSON-RPC 2.0 specification.
     * 
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (jsonrpc == null || !jsonrpc.equals("2.0")) {
            throw new IllegalArgumentException("jsonrpc field must be \"2.0\"");
        }
        if (result != null && error != null) {
            throw new IllegalArgumentException("response must contain either result or error, not both");
        }
        if (result == null && error == null) {
            throw new IllegalArgumentException("response must contain either result or error");
        }
    }
    
    /**
     * Checks if this response represents an error.
     * 
     * @return true if this is an error response, false otherwise
     */
    public boolean isError() {
        return error != null;
    }
    
    /**
     * Checks if this response represents a success.
     * 
     * @return true if this is a success response, false otherwise
     */
    public boolean isSuccess() {
        return result != null;
    }
    
    public String getJsonrpc() {
        return jsonrpc;
    }
    
    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }
    
    public Object getResult() {
        return result;
    }
    
    public void setResult(Object result) {
        this.result = result;
        this.error = null; // Ensure mutual exclusivity
    }
    
    public JsonRpcError getError() {
        return error;
    }
    
    public void setError(JsonRpcError error) {
        this.error = error;
        this.result = null; // Ensure mutual exclusivity
    }
    
    public Object getId() {
        return id;
    }
    
    public void setId(Object id) {
        this.id = id;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonRpcResponse that = (JsonRpcResponse) o;
        return Objects.equals(jsonrpc, that.jsonrpc) &&
               Objects.equals(result, that.result) &&
               Objects.equals(error, that.error) &&
               Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(jsonrpc, result, error, id);
    }
    
    @Override
    public String toString() {
        return "JsonRpcResponse{" +
                "jsonrpc='" + jsonrpc + '\'' +
                ", result=" + result +
                ", error=" + error +
                ", id=" + id +
                '}';
    }
}
