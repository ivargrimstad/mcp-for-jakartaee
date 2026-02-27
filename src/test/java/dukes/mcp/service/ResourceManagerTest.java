package dukes.mcp.service;

import dukes.mcp.model.ResourceContent;
import dukes.mcp.model.ResourceDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ResourceManager.
 * 
 * Tests resource registration, unregistration, discovery, and reading functionality.
 */
class ResourceManagerTest {
    
    private ResourceManager resourceManager;
    
    @BeforeEach
    void setUp() {
        resourceManager = new ResourceManager();
    }
    
    @Test
    void testRegisterResource() {
        // Given
        Resource resource = new TestResource("config://test", "Test Config", "Test configuration", "text/plain", "test content");
        
        // When
        resourceManager.registerResource(resource);
        
        // Then
        Optional<Resource> retrieved = resourceManager.getResource("config://test");
        assertTrue(retrieved.isPresent());
        assertEquals("config://test", retrieved.get().getUri());
    }
    
    @Test
    void testRegisterResourceWithNullThrowsException() {
        // When/Then
        assertThrows(NullPointerException.class, () -> resourceManager.registerResource(null));
    }
    
    @Test
    void testRegisterResourceWithNullUriThrowsException() {
        // Given
        Resource resource = new TestResource(null, "Test", "Test", "text/plain", "content");
        
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> resourceManager.registerResource(resource));
    }
    
    @Test
    void testRegisterResourceWithEmptyUriThrowsException() {
        // Given
        Resource resource = new TestResource("", "Test", "Test", "text/plain", "content");
        
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> resourceManager.registerResource(resource));
    }
    
    @Test
    void testRegisterResourceReplacesExisting() {
        // Given
        Resource resource1 = new TestResource("config://test", "Test 1", "First", "text/plain", "content 1");
        Resource resource2 = new TestResource("config://test", "Test 2", "Second", "text/plain", "content 2");
        
        // When
        resourceManager.registerResource(resource1);
        resourceManager.registerResource(resource2);
        
        // Then
        Optional<Resource> retrieved = resourceManager.getResource("config://test");
        assertTrue(retrieved.isPresent());
        assertEquals("Test 2", retrieved.get().getName());
    }
    
    @Test
    void testUnregisterResource() {
        // Given
        Resource resource = new TestResource("config://test", "Test", "Test", "text/plain", "content");
        resourceManager.registerResource(resource);
        
        // When
        resourceManager.unregisterResource("config://test");
        
        // Then
        Optional<Resource> retrieved = resourceManager.getResource("config://test");
        assertFalse(retrieved.isPresent());
    }
    
    @Test
    void testUnregisterNonExistentResource() {
        // When/Then - should not throw exception
        assertDoesNotThrow(() -> resourceManager.unregisterResource("config://nonexistent"));
    }
    
    @Test
    void testUnregisterResourceWithNullUriThrowsException() {
        // When/Then
        assertThrows(NullPointerException.class, () -> resourceManager.unregisterResource(null));
    }
    
    @Test
    void testGetResourceReturnsEmpty() {
        // When
        Optional<Resource> retrieved = resourceManager.getResource("config://nonexistent");
        
        // Then
        assertFalse(retrieved.isPresent());
    }
    
    @Test
    void testListResourcesEmpty() {
        // When
        List<ResourceDefinition> resources = resourceManager.listResources();
        
        // Then
        assertNotNull(resources);
        assertTrue(resources.isEmpty());
    }
    
    @Test
    void testListResourcesWithMultipleResources() {
        // Given
        Resource resource1 = new TestResource("config://test1", "Test 1", "First", "text/plain", "content 1");
        Resource resource2 = new TestResource("config://test2", "Test 2", "Second", "application/json", "content 2");
        Resource resource3 = new TestResource("db://schema", "Schema", "Database schema", "application/json", "schema");
        
        resourceManager.registerResource(resource1);
        resourceManager.registerResource(resource2);
        resourceManager.registerResource(resource3);
        
        // When
        List<ResourceDefinition> resources = resourceManager.listResources();
        
        // Then
        assertNotNull(resources);
        assertEquals(3, resources.size());
        
        // Verify all resources are present
        assertTrue(resources.stream().anyMatch(r -> r.getUri().equals("config://test1")));
        assertTrue(resources.stream().anyMatch(r -> r.getUri().equals("config://test2")));
        assertTrue(resources.stream().anyMatch(r -> r.getUri().equals("db://schema")));
        
        // Verify resource details
        ResourceDefinition def1 = resources.stream()
                .filter(r -> r.getUri().equals("config://test1"))
                .findFirst()
                .orElseThrow();
        assertEquals("Test 1", def1.getName());
        assertEquals("First", def1.getDescription());
        assertEquals("text/plain", def1.getMimeType());
    }
    
    @Test
    void testReadResourceSuccess() {
        // Given
        Resource resource = new TestResource("config://test", "Test", "Test", "text/plain", "test content");
        resourceManager.registerResource(resource);
        
        // When
        ResourceContent content = resourceManager.readResource("config://test");
        
        // Then
        assertNotNull(content);
        assertEquals("config://test", content.getUri());
        assertEquals("text/plain", content.getMimeType());
        assertNotNull(content.getContents());
        assertEquals(1, content.getContents().size());
        assertEquals("text", content.getContents().get(0).getType());
        assertEquals("test content", content.getContents().get(0).getText());
    }
    
    @Test
    void testReadResourceNotFound() {
        // When/Then
        ResourceManager.ResourceNotFoundException exception = assertThrows(
                ResourceManager.ResourceNotFoundException.class,
                () -> resourceManager.readResource("config://nonexistent")
        );
        assertTrue(exception.getMessage().contains("Resource not found"));
        assertTrue(exception.getMessage().contains("config://nonexistent"));
    }
    
    @Test
    void testReadResourceIOError() {
        // Given
        Resource resource = new TestResource("config://error", "Error", "Error", "text/plain", null) {
            @Override
            public String read() throws IOException {
                throw new IOException("Simulated I/O error");
            }
        };
        resourceManager.registerResource(resource);
        
        // When/Then
        ResourceManager.ResourceReadException exception = assertThrows(
                ResourceManager.ResourceReadException.class,
                () -> resourceManager.readResource("config://error")
        );
        assertTrue(exception.getMessage().contains("Failed to read resource"));
        assertTrue(exception.getMessage().contains("config://error"));
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof IOException);
    }
    
    @Test
    void testResourceUriUniqueness() {
        // Given
        Resource resource1 = new TestResource("config://unique", "First", "First", "text/plain", "content 1");
        Resource resource2 = new TestResource("config://unique", "Second", "Second", "text/plain", "content 2");
        
        // When
        resourceManager.registerResource(resource1);
        resourceManager.registerResource(resource2);
        
        // Then - only one resource with this URI should exist
        List<ResourceDefinition> resources = resourceManager.listResources();
        long count = resources.stream()
                .filter(r -> r.getUri().equals("config://unique"))
                .count();
        assertEquals(1, count);
        
        // Verify it's the second resource (replacement)
        Optional<Resource> retrieved = resourceManager.getResource("config://unique");
        assertTrue(retrieved.isPresent());
        assertEquals("Second", retrieved.get().getName());
    }
    
    @Test
    void testConcurrentResourceAccess() throws InterruptedException {
        // Given
        Resource resource = new TestResource("config://concurrent", "Test", "Test", "text/plain", "content");
        resourceManager.registerResource(resource);
        
        // When - multiple threads read the same resource
        Thread[] threads = new Thread[10];
        boolean[] results = new boolean[10];
        
        for (int i = 0; i < threads.length; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                try {
                    ResourceContent content = resourceManager.readResource("config://concurrent");
                    results[index] = content != null && "content".equals(content.getContents().get(0).getText());
                } catch (Exception e) {
                    results[index] = false;
                }
            });
            threads[i].start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Then - all reads should succeed
        for (boolean result : results) {
            assertTrue(result);
        }
    }
    
    /**
     * Test implementation of Resource interface.
     */
    private static class TestResource implements Resource {
        private final String uri;
        private final String name;
        private final String description;
        private final String mimeType;
        private final String content;
        
        public TestResource(String uri, String name, String description, String mimeType, String content) {
            this.uri = uri;
            this.name = name;
            this.description = description;
            this.mimeType = mimeType;
            this.content = content;
        }
        
        @Override
        public String getUri() {
            return uri;
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public String getDescription() {
            return description;
        }
        
        @Override
        public String getMimeType() {
            return mimeType;
        }
        
        @Override
        public String read() throws IOException {
            if (content == null) {
                throw new IOException("Content is null");
            }
            return content;
        }
    }
}
