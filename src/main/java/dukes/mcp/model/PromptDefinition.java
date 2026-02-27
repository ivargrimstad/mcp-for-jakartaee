package dukes.mcp.model;

import jakarta.json.bind.annotation.JsonbProperty;
import java.util.List;
import java.util.Objects;

/**
 * Represents a prompt definition in the MCP protocol.
 * 
 * A prompt definition describes a template with its name,
 * description, and required/optional arguments.
 */
public class PromptDefinition {
    
    @JsonbProperty("name")
    private String name;
    
    @JsonbProperty("description")
    private String description;
    
    @JsonbProperty("arguments")
    private List<PromptArgument> arguments;
    
    /**
     * Default constructor for JSON-B deserialization.
     */
    public PromptDefinition() {
    }
    
    /**
     * Creates a prompt definition with name, description, and arguments.
     * 
     * @param name the prompt name
     * @param description the prompt description
     * @param arguments the prompt arguments
     */
    public PromptDefinition(String name, String description, List<PromptArgument> arguments) {
        this.name = name;
        this.description = description;
        this.arguments = arguments;
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
    
    public List<PromptArgument> getArguments() {
        return arguments;
    }
    
    public void setArguments(List<PromptArgument> arguments) {
        this.arguments = arguments;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PromptDefinition that = (PromptDefinition) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(description, that.description) &&
               Objects.equals(arguments, that.arguments);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, description, arguments);
    }
    
    @Override
    public String toString() {
        return "PromptDefinition{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", arguments=" + arguments +
                '}';
    }
}
