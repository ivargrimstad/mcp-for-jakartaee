package dukes.mcp.model;

import jakarta.json.bind.annotation.JsonbProperty;
import java.util.Objects;

/**
 * Represents a message in a prompt.
 * 
 * Contains the role (e.g., "user", "assistant") and content.
 */
public class PromptMessage {
    
    @JsonbProperty("role")
    private String role;
    
    @JsonbProperty("content")
    private ContentItem content;
    
    /**
     * Default constructor for JSON-B deserialization.
     */
    public PromptMessage() {
    }
    
    /**
     * Creates a prompt message with role and content.
     * 
     * @param role the message role
     * @param content the message content
     */
    public PromptMessage(String role, ContentItem content) {
        this.role = role;
        this.content = content;
    }
    
    /**
     * Creates a user prompt message with text content.
     * 
     * @param text the message text
     * @return a user prompt message
     */
    public static PromptMessage user(String text) {
        return new PromptMessage("user", new ContentItem("text", text));
    }
    
    /**
     * Creates an assistant prompt message with text content.
     * 
     * @param text the message text
     * @return an assistant prompt message
     */
    public static PromptMessage assistant(String text) {
        return new PromptMessage("assistant", new ContentItem("text", text));
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public ContentItem getContent() {
        return content;
    }
    
    public void setContent(ContentItem content) {
        this.content = content;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PromptMessage that = (PromptMessage) o;
        return Objects.equals(role, that.role) &&
               Objects.equals(content, that.content);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(role, content);
    }
    
    @Override
    public String toString() {
        return "PromptMessage{" +
                "role='" + role + '\'' +
                ", content=" + content +
                '}';
    }
}
