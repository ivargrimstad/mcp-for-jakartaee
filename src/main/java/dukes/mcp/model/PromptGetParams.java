package dukes.mcp.model;

import jakarta.json.bind.annotation.JsonbProperty;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the parameters for the prompts/get method.
 * 
 * Contains the prompt name and arguments to pass to the prompt template.
 */
public class PromptGetParams {
    
    @JsonbProperty("name")
    private String name;
    
    @JsonbProperty("arguments")
    private Map<String, String> arguments;
    
    /**
     * Default constructor for JSON-B deserialization.
     */
    public PromptGetParams() {
    }
    
    /**
     * Creates prompt get parameters with name and arguments.
     * 
     * @param name the prompt name
     * @param arguments the prompt arguments
     */
    public PromptGetParams(String name, Map<String, String> arguments) {
        this.name = name;
        this.arguments = arguments;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Map<String, String> getArguments() {
        return arguments;
    }
    
    public void setArguments(Map<String, String> arguments) {
        this.arguments = arguments;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PromptGetParams that = (PromptGetParams) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(arguments, that.arguments);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, arguments);
    }
    
    @Override
    public String toString() {
        return "PromptGetParams{" +
                "name='" + name + '\'' +
                ", arguments=" + arguments +
                '}';
    }
}
