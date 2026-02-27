package dukes.mcp.model;

import jakarta.json.bind.annotation.JsonbProperty;
import java.util.Objects;

/**
 * Represents the capabilities supported by the MCP server.
 * 
 * Capabilities indicate which MCP features are available, such as
 * tools, resources, and prompts.
 */
public class ServerCapabilities {
    
    @JsonbProperty("tools")
    private ToolsCapability tools;
    
    @JsonbProperty("resources")
    private ResourcesCapability resources;
    
    @JsonbProperty("prompts")
    private PromptsCapability prompts;
    
    /**
     * Default constructor for JSON-B deserialization.
     */
    public ServerCapabilities() {
    }
    
    /**
     * Creates server capabilities with all features enabled.
     * 
     * @return server capabilities with tools, resources, and prompts enabled
     */
    public static ServerCapabilities allEnabled() {
        ServerCapabilities capabilities = new ServerCapabilities();
        capabilities.setTools(new ToolsCapability());
        capabilities.setResources(new ResourcesCapability());
        capabilities.setPrompts(new PromptsCapability());
        return capabilities;
    }
    
    public ToolsCapability getTools() {
        return tools;
    }
    
    public void setTools(ToolsCapability tools) {
        this.tools = tools;
    }
    
    public ResourcesCapability getResources() {
        return resources;
    }
    
    public void setResources(ResourcesCapability resources) {
        this.resources = resources;
    }
    
    public PromptsCapability getPrompts() {
        return prompts;
    }
    
    public void setPrompts(PromptsCapability prompts) {
        this.prompts = prompts;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerCapabilities that = (ServerCapabilities) o;
        return Objects.equals(tools, that.tools) &&
               Objects.equals(resources, that.resources) &&
               Objects.equals(prompts, that.prompts);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(tools, resources, prompts);
    }
    
    @Override
    public String toString() {
        return "ServerCapabilities{" +
                "tools=" + tools +
                ", resources=" + resources +
                ", prompts=" + prompts +
                '}';
    }
}
