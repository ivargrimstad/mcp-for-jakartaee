package dukes.mcp.model;

import jakarta.json.bind.annotation.JsonbProperty;
import java.util.Objects;

/**
 * Represents an argument definition for a prompt.
 * 
 * Describes a parameter that can be passed to a prompt template,
 * including whether it is required.
 */
public class PromptArgument {
    
    @JsonbProperty("name")
    private String name;
    
    @JsonbProperty("description")
    private String description;
    
    @JsonbProperty("required")
    private Boolean required;
    
    /**
     * Default constructor for JSON-B deserialization.
     */
    public PromptArgument() {
    }
    
    /**
     * Creates a prompt argument with name, description, and required flag.
     * 
     * @param name the argument name
     * @param description the argument description
     * @param required whether the argument is required
     */
    public PromptArgument(String name, String description, boolean required) {
        this.name = name;
        this.description = description;
        this.required = required;
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
    
    public Boolean getRequired() {
        return required;
    }
    
    public void setRequired(Boolean required) {
        this.required = required;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PromptArgument that = (PromptArgument) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(description, that.description) &&
               Objects.equals(required, that.required);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, description, required);
    }
    
    @Override
    public String toString() {
        return "PromptArgument{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", required=" + required +
                '}';
    }
}
