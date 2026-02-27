package dukes.mcp.model;

import jakarta.json.bind.annotation.JsonbProperty;
import java.util.Objects;

/**
 * Represents information about the MCP server.
 */
public class ServerInfo {
    
    @JsonbProperty("name")
    private String name;
    
    @JsonbProperty("version")
    private String version;
    
    /**
     * Default constructor for JSON-B deserialization.
     */
    public ServerInfo() {
    }
    
    /**
     * Creates server info with name and version.
     * 
     * @param name the server name
     * @param version the server version
     */
    public ServerInfo(String name, String version) {
        this.name = name;
        this.version = version;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerInfo that = (ServerInfo) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(version, that.version);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, version);
    }
    
    @Override
    public String toString() {
        return "ServerInfo{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
