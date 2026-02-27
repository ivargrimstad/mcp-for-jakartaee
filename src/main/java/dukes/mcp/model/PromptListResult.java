package dukes.mcp.model;

import jakarta.json.bind.annotation.JsonbProperty;
import java.util.List;
import java.util.Objects;

/**
 * Represents the result of the prompts/list method.
 * 
 * Contains a list of all available prompt definitions.
 */
public class PromptListResult {
    
    @JsonbProperty("prompts")
    private List<PromptDefinition> prompts;
    
    /**
     * Default constructor for JSON-B deserialization.
     */
    public PromptListResult() {
    }
    
    /**
     * Creates a prompt list result with prompts.
     * 
     * @param prompts the list of prompt definitions
     */
    public PromptListResult(List<PromptDefinition> prompts) {
        this.prompts = prompts;
    }
    
    public List<PromptDefinition> getPrompts() {
        return prompts;
    }
    
    public void setPrompts(List<PromptDefinition> prompts) {
        this.prompts = prompts;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PromptListResult that = (PromptListResult) o;
        return Objects.equals(prompts, that.prompts);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(prompts);
    }
    
    @Override
    public String toString() {
        return "PromptListResult{" +
                "prompts=" + prompts +
                '}';
    }
}
