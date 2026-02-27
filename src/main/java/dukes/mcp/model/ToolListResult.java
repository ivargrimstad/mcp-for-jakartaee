package dukes.mcp.model;

import jakarta.json.bind.annotation.JsonbProperty;
import java.util.List;
import java.util.Objects;

/**
 * Represents the result of the tools/list method.
 * 
 * Contains a list of all available tool definitions.
 */
public class ToolListResult {
    
    @JsonbProperty("tools")
    private List<ToolDefinition> tools;
    
    /**
     * Default constructor for JSON-B deserialization.
     */
    public ToolListResult() {
    }
    
    /**
     * Creates a tool list result with tools.
     * 
     * @param tools the list of tool definitions
     */
    public ToolListResult(List<ToolDefinition> tools) {
        this.tools = tools;
    }
    
    public List<ToolDefinition> getTools() {
        return tools;
    }
    
    public void setTools(List<ToolDefinition> tools) {
        this.tools = tools;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ToolListResult that = (ToolListResult) o;
        return Objects.equals(tools, that.tools);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(tools);
    }
    
    @Override
    public String toString() {
        return "ToolListResult{" +
                "tools=" + tools +
                '}';
    }
}
