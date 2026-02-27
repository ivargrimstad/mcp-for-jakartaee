package dukes.mcp.model;

import jakarta.json.bind.annotation.JsonbProperty;
import java.util.List;
import java.util.Objects;

/**
 * Represents the result of the prompts/get method.
 * 
 * Contains the rendered prompt messages.
 */
public class PromptGetResult {
    
    @JsonbProperty("messages")
    private List<PromptMessage> messages;
    
    /**
     * Default constructor for JSON-B deserialization.
     */
    public PromptGetResult() {
    }
    
    /**
     * Creates a prompt get result with messages.
     * 
     * @param messages the prompt messages
     */
    public PromptGetResult(List<PromptMessage> messages) {
        this.messages = messages;
    }
    
    public List<PromptMessage> getMessages() {
        return messages;
    }
    
    public void setMessages(List<PromptMessage> messages) {
        this.messages = messages;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PromptGetResult that = (PromptGetResult) o;
        return Objects.equals(messages, that.messages);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(messages);
    }
    
    @Override
    public String toString() {
        return "PromptGetResult{" +
                "messages=" + messages +
                '}';
    }
}
