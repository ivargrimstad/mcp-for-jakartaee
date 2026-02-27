package dukes.mcp.model;

import jakarta.json.bind.annotation.JsonbProperty;
import java.util.Objects;

/**
 * Represents the parameters for the resources/read method.
 * 
 * Contains the URI of the resource to read.
 */
public class ResourceReadParams {
    
    @JsonbProperty("uri")
    private String uri;
    
    /**
     * Default constructor for JSON-B deserialization.
     */
    public ResourceReadParams() {
    }
    
    /**
     * Creates resource read parameters with URI.
     * 
     * @param uri the resource URI
     */
    public ResourceReadParams(String uri) {
        this.uri = uri;
    }
    
    public String getUri() {
        return uri;
    }
    
    public void setUri(String uri) {
        this.uri = uri;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceReadParams that = (ResourceReadParams) o;
        return Objects.equals(uri, that.uri);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(uri);
    }
    
    @Override
    public String toString() {
        return "ResourceReadParams{" +
                "uri='" + uri + '\'' +
                '}';
    }
}
