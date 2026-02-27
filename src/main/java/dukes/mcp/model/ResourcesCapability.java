package dukes.mcp.model;

import java.util.Objects;

/**
 * Represents the resources capability of the MCP server.
 * 
 * An empty object indicates that resources are supported.
 */
public class ResourcesCapability {
    
    /**
     * Default constructor for JSON-B deserialization.
     */
    public ResourcesCapability() {
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
        return "ResourcesCapability{}";
    }
}
