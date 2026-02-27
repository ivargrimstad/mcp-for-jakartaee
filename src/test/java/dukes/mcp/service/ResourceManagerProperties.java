package dukes.mcp.service;

import dukes.mcp.model.ResourceContent;
import dukes.mcp.model.ResourceDefinition;
import net.jqwik.api.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for ResourceManager.
 * 
 * These tests validate:
 * - Property 15: Resource Registration Makes Resources Available
 * - Property 16: Resource List Completeness
 * - Property 17: Resource Reading with Valid URI
 * - Property 19: Resource URI Uniqueness
 * 
 * **Validates: Requirements 9.2, 9.3, 7.1, 7.2, 8.1, 8.2, 9.4**
 */
class ResourceManagerProperties {
    
    /**
     * Property 15: Resource Registration Makes Resources Available
     * 
     * For any resource that is registered, it must appear in the resources/list response
     * and be readable via resources/read.
     * 
     * **Validates: Requirements 9.2, 9.3**
     */
    @Property
    void registeredResourceIsAvailableForListingAndReading(
            @ForAll("resourceUris") String uri,
            @ForAll("resourceNames") String name,
            @ForAll("resourceDescriptions") String description,
            @ForAll("mimeTypes") String mimeType,
            @ForAll("resourceContents") String content) {
        
        ResourceManager manager = new ResourceManager();
        Resource resource = new SimpleResource(uri, name, description, mimeType, content);
        
        // Register the resource
        manager.registerResource(resource);
        
        // Assert: Resource appears in list
        List<ResourceDefinition> resources = manager.listResources();
        assertTrue(resources.stream().anyMatch(r -> r.getUri().equals(uri)),
                "Registered resource must appear in resources list");
        
        // Assert: Resource can be retrieved
        assertTrue(manager.getResource(uri).isPresent(),
                "Registered resource must be retrievable by URI");
        
        // Assert: Resource can be read
        ResourceContent resourceContent = manager.readResource(uri);
        assertNotNull(resourceContent, "Resource reading must return content");
        assertEquals(uri, resourceContent.getUri(), "Resource content must have correct URI");
        assertEquals(mimeType, resourceContent.getMimeType(), "Resource content must have correct MIME type");
        assertNotNull(resourceContent.getContents(), "Resource content must have contents");
        assertFalse(resourceContent.getContents().isEmpty(), "Resource content must not be empty");
    }
    
    /**
     * Property 16: Resource List Completeness
     * 
     * For any set of registered resources, calling resources/list must return all
     * registered resources with their URI, name, description, and MIME type.
     * 
     * **Validates: Requirements 7.1, 7.2**
     */
    @Property
    void listResourcesReturnsAllRegisteredResources(
            @ForAll("resourceLists") List<SimpleResource> resourcesToRegister) {
        
        ResourceManager manager = new ResourceManager();
        
        // Register all resources
        for (SimpleResource resource : resourcesToRegister) {
            manager.registerResource(resource);
        }
        
        // Get resource list
        List<ResourceDefinition> listedResources = manager.listResources();
        
        // Assert: All registered resources appear in the list
        assertEquals(resourcesToRegister.size(), listedResources.size(),
                "List must contain all registered resources");
        
        for (SimpleResource resource : resourcesToRegister) {
            boolean found = listedResources.stream()
                    .anyMatch(def -> def.getUri().equals(resource.getUri()) &&
                                   def.getName().equals(resource.getName()) &&
                                   def.getDescription().equals(resource.getDescription()) &&
                                   def.getMimeType().equals(resource.getMimeType()));
            assertTrue(found, "Resource " + resource.getUri() + " must be in the list with complete metadata");
        }
    }
    
    /**
     * Property 17: Resource Reading with Valid URI
     * 
     * For any registered resource URI, calling resources/read must return the
     * resource content and MIME type.
     * 
     * **Validates: Requirements 8.1, 8.2**
     */
    @Property
    void resourceReadingWithValidUriReturnsContent(
            @ForAll("resourceUris") String uri,
            @ForAll("resourceNames") String name,
            @ForAll("mimeTypes") String mimeType,
            @ForAll("resourceContents") String content) {
        
        ResourceManager manager = new ResourceManager();
        
        // Create and register a resource
        Resource resource = new SimpleResource(uri, name, "Test resource", mimeType, content);
        manager.registerResource(resource);
        
        // Read the resource
        ResourceContent resourceContent = manager.readResource(uri);
        
        // Assert: Content is returned with correct metadata
        assertNotNull(resourceContent, "Resource content must not be null");
        assertEquals(uri, resourceContent.getUri(), "Resource content must have correct URI");
        assertEquals(mimeType, resourceContent.getMimeType(), "Resource content must have correct MIME type");
        
        // Assert: Content has proper structure
        assertNotNull(resourceContent.getContents(), "Resource content must have contents list");
        assertFalse(resourceContent.getContents().isEmpty(), "Resource content must not be empty");
        
        // Assert: Content matches what was stored
        String actualContent = resourceContent.getContents().get(0).getText();
        assertEquals(content, actualContent, "Resource content must match stored content");
    }
    
