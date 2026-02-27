package dukes.mcp.model;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for initialization-related MCP data models.
 * Tests serialization, deserialization, and validation.
 */
class InitializeModelsTest {
    
    private Jsonb jsonb;
    
    @BeforeEach
    void setUp() {
        jsonb = JsonbBuilder.create();
    }
    
    @Test
    void testClientCapabilitiesSerialization() {
        ClientCapabilities capabilities = new ClientCapabilities();
        capabilities.setExperimental(null);
        
        String json = jsonb.toJson(capabilities);
        assertNotNull(json);
        
        ClientCapabilities deserialized = jsonb.fromJson(json, ClientCapabilities.class);
        assertEquals(capabilities, deserialized);
    }
    
    @Test
    void testClientInfoSerialization() {
        ClientInfo clientInfo = new ClientInfo("TestClient", "1.0.0");
        
        String json = jsonb.toJson(clientInfo);
        assertTrue(json.contains("TestClient"));
        assertTrue(json.contains("1.0.0"));
        
        ClientInfo deserialized = jsonb.fromJson(json, ClientInfo.class);
        assertEquals(clientInfo, deserialized);
        assertEquals("TestClient", deserialized.getName());
        assertEquals("1.0.0", deserialized.getVersion());
    }
    
    @Test
    void testInitializeParamsSerialization() {
        ClientCapabilities capabilities = new ClientCapabilities();
        ClientInfo clientInfo = new ClientInfo("TestClient", "1.0.0");
        InitializeParams params = new InitializeParams("2024-11-05", capabilities);
        params.setClientInfo(clientInfo);
        
        String json = jsonb.toJson(params);
        assertTrue(json.contains("2024-11-05"));
        assertTrue(json.contains("TestClient"));
        
        InitializeParams deserialized = jsonb.fromJson(json, InitializeParams.class);
        assertEquals(params, deserialized);
        assertEquals("2024-11-05", deserialized.getProtocolVersion());
        assertNotNull(deserialized.getClientInfo());
    }
    
    @Test
    void testServerCapabilitiesSerialization() {
        ServerCapabilities capabilities = ServerCapabilities.allEnabled();
        
        String json = jsonb.toJson(capabilities);
        assertNotNull(json);
        
        ServerCapabilities deserialized = jsonb.fromJson(json, ServerCapabilities.class);
        assertEquals(capabilities, deserialized);
        assertNotNull(deserialized.getTools());
        assertNotNull(deserialized.getResources());
        assertNotNull(deserialized.getPrompts());
    }
    
    @Test
    void testServerInfoSerialization() {
        ServerInfo serverInfo = new ServerInfo("MCPServer", "1.0.0");
        
        String json = jsonb.toJson(serverInfo);
        assertTrue(json.contains("MCPServer"));
        assertTrue(json.contains("1.0.0"));
        
        ServerInfo deserialized = jsonb.fromJson(json, ServerInfo.class);
        assertEquals(serverInfo, deserialized);
    }
    
    @Test
    void testInitializeResultSerialization() {
        ServerCapabilities capabilities = ServerCapabilities.allEnabled();
        ServerInfo serverInfo = new ServerInfo("MCPServer", "1.0.0");
        InitializeResult result = new InitializeResult("2024-11-05", capabilities, serverInfo);
        
        String json = jsonb.toJson(result);
        assertTrue(json.contains("2024-11-05"));
        assertTrue(json.contains("MCPServer"));
        
        InitializeResult deserialized = jsonb.fromJson(json, InitializeResult.class);
        assertEquals(result, deserialized);
        assertEquals("2024-11-05", deserialized.getProtocolVersion());
        assertNotNull(deserialized.getCapabilities());
        assertNotNull(deserialized.getServerInfo());
    }
    
    @Test
    void testCapabilityClassesEquality() {
        ToolsCapability tools1 = new ToolsCapability();
        ToolsCapability tools2 = new ToolsCapability();
        assertEquals(tools1, tools2);
        
        ResourcesCapability resources1 = new ResourcesCapability();
        ResourcesCapability resources2 = new ResourcesCapability();
        assertEquals(resources1, resources2);
        
        PromptsCapability prompts1 = new PromptsCapability();
        PromptsCapability prompts2 = new PromptsCapability();
        assertEquals(prompts1, prompts2);
    }
}
