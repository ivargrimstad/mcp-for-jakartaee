package dukes.mcp.model;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for tool-related MCP data models.
 * Tests serialization, deserialization, and validation.
 */
class ToolModelsTest {
    
    private Jsonb jsonb;
    
    @BeforeEach
    void setUp() {
        jsonb = JsonbBuilder.create();
    }
    
    @Test
    void testToolDefinitionSerialization() {
        Map<String, Object> schema = Map.of(
            "type", "object",
            "properties", Map.of("query", Map.of("type", "string"))
        );
        ToolDefinition tool = new ToolDefinition("test_tool", "A test tool", schema);
        
        String json = jsonb.toJson(tool);
        assertTrue(json.contains("test_tool"));
        assertTrue(json.contains("A test tool"));
        
        ToolDefinition deserialized = jsonb.fromJson(json, ToolDefinition.class);
        assertEquals(tool.getName(), deserialized.getName());
        assertEquals(tool.getDescription(), deserialized.getDescription());
        assertNotNull(deserialized.getInputSchema());
    }
    
    @Test
    void testToolCallParamsSerialization() {
        Map<String, Object> arguments = Map.of("query", "SELECT * FROM users");
        ToolCallParams params = new ToolCallParams("database_query", arguments);
        
        String json = jsonb.toJson(params);
        assertTrue(json.contains("database_query"));
        assertTrue(json.contains("SELECT"));
        
        ToolCallParams deserialized = jsonb.fromJson(json, ToolCallParams.class);
        assertEquals(params, deserialized);
        assertEquals("database_query", deserialized.getName());
        assertNotNull(deserialized.getArguments());
    }
    
    @Test
    void testContentItemSerialization() {
        ContentItem item = new ContentItem("text", "Hello, world!");
        
        String json = jsonb.toJson(item);
        assertTrue(json.contains("text"));
        assertTrue(json.contains("Hello, world!"));
        
        ContentItem deserialized = jsonb.fromJson(json, ContentItem.class);
        assertEquals(item, deserialized);
        assertEquals("text", deserialized.getType());
        assertEquals("Hello, world!", deserialized.getText());
    }
    
    @Test
    void testToolResultSuccessSerialization() {
        ToolResult result = ToolResult.success("Operation completed");
        
        String json = jsonb.toJson(result);
        assertTrue(json.contains("Operation completed"));
        
        ToolResult deserialized = jsonb.fromJson(json, ToolResult.class);
        assertNotNull(deserialized.getContent());
        assertFalse(deserialized.getIsError());
        assertEquals(1, deserialized.getContent().size());
    }
    
    @Test
    void testToolResultErrorSerialization() {
        ToolResult result = ToolResult.error("Tool execution failed");
        
        String json = jsonb.toJson(result);
        assertTrue(json.contains("Tool execution failed"));
        
        ToolResult deserialized = jsonb.fromJson(json, ToolResult.class);
        assertNotNull(deserialized.getContent());
        assertTrue(deserialized.getIsError());
    }
    
    @Test
    void testToolResultWithMultipleContentItems() {
        List<ContentItem> content = List.of(
            new ContentItem("text", "First item"),
            new ContentItem("text", "Second item")
        );
        ToolResult result = new ToolResult(content, false);
        
        String json = jsonb.toJson(result);
        ToolResult deserialized = jsonb.fromJson(json, ToolResult.class);
        
        assertEquals(2, deserialized.getContent().size());
        assertFalse(deserialized.getIsError());
    }
    
    @Test
    void testToolListResultSerialization() {
        Map<String, Object> schema = Map.of("type", "object");
        List<ToolDefinition> tools = List.of(
            new ToolDefinition("tool1", "First tool", schema),
            new ToolDefinition("tool2", "Second tool", schema)
        );
        ToolListResult result = new ToolListResult(tools);
        
        String json = jsonb.toJson(result);
        assertTrue(json.contains("tool1"));
        assertTrue(json.contains("tool2"));
        
        ToolListResult deserialized = jsonb.fromJson(json, ToolListResult.class);
        assertEquals(2, deserialized.getTools().size());
    }
    
    @Test
    void testToolListResultEmpty() {
        ToolListResult result = new ToolListResult(List.of());
        
        String json = jsonb.toJson(result);
        ToolListResult deserialized = jsonb.fromJson(json, ToolListResult.class);
        
        assertNotNull(deserialized.getTools());
        assertTrue(deserialized.getTools().isEmpty());
    }
}
