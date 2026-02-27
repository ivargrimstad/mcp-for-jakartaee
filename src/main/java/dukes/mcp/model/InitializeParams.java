package dukes.mcp.model;

import jakarta.json.bind.annotation.JsonbProperty;
import java.util.Objects;

/**
 * Represents the parameters for the initialize method.
 * 
 * The initialize request is sent by the client to negotiate protocol
 * version and capabilities with the server.
 */
public class InitializeParams {
    
    @JsonbProperty("protocolVersion")
    private String protocolVersion;
    
    @JsonbProperty("capabilities")
    private ClientCapabilities capabilities;
    
    @JsonbProperty("clientInfo")
    private ClientInfo clientInfo;
    
    /**
     * Default constructor for JSON-B deserialization.
     */
    public InitializeParams() {
    }
    
    /**
     * Creates initialize parameters with protocol version and capabilities.
     * 
     * @param protocolVersion the protocol version
     * @param capabilities the client capabilities
     */
    public InitializeParams(String protocolVersion, ClientCapabilities capabilities) {
        this.protocolVersion = protocolVersion;
        this.capabilities = capabilities;
    }
    
    public String getProtocolVersion() {
        return protocolVersion;
    }
    
    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }
    
    public ClientCapabilities getCapabilities() {
        return capabilities;
    }
    
    public void setCapabilities(ClientCapabilities capabilities) {
        this.capabilities = capabilities;
    }
    
    public ClientInfo getClientInfo() {
        return clientInfo;
    }
    
    public void setClientInfo(ClientInfo clientInfo) {
        this.clientInfo = clientInfo;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InitializeParams that = (InitializeParams) o;
        return Objects.equals(protocolVersion, that.protocolVersion) &&
               Objects.equals(capabilities, that.capabilities) &&
               Objects.equals(clientInfo, that.clientInfo);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(protocolVersion, capabilities, clientInfo);
    }
    
    @Override
    public String toString() {
        return "InitializeParams{" +
                "protocolVersion='" + protocolVersion + '\'' +
                ", capabilities=" + capabilities +
                ", clientInfo=" + clientInfo +
                '}';
    }
}
