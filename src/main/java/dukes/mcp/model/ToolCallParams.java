package dukes.mcp.model;

import jakarta.json.bind.annotation.JsonbProperty;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the parameters for the tools/call method.
 * 
 * Contains the tool name and arguments to pass to the tool.
 */
public class ToolCallParams {
    
    @JsonbProperty("name")
    private String name;
    
    @JsonbProperty("arguments")
    private Map<String, Object> arguments;
    
    /**
     * Default constructor for JSON-B deserialization.
     */
    public ToolCallParams() {
    }
    
    /**
     * Creates tool call parameters with name and arguments.
     * 
     * @param name the tool name
     * @param arguments the tool arguments
     */
    public ToolCallParams(String name, Map<String, Object> arguments) {
        this.name = name;
        this.arguments = arguments;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Map<String, Object> getArguments() {
        return arguments;
    }
    
    public void setArguments(Map<String, Object> arguments) {
        this.arguments = arguments;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ToolCallParams that = (ToolCallParams) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(arguments, that.arguments);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, arguments);
    }
    
    @Override
    public String toString() {
        return "ToolCallParams{" +
                "name='" + name + '\'' +
                ", arguments=" + arguments +
                '}';
    }
}
