package dukes.mcp.service;

import dukes.mcp.model.ResourceContent;
import dukes.mcp.model.ResourceDefinition;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * ApplicationScoped CDI bean that manages the registration, discovery, and reading of MCP resources.
 * 
 * <p>The ResourceManager provides a centralized registry for resources that can be discovered and read
 * through the MCP protocol. It supports dynamic resource registration/unregistration, thread-safe
 * concurrent access, and enforces unique resource URIs.</p>
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li>Thread-safe resource storage using ConcurrentHashMap</li>
 *   <li>Dynamic resource registration and unregistration</li>
 *   <li>Unique resource URI enforcement</li>
 *   <li>Graceful error handling for resource reading failures</li>
 *   <li>Resource discovery through listResources()</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * @Inject
 * private ResourceManager resourceManager;
 * 
 * public void init() {
 *     resourceManager.registerResource(new ConfigurationResource());
 *     List<ResourceDefinition> resources = resourceManager.listResources();
 *     ResourceContent content = resourceManager.readResource("config://application.properties");
 * }
 * }</pre>
 * 
 * @see Resource
 * @see ResourceDefinition
 * @see ResourceContent
 */
@ApplicationScoped
public class ResourceManager {
    
    private static final Logger LOGGER = Logger.getLogger(ResourceManager.class.getName());
    
    /**
     * Thread-safe map storing registered resources by URI.
     */
    private final ConcurrentHashMap<String, Resource> resources = new ConcurrentHashMap<>();
    
    /**
     * Registers a resource in the registry.
     * 
     * <p>If a resource with the same URI already exists, it will be replaced with the new resource.
     * This allows for dynamic resource updates at runtime.</p>
     * 
     * @param resource the resource to register, must not be null
     * @throws NullPointerException if resource is null
     * @throws IllegalArgumentException if resource URI is null or empty
     */
    public void registerResource(Resource resource) {
        if (resource == null) {
            throw new NullPointerException("Resource cannot be null");
        }
        
        String uri = resource.getUri();
        if (uri == null || uri.trim().isEmpty()) {
            throw new IllegalArgumentException("Resource URI cannot be null or empty");
        }
        
        Resource previousResource = resources.put(uri, resource);
        if (previousResource != null) {
            LOGGER.log(Level.INFO, "Replaced existing resource: {0}", uri);
        } else {
            LOGGER.log(Level.INFO, "Registered new resource: {0}", uri);
        }
    }
    
    /**
     * Unregisters a resource from the registry by URI.
     * 
     * <p>If the resource does not exist, this method does nothing.</p>
     * 
     * @param uri the URI of the resource to unregister, must not be null
     * @throws NullPointerException if uri is null
     */
    public void unregisterResource(String uri) {
        if (uri == null) {
            throw new NullPointerException("Resource URI cannot be null");
        }
        
        Resource removedResource = resources.remove(uri);
        if (removedResource != null) {
            LOGGER.log(Level.INFO, "Unregistered resource: {0}", uri);
        }
    }
    
    /**
     * Retrieves a resource by URI.
     * 
     * @param uri the URI of the resource to retrieve
     * @return an Optional containing the resource if found, or empty if not found
     */
    public Optional<Resource> getResource(String uri) {
        return Optional.ofNullable(resources.get(uri));
    }
    
    /**
     * Lists all registered resources.
     * 
     * <p>Returns a list of ResourceDefinition objects containing the URI, name, description,
     * and MIME type for each registered resource. The list is a snapshot of the current
     * registry state and is safe to iterate even if resources are registered/unregistered
     * concurrently.</p>
     * 
     * @return a list of all registered resource definitions, never null
     */
    public List<ResourceDefinition> listResources() {
        return resources.values().stream()
                .map(resource -> new ResourceDefinition(
                        resource.getUri(),
                        resource.getName(),
                        resource.getDescription(),
                        resource.getMimeType()
                ))
                .collect(Collectors.toList());
    }
    
    /**
     * Reads and returns the content of a resource by URI.
     * 
     * <p>This method performs the following steps:</p>
     * <ol>
     *   <li>Looks up the resource by URI</li>
     *   <li>Reads the resource content</li>
     *   <li>Returns the content with URI and MIME type</li>
     * </ol>
     * 
     * <p>All errors are caught and wrapped in appropriate exceptions with descriptive messages.
     * This ensures consistent error handling across the MCP protocol.</p>
     * 
     * @param uri the URI of the resource to read
     * @return a ResourceContent containing the resource data with URI and MIME type
     * @throws ResourceNotFoundException if the resource URI is not registered
     * @throws ResourceReadException if the resource cannot be read due to I/O errors
     */
    public ResourceContent readResource(String uri) {
        // Step 1: Lookup resource by URI
        Optional<Resource> resourceOpt = getResource(uri);
        if (resourceOpt.isEmpty()) {
            LOGGER.log(Level.WARNING, "Resource not found: {0}", uri);
            throw new ResourceNotFoundException("Resource not found: " + uri);
        }
        Resource resource = resourceOpt.get();
        
        // Step 2: Read resource content
        try {
            LOGGER.log(Level.FINE, "Reading resource: {0}", uri);
            String content = resource.read();
            String mimeType = resource.getMimeType();
            
            // Step 3: Build resource content response
            return ResourceContent.text(uri, mimeType, content);
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to read resource: " + uri, e);
            throw new ResourceReadException("Failed to read resource: " + uri, e);
        }
    }
    
    /**
     * Exception thrown when a requested resource is not found in the registry.
     */
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }
    
    /**
     * Exception thrown when a resource cannot be read due to I/O errors.
     */
    public static class ResourceReadException extends RuntimeException {
        public ResourceReadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
