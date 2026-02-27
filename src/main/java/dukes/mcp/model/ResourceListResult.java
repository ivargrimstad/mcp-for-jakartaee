package dukes.mcp.model;

import jakarta.json.bind.annotation.JsonbProperty;
import java.util.List;
import java.util.Objects;

/**
 * Represents the result of the resources/list method.
 * 
 * Contains a list of all available resource definitions.
 */
public class ResourceListResult {
    
    @JsonbProperty("resources")
    private List<ResourceDefinition> resources;
    
    /**
     * Default constructor for JSON-B deserialization.
     */
    public ResourceListResult() {
    }
    
    /**
     * Creates a resource list result with resources.
     * 
     * @param resources the list of resource definitions
     */
    public ResourceListResult(List<ResourceDefinition> resources) {
        this.resources = resources;
    }
    
    public List<ResourceDefinition> getResources() {
        return resources;
    }
    
    public void setResources(List<ResourceDefinition> resources) {
        this.resources = resources;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceListResult that = (ResourceListResult) o;
        return Objects.equals(resources, that.resources);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(resources);
    }
    
    @Override
    public String toString() {
        return "ResourceListResult{" +
                "resources=" + resources +
                '}';
    }
}
