package dukes.mcp.model;

import jakarta.json.bind.annotation.JsonbProperty;
import java.util.List;
import java.util.Objects;

/**
 * Represents the result of a tool execution.
 * 
 * Contains the tool output content or error information if execution failed.
 */
public class ToolResult {
    
    @JsonbProperty("content")
    private List<ContentItem> content;
    
    @JsonbProperty("isError")
    private Boolean isError;
    
    /**
     * Default constructor for JSON-B deserialization.
     */
    public ToolResult() {
    }
    
    /**
     * Creates a successful tool result with content.
     * 
     * @param content the tool output content
     */
    public ToolResult(List<ContentItem> content) {
        this.content = content;
        this.isError = false;
    }
    
    /**
     * Creates a tool result with content and error flag.
     * 
     * @param content the tool output or error content
     * @param isError whether this is an error result
     */
    public ToolResult(List<ContentItem> content, boolean isError) {
        this.content = content;
        this.isError = isError;
    }
    
    /**
     * Creates a successful tool result with text content.
     * 
     * @param text the tool output text
     * @return a success tool result
     */
    public static ToolResult success(String text) {
        return new ToolResult(List.of(new ContentItem("text", text)), false);
    }
    
    /**
     * Creates an error tool result with error message.
     * 
     * @param errorMessage the error message
     * @return an error tool result
     */
    public static ToolResult error(String errorMessage) {
        return new ToolResult(List.of(new ContentItem("text", errorMessage)), true);
    }
    
    public List<ContentItem> getContent() {
        return content;
    }
    
    public void setContent(List<ContentItem> content) {
        this.content = content;
    }
    
    public Boolean getIsError() {
        return isError;
    }
    
    public void setIsError(Boolean isError) {
        this.isError = isError;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ToolResult that = (ToolResult) o;
        return Objects.equals(content, that.content) &&
               Objects.equals(isError, that.isError);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(content, isError);
    }
    
    @Override
    public String toString() {
        return "ToolResult{" +
                "content=" + content +
                ", isError=" + isError +
                '}';
    }
}