    /**
     * Property 19: Resource URI Uniqueness
     * 
     * For any two resources in the registry with the same URI, they must be
     * the same resource instance.
     * 
     * **Validates: Requirement 9.4**
     */
    @Property
    void resourceUriUniquenessIsEnforced(
            @ForAll("resourceUris") String uri) {
        
        ResourceManager manager = new ResourceManager();
        
        // Register first resource
        Resource resource1 = new SimpleResource(uri, "First Resource", "First version", "text/plain", "Content 1");
        manager.registerResource(resource1);
        
        // Register second resource with same URI
        Resource resource2 = new SimpleResource(uri, "Second Resource", "Second version", "application/json", "Content 2");
        manager.registerResource(resource2);
        
        // Assert: Only one resource with that URI exists
        List<ResourceDefinition> resources = manager.listResources();
        long count = resources.stream().filter(r -> r.getUri().equals(uri)).count();
        assertEquals(1, count, "Only one resource with the URI should exist");
        
        // Assert: The second resource replaced the first
        ResourceDefinition definition = resources.stream()
                .filter(r -> r.getUri().equals(uri))
                .findFirst()
                .orElseThrow();
        assertEquals("Second Resource", definition.getName(),
                "The second resource should have replaced the first");
        assertEquals("Second version", definition.getDescription(),
                "The second resource should have replaced the first");
    }
    
    /**
     * Property: Resource unregistration removes resource
     * 
     * For any registered resource, unregistering it by URI must remove it from
     * the registry so it no longer appears in resources/list and is not readable.
     * 
     * **Validates: Requirement 9.5**
     */
    @Property
    void unregisteredResourceIsNotAvailable(
            @ForAll("resourceUris") String uri) {
        
        ResourceManager manager = new ResourceManager();
        
        // Register resource
        Resource resource = new SimpleResource(uri, "Test Resource", "Test", "text/plain", "Content");
        manager.registerResource(resource);
        
        // Verify it's registered
        assertTrue(manager.getResource(uri).isPresent(),
                "Resource should be registered");
        
        // Unregister resource
        manager.unregisterResource(uri);
        
        // Assert: Resource is no longer in registry
        assertFalse(manager.getResource(uri).isPresent(),
                "Unregistered resource should not be retrievable");
        
        // Assert: Resource does not appear in list
        List<ResourceDefinition> resources = manager.listResources();
        assertFalse(resources.stream().anyMatch(r -> r.getUri().equals(uri)),
                "Unregistered resource should not appear in resources list");
        
        // Assert: Resource reading throws exception
        assertThrows(ResourceManager.ResourceNotFoundException.class,
                () -> manager.readResource(uri),
                "Reading unregistered resource should throw ResourceNotFoundException");
    }
    
    /**
     * Property: Non-existent resource rejection
     * 
     * For any resource URI that is not registered, calling resources/read must
     * throw ResourceNotFoundException.
     * 
     * **Validates: Requirement 8.3**
     */
    @Property
    void nonExistentResourceThrowsException(
            @ForAll("resourceUris") String uri) {
        
        ResourceManager manager = new ResourceManager();
        
        // Assert: Reading non-existent resource throws exception
        assertThrows(ResourceManager.ResourceNotFoundException.class,
                () -> manager.readResource(uri),
                "Reading non-existent resource should throw ResourceNotFoundException");
    }
    
    // ========== Arbitraries (Data Generators) ==========
    
    @Provide
    Arbitrary<String> resourceUris() {
        return Arbitraries.of(
                "config://application.properties",
                "config://database.properties",
                "file://readme.txt",
                "file://schema.sql",
                "db://schema/users",
                "db://schema/products",
                "api://swagger.json",
                "doc://user-guide.md"
        );
    }
    
    @Provide
    Arbitrary<String> resourceNames() {
        return Arbitraries.of(
                "Application Configuration",
                "Database Configuration",
                "README File",
                "Database Schema",
                "User Schema",
                "Product Schema",
                "API Documentation",
                "User Guide"
        );
    }
    
    @Provide
    Arbitrary<String> resourceDescriptions() {
        return Arbitraries.of(
                "Main application configuration properties",
                "Database connection settings",
                "Project documentation and setup instructions",
                "Database schema definition",
                "User table schema",
                "Product catalog schema",
                "REST API specification",
                "End-user documentation"
        );
    }
    
    @Provide
    Arbitrary<String> mimeTypes() {
        return Arbitraries.of(
                "text/plain",
                "application/json",
                "application/xml",
                "text/html",
                "text/markdown",
                "application/sql"
        );
    }
    
    @Provide
    Arbitrary<String> resourceContents() {
        return Arbitraries.of(
                "app.name=MyApp\napp.version=1.0",
                "{\"database\": \"postgresql\", \"host\": \"localhost\"}",
                "# README\n\nThis is a test project.",
                "CREATE TABLE users (id INT PRIMARY KEY);",
                "<config><setting>value</setting></config>",
                "<!DOCTYPE html><html><body>Test</body></html>"
        );
    }
    
    @Provide
    Arbitrary<List<SimpleResource>> resourceLists() {
        return Combinators.combine(
                resourceUris(),
                resourceNames(),
                resourceDescriptions(),
                mimeTypes(),
                resourceContents()
        ).as((uri, name, desc, mime, content) -> 
                new SimpleResource(uri, name, desc, mime, content))
         .list().ofMinSize(1).ofMaxSize(5)
         .map(list -> {
             // Ensure unique resource URIs
             Map<String, SimpleResource> uniqueResources = new ConcurrentHashMap<>();
             for (SimpleResource resource : list) {
                 uniqueResources.put(resource.getUri(), resource);
             }
             return List.copyOf(uniqueResources.values());
         });
    }
    
    // ========== Helper Classes ==========
    
    /**
     * Simple resource implementation for testing.
     */
    static class SimpleResource implements Resource {
        private final String uri;
        private final String name;
        private final String description;
        private final String mimeType;
        private final String content;
        
        public SimpleResource(String uri, String name, String description, String mimeType, String content) {
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
            return content;
        }
    }
}
