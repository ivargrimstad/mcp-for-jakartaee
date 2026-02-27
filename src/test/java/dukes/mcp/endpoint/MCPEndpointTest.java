package dukes.mcp.endpoint;

import dukes.mcp.model.JsonRpcRequest;
import dukes.mcp.model.JsonRpcResponse;
import dukes.mcp.service.MCPProtocolHandler;
import dukes.mcp.service.PromptManager;
import dukes.mcp.service.ResourceManager;
import dukes.mcp.service.ToolRegistry;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MCPEndpoint.
 */
class MCPEndpointTest {
    
    private MCPProtocolHandler protocolHandler;
    private MCPEndpoint endpoint;
    
    @BeforeEach
    void setUp() {
        // Create real instances
        ToolRegistry toolRegistry = new ToolRegistry();
        ResourceManager resourceManager = new ResourceManager();
        PromptManager promptManager = new PromptManager();
        protocolHandler = new MCPProtocolHandler();
        
        // Inject dependencies into protocol handler
        injectField(protocolHandler, "toolRegistry", toolRegistry);
        injectField(protocolHandler, "resourceManager", resourceManager);
        injectField(protocolHandler, "promptManager", promptManager);
        
        // Create endpoint
        endpoint = new MCPEndpoint();
        injectField(endpoint, "protocolHandler", protocolHandler);
    }
    
    private void injectField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            fail("Failed to inject field " + fieldName + ": " + e.getMessage());
        }
    }
    
    @Test
    void testHandleRequest_Success() {
        // Given - initialize first
        JsonRpcRequest initRequest = new JsonRpcRequest("initialize", 
            java.util.Map.of("protocolVersion", "1.0.0", "clientInfo", java.util.Map.of("name", "test")), 
            "0");
        endpoint.handleRequest(initRequest);
        
        // Now test tools/list
        JsonRpcRequest request = new JsonRpcRequest("tools/list", null, "1");
        
        // When
        Response response = endpoint.handleRequest(request);
        
        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        
        JsonRpcResponse jsonRpcResponse = (JsonRpcResponse) response.getEntity();
        assertNotNull(jsonRpcResponse);
        assertFalse(jsonRpcResponse.isError());
        assertEquals("1", jsonRpcResponse.getId());
    }
    
    @Test
    void testHandleRequest_Error() {
        // Given - initialize first
        JsonRpcRequest initRequest = new JsonRpcRequest("initialize", 
            java.util.Map.of("protocolVersion", "1.0.0", "clientInfo", java.util.Map.of("name", "test")), 
            "0");
        endpoint.handleRequest(initRequest);
        
        // Now test unknown method
        JsonRpcRequest request = new JsonRpcRequest("unknown/method", null, "2");
        
        // When
        Response response = endpoint.handleRequest(request);
        
        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        
        JsonRpcResponse jsonRpcResponse = (JsonRpcResponse) response.getEntity();
        assertNotNull(jsonRpcResponse);
        assertTrue(jsonRpcResponse.isError());
        assertEquals("2", jsonRpcResponse.getId());
        assertEquals(-32601, jsonRpcResponse.getError().getCode());
    }
    
    @Test
    void testHandleRequest_NullRequest() {
        // Given
        JsonRpcRequest request = null;
        
        // When
        Response response = endpoint.handleRequest(request);
        
        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        
        JsonRpcResponse jsonRpcResponse = (JsonRpcResponse) response.getEntity();
        assertNotNull(jsonRpcResponse);
        assertTrue(jsonRpcResponse.isError());
        // Null request causes internal error (-32603) not invalid request (-32600)
        assertEquals(-32603, jsonRpcResponse.getError().getCode());
    }
    
    @Test
    void testHandleRequest_AlwaysReturnsHttp200() {
        // Given - various scenarios
        JsonRpcRequest[] requests = {
            new JsonRpcRequest("initialize", 
                java.util.Map.of("protocolVersion", "1.0.0", "clientInfo", java.util.Map.of("name", "test")), 
                "1"),
            new JsonRpcRequest("invalid", null, "2"),
            null
        };
        
        // When & Then - all should return HTTP 200
        for (JsonRpcRequest request : requests) {
            Response response = endpoint.handleRequest(request);
            assertEquals(200, response.getStatus(), 
                "All responses should have HTTP 200 status per JSON-RPC 2.0 spec");
        }
    }
}
