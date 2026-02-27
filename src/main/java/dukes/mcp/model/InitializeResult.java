package dukes.mcp.model;

import jakarta.json.bind.annotation.JsonbProperty;
import java.util.Objects;

/**
 * Represents the result of the initialize method.
 * 
 * The initialize result contains the server's protocol version,
 * capabilities, and server information.
 */
public class InitializeResult {
    
    @JsonbProperty("protocolVersion")
    private String protocolVersion;
    
    @JsonbProperty("capabilities")
    private ServerCapabilities capabilities;
    
    @JsonbProperty("serverInfo")
    private ServerInfo serverInfo;
    
    /**
     * Default constructor for JSON-B deserialization.
     */
    public InitializeResult() {
    }
    
    /**
     * Creates initialize result with protocol version and capabilities.
     * 
     * @param protocolVersion the protocol version
     * @param capabilities the server capabilities
     */
    public InitializeResult(String protocolVersion, ServerCapabilities capabilities) {
        this.protocolVersion = protocolVersion;
        this.capabilities = capabilities;
    }
    
    /**
     * Creates initialize result with protocol version, capabilities, and server info.
     * 
     * @param protocolVersion the protocol version
     * @param capabilities the server capabilities
     * @param serverInfo the server information
     */
    public InitializeResult(String protocolVersion, ServerCapabilities capabilities, ServerInfo serverInfo) {
        this.protocolVersion = protocolVersion;
        this.capabilities = capabilities;
        this.serverInfo = serverInfo;
    }
    
    public String getProtocolVersion() {
        return protocolVersion;
    }
    
    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }
    
    public ServerCapabilities getCapabilities() {
        return capabilities;
    }
    
    public void setCapabilities(ServerCapabilities capabilities) {
        this.capabilities = capabilities;
    }
    
    public ServerInfo getServerInfo() {
        return serverInfo;
    }
    
    public void setServerInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InitializeResult that = (InitializeResult) o;
        return Objects.equals(protocolVersion, that.protocolVersion) &&
               Objects.equals(capabilities, that.capabilities) &&
               Objects.equals(serverInfo, that.serverInfo);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(protocolVersion, capabilities, serverInfo);
    }
    
    @Override
    public String toString() {
        return "InitializeResult{" +
                "protocolVersion='" + protocolVersion + '\'' +
                ", capabilities=" + capabilities +
                ", serverInfo=" + serverInfo +
                '}';
    }
}
