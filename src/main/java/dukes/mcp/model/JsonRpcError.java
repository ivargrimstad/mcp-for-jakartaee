package dukes.mcp.model;

import jakarta.json.bind.annotation.JsonbProperty;
import java.util.Objects;

/**
 * Represents a JSON-RPC 2.0 error object.
 * 
 * An error object is included in the response when a request fails.
 * It contains an error code, message, and optional additional data.
 */
public class JsonRpcError {
    
    @JsonbProperty("code")
    private int code;
    
    @JsonbProperty("message")
    private String message;
    
    @JsonbProperty("data")
    private Object data;
    
    /**
     * Default constructor for JSON-B deserialization.
     */
    public JsonRpcError() {
    }
    
    /**
     * Creates a JSON-RPC error with code and message.
     * 
     * @param code the error code
     * @param message the error message
     */
    public JsonRpcError(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    /**
     * Creates a JSON-RPC error with code, message, and additional data.
     * 
     * @param code the error code
     * @param message the error message
     * @param data additional error details
     */
    public JsonRpcError(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonRpcError that = (JsonRpcError) o;
        return code == that.code && 
               Objects.equals(message, that.message) && 
               Objects.equals(data, that.data);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(code, message, data);
    }
    
    @Override
    public String toString() {
        return "JsonRpcError{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
