package dukes.mcp.model;

import jakarta.json.bind.annotation.JsonbProperty;
import java.util.Objects;

/**
 * Represents a JSON-RPC 2.0 request.
 * 
 * A request object contains the protocol version, method name, parameters,
 * and an optional identifier for matching with responses.
 */
public class JsonRpcRequest {
    
    @JsonbProperty("jsonrpc")
    private String jsonrpc;
    
    @JsonbProperty("method")
    private String method;
    
    @JsonbProperty("params")
    private Object params;
    
    @JsonbProperty("id")
    private Object id;
    
    /**
     * Default constructor for JSON-B deserialization.
     */
    public JsonRpcRequest() {
    }
    
    /**
     * Creates a JSON-RPC request with method and parameters.
     * 
     * @param method the method name
     * @param params the method parameters
     */
    public JsonRpcRequest(String method, Object params) {
        this.jsonrpc = "2.0";
        this.method = method;
        this.params = params;
    }
    
    /**
     * Creates a JSON-RPC request with method, parameters, and id.
     * 
     * @param method the method name
     * @param params the method parameters
     * @param id the request identifier
     */
    public JsonRpcRequest(String method, Object params, Object id) {
        this.jsonrpc = "2.0";
        this.method = method;
        this.params = params;
        this.id = id;
    }
    
    /**
     * Validates that this request conforms to JSON-RPC 2.0 specification.
     * 
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (jsonrpc == null || !jsonrpc.equals("2.0")) {
            throw new IllegalArgumentException("jsonrpc field must be \"2.0\"");
        }
        if (method == null || method.trim().isEmpty()) {
            throw new IllegalArgumentException("method field must be present and non-empty");
        }
    }
    
    /**
     * Checks if this is a notification (request without id).
     * 
     * @return true if this is a notification, false otherwise
     */
    public boolean isNotification() {
        return id == null;
    }
    
    public String getJsonrpc() {
        return jsonrpc;
    }
    
    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
    
    public Object getParams() {
        return params;
    }
    
    public void setParams(Object params) {
        this.params = params;
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
        JsonRpcRequest that = (JsonRpcRequest) o;
        return Objects.equals(jsonrpc, that.jsonrpc) &&
               Objects.equals(method, that.method) &&
               Objects.equals(params, that.params) &&
               Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(jsonrpc, method, params, id);
    }
    
    @Override
    public String toString() {
        return "JsonRpcRequest{" +
                "jsonrpc='" + jsonrpc + '\'' +
                ", method='" + method + '\'' +
                ", params=" + params +
                ", id=" + id +
                '}';
    }
}
