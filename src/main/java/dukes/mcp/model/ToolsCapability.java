package dukes.mcp.model;

import java.util.Objects;

/**
 * Represents the tools capability of the MCP server.
 * 
 * An empty object indicates that tools are supported.
 */
public class ToolsCapability {
    
    /**
     * Default constructor for JSON-B deserialization.
     */
    public ToolsCapability() {
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
        return "ToolsCapability{}";
    }
}
