package dukes.mcp.model;

import java.util.Objects;

/**
 * Represents the prompts capability of the MCP server.
 * 
 * An empty object indicates that prompts are supported.
 */
public class PromptsCapability {
    
    /**
     * Default constructor for JSON-B deserialization.
     */
    public PromptsCapability() {
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o != null && getClass() == o.getClass();
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getClass());
    }
    
    @Override
    public String toString() {
        return "PromptsCapability{}";
    }
}
