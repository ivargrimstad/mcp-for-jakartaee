package dukes.mcp.model;

import jakarta.json.bind.annotation.JsonbProperty;
import java.util.Objects;

/**
 * Represents the capabilities supported by the MCP client.
 * 
 * Client capabilities indicate which features the client supports.
 */
public class ClientCapabilities {
    
    @JsonbProperty("experimental")
    private Object experimental;
    
    /**
     * Default constructor for JSON-B deserialization.
     */
    public ClientCapabilities() {
    }
    
    public Object getExperimental() {
        return experimental;
    }
    
    public void setExperimental(Object experimental) {
        this.experimental = experimental;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientCapabilities that = (ClientCapabilities) o;
        return Objects.equals(experimental, that.experimental);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(experimental);
    }
    
    @Override
    public String toString() {
        return "ClientCapabilities{" +
                "experimental=" + experimental +
                '}';
    }
}
