package dukes.mcp.model;

import jakarta.json.bind.annotation.JsonbProperty;
import java.util.Objects;

/**
 * Represents a tool definition in the MCP protocol.
 * 
 * A tool definition describes a callable function with its name,
 * description, and input schema for validation.
 */
public class ToolDefinition {
    
    @JsonbProperty("name")
    private String name;
    
    @JsonbProperty("description")
    private String description;
    
    @JsonbProperty("inputSchema")
    private Object inputSchema;
    
    /**
     * Default constructor for JSON-B deserialization.
     */
    public ToolDefinition() {
    }
    
    /**
     * Creates a tool definition with name, description, and input schema.
     * 
     * @param name the tool name
     * @param description the tool description
     * @param inputSchema the JSON Schema for tool arguments
     */
    public ToolDefinition(String name, String description, Object inputSchema) {
        this.name = name;
        this.description = description;
        this.inputSchema = inputSchema;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Object getInputSchema() {
        return inputSchema;
    }
    
    public void setInputSchema(Object inputSchema) {
        this.inputSchema = inputSchema;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ToolDefinition that = (ToolDefinition) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(description, that.description) &&
               Objects.equals(inputSchema, that.inputSchema);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, description, inputSchema);
    }
    
    @Override
    public String toString() {
        return "ToolDefinition{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", inputSchema=" + inputSchema +
                '}';
    }
}
