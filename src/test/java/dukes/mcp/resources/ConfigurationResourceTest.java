package dukes.mcp.resources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConfigurationResource.
 * 
 * <p>Tests verify that the ConfigurationResource correctly implements the Resource interface
 * and handles configuration file reading with proper error handling.</p>
 * 
 * <p><b>Validates: Requirements 8.1, 8.2, 9.1</b></p>
 */
class ConfigurationResourceTest {
    
    private ConfigurationResource resource;
    
    @BeforeEach
    void setUp() {
        resource = new ConfigurationResource();
    }
    
    @Test
    void testGetUri() {
        // When
        String uri = resource.getUri();
        
        // Then
        assertNotNull(uri, "URI should not be null");
        assertEquals("config://application.properties", uri, "URI should match expected value");
    }
    
    @Test
    void testGetName() {
        // When
        String name = resource.getName();
        
        // Then
        assertNotNull(name, "Name should not be null");
        assertFalse(name.isEmpty(), "Name should not be empty");
        assertEquals("Application Configuration", name, "Name should match expected value");
    }
    
    @Test
    void testGetDescription() {
        // When
        String description = resource.getDescription();
        
        // Then
        assertNotNull(description, "Description should not be null");
        assertFalse(description.isEmpty(), "Description should not be empty");
        assertTrue(description.contains("configuration"), "Description should mention configuration");
    }
    
    @Test
    void testGetMimeType() {
        // When
        String mimeType = resource.getMimeType();
        
        // Then
        assertNotNull(mimeType, "MIME type should not be null");
        assertEquals("text/plain", mimeType, "MIME type should be text/plain for properties file");
    }
    
    @Test
    void testReadReturnsContent() throws IOException {
        // When
        String content = resource.read();
        
        // Then
        assertNotNull(content, "Content should not be null");
        assertFalse(content.isEmpty(), "Content should not be empty");
        
        // Verify it contains expected configuration properties
        assertTrue(content.contains("mcp.server.name"), "Content should contain server name property");
        assertTrue(content.contains("mcp.protocol.version"), "Content should contain protocol version property");
    }
    
    @Test
    void testReadReturnsValidPropertiesFormat() throws IOException {
        // When
        String content = resource.read();
        
        // Then
        // Properties file should contain key=value pairs
        String[] lines = content.split("\n");
        boolean hasPropertyLine = false;
        
        for (String line : lines) {
            String trimmed = line.trim();
            // Skip comments and empty lines
            if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                // Should contain = for key=value pairs
                if (trimmed.contains("=")) {
                    hasPropertyLine = true;
                    break;
                }
            }
        }
        
        assertTrue(hasPropertyLine, "Content should contain at least one property line with key=value format");
    }
    
    @Test
    void testReadIsConsistent() throws IOException {
        // When - read multiple times
        String content1 = resource.read();
        String content2 = resource.read();
        
        // Then - should return same content
        assertEquals(content1, content2, "Multiple reads should return identical content");
    }
    
    @Test
    void testResourceMetadataIsConsistent() {
        // When - get metadata multiple times
        String uri1 = resource.getUri();
        String uri2 = resource.getUri();
        String name1 = resource.getName();
        String name2 = resource.getName();
        String mimeType1 = resource.getMimeType();
        String mimeType2 = resource.getMimeType();
        
        // Then - should be consistent
        assertEquals(uri1, uri2, "URI should be consistent across calls");
        assertEquals(name1, name2, "Name should be consistent across calls");
        assertEquals(mimeType1, mimeType2, "MIME type should be consistent across calls");
    }
}
