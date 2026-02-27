package dukes.mcp.model;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for resource-related MCP data models.
 * Tests serialization, deserialization, and validation.
 */
class ResourceModelsTest {
    
    private Jsonb jsonb;
    
    @BeforeEach
    void setUp() {
        jsonb = JsonbBuilder.create();
    }
    
    @Test
    void testResourceDefinitionSerialization() {
        ResourceDefinition resource = new ResourceDefinition(
            "config://app.properties",
            "Application Config",
            "Main application configuration",
            "text/plain"
        );
        
        String json = jsonb.toJson(resource);
        assertTrue(json.contains("config://app.properties"));
        assertTrue(json.contains("Application Config"));
        assertTrue(json.contains("text/plain"));
        
        ResourceDefinition deserialized = jsonb.fromJson(json, ResourceDefinition.class);
        assertEquals(resource, deserialized);
        assertEquals("config://app.properties", deserialized.getUri());
        assertEquals("text/plain", deserialized.getMimeType());
    }
    
    @Test
    void testResourceReadParamsSerialization() {
        ResourceReadParams params = new ResourceReadParams("config://app.properties");
        
        String json = jsonb.toJson(params);
        assertTrue(json.contains("config://app.properties"));
        
        ResourceReadParams deserialized = jsonb.fromJson(json, ResourceReadParams.class);
        assertEquals(params, deserialized);
        assertEquals("config://app.properties", deserialized.getUri());
    }
    
    @Test
    void testResourceContentSerialization() {
        List<ContentItem> contents = List.of(
            new ContentItem("text", "app.name=MyApp\napp.version=1.0")
        );
        ResourceContent content = new ResourceContent(
            "config://app.properties",
            "text/plain",
            contents
        );
        
        String json = jsonb.toJson(content);
        assertTrue(json.contains("config://app.properties"));
        assertTrue(json.contains("text/plain"));
        assertTrue(json.contains("MyApp"));
        
        ResourceContent deserialized = jsonb.fromJson(json, ResourceContent.class);
        assertEquals(content, deserialized);
        assertEquals("config://app.properties", deserialized.getUri());
        assertEquals(1, deserialized.getContents().size());
    }
    
    @Test
    void testResourceContentTextFactory() {
        ResourceContent content = ResourceContent.text(
            "file://readme.txt",
            "text/plain",
            "This is a readme file"
        );
        
        assertEquals("file://readme.txt", content.getUri());
        assertEquals("text/plain", content.getMimeType());
        assertEquals(1, content.getContents().size());
        assertEquals("text", content.getContents().get(0).getType());
        assertEquals("This is a readme file", content.getContents().get(0).getText());
    }
    
    @Test
    void testResourceListResultSerialization() {
        List<ResourceDefinition> resources = List.of(
            new ResourceDefinition("config://app.properties", "App Config", "Config", "text/plain"),
            new ResourceDefinition("file://readme.md", "Readme", "Documentation", "text/markdown")
        );
        ResourceListResult result = new ResourceListResult(resources);
        
        String json = jsonb.toJson(result);
        assertTrue(json.contains("app.properties"));
        assertTrue(json.contains("readme.md"));
        
        ResourceListResult deserialized = jsonb.fromJson(json, ResourceListResult.class);
        assertEquals(2, deserialized.getResources().size());
    }
    
    @Test
    void testResourceListResultEmpty() {
        ResourceListResult result = new ResourceListResult(List.of());
        
        String json = jsonb.toJson(result);
        ResourceListResult deserialized = jsonb.fromJson(json, ResourceListResult.class);
        
        assertNotNull(deserialized.getResources());
        assertTrue(deserialized.getResources().isEmpty());
    }
    
    @Test
    void testResourceReadResultSerialization() {
        ResourceContent content = ResourceContent.text(
            "file://test.txt",
            "text/plain",
            "Test content"
        );
        ResourceReadResult result = ResourceReadResult.of(content);
        
        String json = jsonb.toJson(result);
        assertTrue(json.contains("file://test.txt"));
        assertTrue(json.contains("Test content"));
        
        ResourceReadResult deserialized = jsonb.fromJson(json, ResourceReadResult.class);
        assertEquals(1, deserialized.getContents().size());
        assertEquals("file://test.txt", deserialized.getContents().get(0).getUri());
    }
    
    @Test
    void testResourceReadResultMultipleContents() {
        List<ResourceContent> contents = List.of(
            ResourceContent.text("file://file1.txt", "text/plain", "Content 1"),
            ResourceContent.text("file://file2.txt", "text/plain", "Content 2")
        );
        ResourceReadResult result = new ResourceReadResult(contents);
        
        String json = jsonb.toJson(result);
        ResourceReadResult deserialized = jsonb.fromJson(json, ResourceReadResult.class);
        
        assertEquals(2, deserialized.getContents().size());
    }
}
