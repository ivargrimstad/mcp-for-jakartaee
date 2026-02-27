package dukes.mcp.model;

import jakarta.json.bind.annotation.JsonbProperty;
import java.util.Objects;

/**
 * Represents a content item in tool results or prompt content.
 * 
 * Content items can be text, images, or other types of content.
 */
public class ContentItem {
    
    @JsonbProperty("type")
    private String type;
    
    @JsonbProperty("text")
    private String text;
    
    /**
     * Default constructor for JSON-B deserialization.
     */
    public ContentItem() {
    }
    
    /**
     * Creates a content item with type and text.
     * 
     * @param type the content type (e.g., "text")
     * @param text the content text
     */
    public ContentItem(String type, String text) {
        this.type = type;
        this.text = text;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContentItem that = (ContentItem) o;
        return Objects.equals(type, that.type) &&
               Objects.equals(text, that.text);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(type, text);
    }
    
    @Override
    public String toString() {
        return "ContentItem{" +
                "type='" + type + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
