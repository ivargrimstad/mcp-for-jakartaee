package dukes.mcp.model;

import jakarta.json.bind.annotation.JsonbProperty;
import java.util.Objects;

/**
 * Represents a resource definition in the MCP protocol.
 * 
 * A resource definition describes an accessible data entity with its
 * URI, name, description, and MIME type.
 */
public class ResourceDefinition {
    
    @JsonbProperty("uri")
    private String uri;
    
    @JsonbProperty("name")
    private String name;
    
    @JsonbProperty("description")
    private String description;
    
    @JsonbProperty("mimeType")
    private String mimeType;
    
    /**
     * Default constructor for JSON-B deserialization.
     */
    public ResourceDefinition() {
    }
    
    /**
     * Creates a resource definition with URI, name, description, and MIME type.
     * 
     * @param uri the resource URI
     * @param name the resource name
     * @param description the resource description
     * @param mimeType the resource MIME type
     */
    public ResourceDefinition(String uri, String name, String description, String mimeType) {
        this.uri = uri;
        this.name = name;
        this.description = description;
        this.mimeType = mimeType;
    }
    
    public String getUri() {
        return uri;
    }
    
    public void setUri(String uri) {
        this.uri = uri;
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
    
    public String getMimeType() {
        return mimeType;
    }
    
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceDefinition that = (ResourceDefinition) o;
        return Objects.equals(uri, that.uri) &&
               Objects.equals(name, that.name) &&
               Objects.equals(description, that.description) &&
               Objects.equals(mimeType, that.mimeType);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(uri, name, description, mimeType);
    }
    
    @Override
    public String toString() {
        return "ResourceDefinition{" +
                "uri='" + uri + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", mimeType='" + mimeType + '\'' +
                '}';
    }
}
