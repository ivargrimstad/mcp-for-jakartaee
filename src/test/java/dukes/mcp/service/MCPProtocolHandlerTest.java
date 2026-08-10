package dukes.mcp.service;

import dukes.mcp.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MCPProtocolHandler.
 * 
 * Note: These tests use real service instances instead of mocks to avoid
 * Mockito compatibility issues with Java 25.
 */
class MCPProtocolHandlerTest {
    
    private ToolRegistry toolRegistry;
    private ResourceManager resourceManager;
    private PromptManager promptManager;
    private MCPProtocolHandler handler;
    
    @BeforeEach
    void setUp() {
        // Create real instances instead of mocks
        toolRegistry = new ToolRegistry();
        resourceManager = new ResourceManager();
        promptManager = new PromptManager();
        handler = new MCPProtocolHandler();
        
        // Inject dependencies using reflection
        injectField(handler, "toolRegistry", toolRegistry);
        injectField(handler, "resourceManager", resourceManager);
        injectField(handler, "promptManager", promptManager);
    }
    
    /**
     * Injects a field value using reflection.
     */
    private void injectField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            fail("Failed to inject field " + fieldName + ": " + e.getMessage());
        }
    }
    
    @Test
    void testProcessRequest_Initialize_Success() {
        // Given
        InitializeParams params = new InitializeParams("2024-11-05", new ClientCapabilities());
        JsonRpcRequest request = new JsonRpcRequest("initialize", params, "1");
        
        // When
        JsonRpcResponse response = handler.processRequest(request);
        
        // Then
        assertNotNull(response);
        assertEquals("1", response.getId());
        assertTrue(response.isSuccess());
        assertNotNull(response.getResult());
        assertTrue(response.getResult() instanceof InitializeResult);
        
        InitializeResult result = (InitializeResult) response.getResult();
        assertEquals("2024-11-05", result.getProtocolVersion());
        assertNotNull(result.getCapabilities());
        assertNotNull(result.getServerInfo());
    }
    
    @Test
    void testProcessRequest_ToolsList_Success() {
        // Given
        // First initialize
        handler.processRequest(new JsonRpcRequest("initialize", 
                new InitializeParams("2024-11-05", new ClientCapabilities()), "0"));
        
        // Register a test tool
        Tool testTool = new Tool() {
            @Override
            public String getName() { return "test_tool"; }
            @Override
            public String getDescription() { return "Test tool"; }
            @Override
            public Map<String, Object> getInputSchema() { return Map.of(); }
            @Override
            public ToolResult execute(Map<String, Object> arguments) {
                return ToolResult.success("result");
            }
        };
        toolRegistry.registerTool(testTool);
        
        JsonRpcRequest request = new JsonRpcRequest("tools/list", null, "1");
        
        // When
        JsonRpcResponse response = handler.processRequest(request);
        
        // Then
        assertNotNull(response);
        assertEquals("1", response.getId());
        assertTrue(response.isSuccess());
        assertTrue(response.getResult() instanceof ToolListResult);
        
        ToolListResult result = (ToolListResult) response.getResult();
        assertEquals(1, result.getTools().size());
        assertEquals("test_tool", result.getTools().get(0).getName());
    }
    
    @Test
    void testProcessRequest_ToolsCall_Success() {
        // Given
        // First initialize
        handler.processRequest(new JsonRpcRequest("initialize", 
                new InitializeParams("2024-11-05", new ClientCapabilities()), "0"));
        
        // Register a test tool
        Tool testTool = new Tool() {
            @Override
            public String getName() { return "test_tool"; }
            @Override
            public String getDescription() { return "Test tool"; }
            @Override
            public Map<String, Object> getInputSchema() { return Map.of(); }
            @Override
            public ToolResult execute(Map<String, Object> arguments) {
                return ToolResult.success("result");
            }
        };
        toolRegistry.registerTool(testTool);
        
        ToolCallParams params = new ToolCallParams("test_tool", Map.of("arg1", "value1"));
        JsonRpcRequest request = new JsonRpcRequest("tools/call", params, "1");
        
        // When
        JsonRpcResponse response = handler.processRequest(request);
        
        // Then
        assertNotNull(response);
        assertEquals("1", response.getId());
        assertTrue(response.isSuccess());
        assertTrue(response.getResult() instanceof ToolResult);
        
        ToolResult result = (ToolResult) response.getResult();
        assertFalse(result.getIsError());
    }
    
    @Test
    void testProcessRequest_ResourcesList_Success() {
        // Given
        // First initialize
        handler.processRequest(new JsonRpcRequest("initialize", 
                new InitializeParams("2024-11-05", new ClientCapabilities()), "0"));
        
        // Register a test resource
        Resource testResource = new Resource() {
            @Override
            public String getUri() { return "test://resource"; }
            @Override
            public String getName() { return "Test Resource"; }
            @Override
            public String getDescription() { return "Test"; }
            @Override
            public String getMimeType() { return "text/plain"; }
            @Override
            public String read() { return "content"; }
        };
        resourceManager.registerResource(testResource);
        
        JsonRpcRequest request = new JsonRpcRequest("resources/list", null, "1");
        
        // When
        JsonRpcResponse response = handler.processRequest(request);
        
        // Then
        assertNotNull(response);
        assertEquals("1", response.getId());
        assertTrue(response.isSuccess());
        assertTrue(response.getResult() instanceof ResourceListResult);
        
        ResourceListResult result = (ResourceListResult) response.getResult();
        assertEquals(1, result.getResources().size());
        assertEquals("test://resource", result.getResources().get(0).getUri());
    }
    
    @Test
    void testProcessRequest_ResourcesRead_Success() {
        // Given
        // First initialize
        handler.processRequest(new JsonRpcRequest("initialize", 
                new InitializeParams("2024-11-05", new ClientCapabilities()), "0"));
        
        // Register a test resource
        Resource testResource = new Resource() {
            @Override
            public String getUri() { return "test://resource"; }
            @Override
            public String getName() { return "Test Resource"; }
            @Override
            public String getDescription() { return "Test"; }
            @Override
            public String getMimeType() { return "text/plain"; }
            @Override
            public String read() { return "content"; }
        };
        resourceManager.registerResource(testResource);
        
        ResourceReadParams params = new ResourceReadParams("test://resource");
        JsonRpcRequest request = new JsonRpcRequest("resources/read", params, "1");
        
        // When
        JsonRpcResponse response = handler.processRequest(request);
        
        // Then
        assertNotNull(response);
        assertEquals("1", response.getId());
        assertTrue(response.isSuccess());
        assertTrue(response.getResult() instanceof ResourceReadResult);
        
        ResourceReadResult result = (ResourceReadResult) response.getResult();
        assertEquals(1, result.getContents().size());
        assertEquals("test://resource", result.getContents().get(0).getUri());
    }
    
    @Test
    void testProcessRequest_PromptsList_Success() {
        // Given
        // First initialize
        handler.processRequest(new JsonRpcRequest("initialize", 
                new InitializeParams("2024-11-05", new ClientCapabilities()), "0"));
        
        // Register a test prompt
        Prompt testPrompt = new Prompt() {
            @Override
            public String getName() { return "test_prompt"; }
            @Override
            public String getDescription() { return "Test Prompt"; }
            @Override
            public List<PromptArgument> getArguments() { return List.of(); }
            @Override
            public List<PromptMessage> render(Map<String, String> arguments) {
                return List.of(
                    new PromptMessage("user", new ContentItem("text", "prompt"))
                );
            }
        };
        promptManager.registerPrompt(testPrompt);
        
        JsonRpcRequest request = new JsonRpcRequest("prompts/list", null, "1");
        
        // When
        JsonRpcResponse response = handler.processRequest(request);
        
        // Then
        assertNotNull(response);
        assertEquals("1", response.getId());
        assertTrue(response.isSuccess());
        assertTrue(response.getResult() instanceof PromptListResult);
        
        PromptListResult result = (PromptListResult) response.getResult();
        assertEquals(1, result.getPrompts().size());
        assertEquals("test_prompt", result.getPrompts().get(0).getName());
    }
    
    @Test
    void testProcessRequest_PromptsGet_Success() {
        // Given
        // First initialize
        handler.processRequest(new JsonRpcRequest("initialize", 
                new InitializeParams("2024-11-05", new ClientCapabilities()), "0"));
        
        // Register a test prompt
        Prompt testPrompt = new Prompt() {
            @Override
            public String getName() { return "test_prompt"; }
            @Override
            public String getDescription() { return "Test Prompt"; }
            @Override
            public List<PromptArgument> getArguments() { return List.of(); }
            @Override
            public List<PromptMessage> render(Map<String, String> arguments) {
                return List.of(
                    new PromptMessage("user", new ContentItem("text", "prompt content"))
                );
            }
        };
        promptManager.registerPrompt(testPrompt);
        
        PromptGetParams params = new PromptGetParams("test_prompt", Map.of("arg1", "value1"));
        JsonRpcRequest request = new JsonRpcRequest("prompts/get", params, "1");
        
        // When
        JsonRpcResponse response = handler.processRequest(request);
        
        // Then
        assertNotNull(response);
        assertEquals("1", response.getId());
        assertTrue(response.isSuccess());
        assertTrue(response.getResult() instanceof PromptGetResult);
        
        PromptGetResult result = (PromptGetResult) response.getResult();
        assertEquals(1, result.getMessages().size());
    }
    
    @Test
    void testProcessRequest_InvalidJsonRpc_ReturnsError() {
        // Given
        JsonRpcRequest request = new JsonRpcRequest("test", null, "1");
        request.setJsonrpc("1.0"); // Invalid version
        
        // When
        JsonRpcResponse response = handler.processRequest(request);
        
        // Then
        assertNotNull(response);
        assertEquals("1", response.getId());
        assertTrue(response.isError());
        assertEquals(-32600, response.getError().getCode());
    }
    
    @Test
    void testProcessRequest_MissingMethod_ReturnsError() {
        // Given
        JsonRpcRequest request = new JsonRpcRequest(null, null, "1");
        
        // When
        JsonRpcResponse response = handler.processRequest(request);
        
        // Then
        assertNotNull(response);
        assertEquals("1", response.getId());
        assertTrue(response.isError());
        assertEquals(-32600, response.getError().getCode());
    }
    
    @Test
    void testProcessRequest_UnknownMethod_ReturnsError() {
        // Given
        // First initialize
        handler.processRequest(new JsonRpcRequest("initialize", 
                new InitializeParams("2024-11-05", new ClientCapabilities()), "0"));
        
        JsonRpcRequest request = new JsonRpcRequest("unknown/method", null, "1");
        
        // When
        JsonRpcResponse response = handler.processRequest(request);
        
        // Then
        assertNotNull(response);
        assertEquals("1", response.getId());
        assertTrue(response.isError());
        assertEquals(-32601, response.getError().getCode());
        assertTrue(response.getError().getMessage().contains("Unknown method"));
    }
    
    @Test
    void testProcessRequest_NotInitialized_ReturnsError() {
        // Given
        JsonRpcRequest request = new JsonRpcRequest("tools/list", null, "1");
        
        // When
        JsonRpcResponse response = handler.processRequest(request);
        
        // Then
        assertNotNull(response);
        assertEquals("1", response.getId());
        assertTrue(response.isError());
        assertEquals(-32002, response.getError().getCode());
        assertTrue(response.getError().getMessage().contains("not initialized"));
    }
    
    @Test
    void testProcessRequest_ResourceNotFound_ReturnsError() {
        // Given
        // First initialize
        handler.processRequest(new JsonRpcRequest("initialize", 
                new InitializeParams("2024-11-05", new ClientCapabilities()), "0"));
        
        ResourceReadParams params = new ResourceReadParams("nonexistent://resource");
        JsonRpcRequest request = new JsonRpcRequest("resources/read", params, "1");
        
        // When
        JsonRpcResponse response = handler.processRequest(request);
        
        // Then
        assertNotNull(response);
        assertEquals("1", response.getId());
        assertTrue(response.isError());
        assertEquals(-32001, response.getError().getCode());
        assertTrue(response.getError().getMessage().contains("not found"));
    }
    
    @Test
    void testProcessRequest_PromptNotFound_ReturnsError() {
        // Given
        // First initialize
        handler.processRequest(new JsonRpcRequest("initialize", 
                new InitializeParams("2024-11-05", new ClientCapabilities()), "0"));
        
        PromptGetParams params = new PromptGetParams("nonexistent_prompt", Map.of());
        JsonRpcRequest request = new JsonRpcRequest("prompts/get", params, "1");
        
        // When
        JsonRpcResponse response = handler.processRequest(request);
        
        // Then
        assertNotNull(response);
        assertEquals("1", response.getId());
        assertTrue(response.isError());
        assertEquals(-32001, response.getError().getCode());
        assertTrue(response.getError().getMessage().contains("not found"));
    }
    
    @Test
    void testProcessRequest_InvalidPromptArguments_ReturnsError() {
        // Given
        // First initialize
        handler.processRequest(new JsonRpcRequest("initialize", 
                new InitializeParams("2024-11-05", new ClientCapabilities()), "0"));
        
        // Register a prompt that requires an argument
        Prompt testPrompt = new Prompt() {
            @Override
            public String getName() { return "test_prompt"; }
            @Override
            public String getDescription() { return "Test Prompt"; }
            @Override
            public List<PromptArgument> getArguments() {
                return List.of(new PromptArgument("required_arg", "Required argument", true));
            }
            @Override
            public List<PromptMessage> render(Map<String, String> arguments) {
                if (!arguments.containsKey("required_arg")) {
                    throw new PromptManager.PromptArgumentException("Missing required argument");
                }
                return List.of(
                    new PromptMessage("user", new ContentItem("text", "prompt"))
                );
            }
        };
        promptManager.registerPrompt(testPrompt);
        
        PromptGetParams params = new PromptGetParams("test_prompt", Map.of());
        JsonRpcRequest request = new JsonRpcRequest("prompts/get", params, "1");
        
        // When
        JsonRpcResponse response = handler.processRequest(request);
        
        // Then
        assertNotNull(response);
        assertEquals("1", response.getId());
        assertTrue(response.isError());
        assertEquals(-32602, response.getError().getCode());
        assertTrue(response.getError().getMessage().contains("Invalid prompt arguments"));
    }
    
    @Test
    void testHandleInitialize_SetsInitializedState() {
        // Given
        InitializeParams params = new InitializeParams("2024-11-05", new ClientCapabilities());
        
        // When
        InitializeResult result = handler.handleInitialize(params);
        
        // Then
        assertNotNull(result);
        assertEquals("2024-11-05", result.getProtocolVersion());
        assertNotNull(result.getCapabilities());
        assertNotNull(result.getServerInfo());
        assertEquals("MCP Server for Jakarta EE", result.getServerInfo().getName());
        assertEquals("1.0.0", result.getServerInfo().getVersion());
        
        // Verify that subsequent requests are accepted
        JsonRpcRequest request = new JsonRpcRequest("tools/list", null, "1");
        JsonRpcResponse response = handler.processRequest(request);
        assertTrue(response.isSuccess());
    }
    
    @Test
    void testHandleToolCall_WithNullArguments_UsesEmptyMap() {
        // Given
        Tool testTool = new Tool() {
            @Override
            public String getName() { return "test_tool"; }
            @Override
            public String getDescription() { return "Test tool"; }
            @Override
            public Map<String, Object> getInputSchema() { return Map.of(); }
            @Override
            public ToolResult execute(Map<String, Object> arguments) {
                return ToolResult.success("result");
            }
        };
        toolRegistry.registerTool(testTool);
        
        // When
        ToolResult result = handler.handleToolCall("test_tool", null);
        
        // Then
        assertNotNull(result);
        assertFalse(result.getIsError());
    }
    
    @Test
    void testHandlePromptGet_WithNullArguments_UsesEmptyMap() {
        // Given
        Prompt testPrompt = new Prompt() {
            @Override
            public String getName() { return "test_prompt"; }
            @Override
            public String getDescription() { return "Test Prompt"; }
            @Override
            public List<PromptArgument> getArguments() { return List.of(); }
            @Override
            public List<PromptMessage> render(Map<String, String> arguments) {
                return List.of(
                    new PromptMessage("user", new ContentItem("text", "prompt"))
                );
            }
        };
        promptManager.registerPrompt(testPrompt);
        
        // When
        PromptGetResult result = handler.handlePromptGet("test_prompt", null);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getMessages().size());
    }
}
