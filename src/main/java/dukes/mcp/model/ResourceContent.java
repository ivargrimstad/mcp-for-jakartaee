package dukes.mcp.model;

import jakarta.json.bind.annotation.JsonbProperty;
import java.util.List;
import java.util.Objects;

/**
 * Represents the content of a resource.
 * 
 * Contains the resource URI, MIME type, and content items.
 */
public class ResourceContent {
    
    @JsonbProperty("uri")
    private String uri;
    
    @JsonbProperty("mimeType")
    private String mimeType;
    
    @JsonbProperty("contents")
    private List<ContentItem> contents;
    
    /**
     * Default constructor for JSON-B deserialization.
     */
    public ResourceContent() {
    }
    
    /**
     * Creates resource content with URI, MIME type, and contents.
     * 
     * @param uri the resource URI
     * @param mimeType the resource MIME type
     * @param contents the resource content items
     */
    public ResourceContent(String uri, String mimeType, List<ContentItem> contents) {
        this.uri = uri;
        this.mimeType = mimeType;
        this.contents = contents;
    }
    
    /**
     * Creates resource content with URI, MIME type, and text content.
     * 
     * @param uri the resource URI
     * @param mimeType the resource MIME type
     * @param text the resource text content
     * @return resource content with text
     */
    public static ResourceContent text(String uri, String mimeType, String text) {
        return new ResourceContent(uri, mimeType, List.of(new ContentItem("text", text)));
    }
    
    public String getUri() {
        return uri;
    }
    
    public void setUri(String uri) {
        this.uri = uri;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    
    public List<ContentItem> getContents() {
        return contents;
    }
    
    public void setContents(List<ContentItem> contents) {
        this.contents = contents;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceContent that = (ResourceContent) o;
        return Objects.equals(uri, that.uri) &&
               Objects.equals(mimeType, that.mimeType) &&
               Objects.equals(contents, that.contents);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(uri, mimeType, contents);
    }
    
    @Override
    public String toString() {
        return "ResourceContent{" +
                "uri='" + uri + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", contents=" + contents +
                '}';
    }
}
