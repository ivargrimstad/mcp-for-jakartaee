package dukes.mcp.model;

import jakarta.json.bind.annotation.JsonbProperty;
import java.util.List;
import java.util.Objects;

/**
 * Represents the result of the resources/read method.
 * 
 * Contains a list of resource contents.
 */
public class ResourceReadResult {
    
    @JsonbProperty("contents")
    private List<ResourceContent> contents;
    
    /**
     * Default constructor for JSON-B deserialization.
     */
    public ResourceReadResult() {
    }
    
    /**
     * Creates a resource read result with contents.
     * 
     * @param contents the list of resource contents
     */
    public ResourceReadResult(List<ResourceContent> contents) {
        this.contents = contents;
    }
    
    /**
     * Creates a resource read result with a single resource content.
     * 
     * @param content the resource content
     * @return resource read result
     */
    public static ResourceReadResult of(ResourceContent content) {
        return new ResourceReadResult(List.of(content));
    }
    
    public List<ResourceContent> getContents() {
        return contents;
    }
    
    public void setContents(List<ResourceContent> contents) {
        this.contents = contents;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceReadResult that = (ResourceReadResult) o;
        return Objects.equals(contents, that.contents);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(contents);
    }
    
    @Override
    public String toString() {
        return "ResourceReadResult{" +
                "contents=" + contents +
                '}';
    }
}
