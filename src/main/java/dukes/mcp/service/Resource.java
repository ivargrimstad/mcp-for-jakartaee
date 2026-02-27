package dukes.mcp.service;

import java.io.IOException;

/**
 * Interface defining the contract for MCP resource implementations.
 * 
 * <p>Resources represent data entities (files, configurations, database records) that
 * AI clients can read through the MCP protocol. Each resource must provide metadata
 * (URI, name, description, MIME type) and implement the read logic.</p>
 * 
 * <p>Resource implementations should:</p>
 * <ul>
 *   <li>Return a unique URI that identifies the resource</li>
 *   <li>Provide a clear, descriptive name for AI clients</li>
 *   <li>Include a helpful description of the resource content</li>
 *   <li>Specify the appropriate MIME type for the content</li>
 *   <li>Handle I/O errors gracefully by throwing IOException</li>
 * </ul>
 * 
 * <p>Example implementation:</p>
 * <pre>{@code
 * @ApplicationScoped
 * public class ConfigurationResource implements Resource {
 *     @Override
 *     public String getUri() {
 *         return "config://application.properties";
 *     }
 *     
 *     @Override
 *     public String getName() {
 *         return "Application Configuration";
 *     }
 *     
 *     @Override
 *     public String getDescription() {
 *         return "Main application configuration properties";
 *     }
 *     
 *     @Override
 *     public String getMimeType() {
 *         return "text/plain";
 *     }
 *     
 *     @Override
 *     public String read() throws IOException {
 *         try (InputStream is = getClass().getResourceAsStream("/application.properties")) {
 *             if (is == null) {
 *                 throw new IOException("Configuration file not found");
 *             }
 *             return new String(is.readAllBytes(), StandardCharsets.UTF_8);
 *         }
 *     }
 * }
 * }</pre>
 * 
 * @see dukes.mcp.model.ResourceDefinition
 * @see dukes.mcp.model.ResourceContent
 */
public interface Resource {
    
    /**
     * Returns the unique URI of this resource.
     * 
     * <p>The URI is used to identify and access the resource through the MCP protocol.
     * It must be unique within the resource registry and should follow URI conventions.
     * Common URI schemes include:</p>
     * <ul>
     *   <li>config:// for configuration resources</li>
     *   <li>file:// for file system resources</li>
     *   <li>db:// for database resources</li>
     *   <li>Custom schemes for application-specific resources</li>
     * </ul>
     * 
     * @return the resource URI, must be non-null and non-empty
     */
    String getUri();
    
    /**
     * Returns a human-readable name for this resource.
     * 
     * <p>The name should be concise and descriptive, helping AI clients understand
     * what the resource represents. It is displayed in resource listings and should
     * be meaningful without requiring the full URI.</p>
     * 
     * @return the resource name, must be non-null and non-empty
     */
    String getName();
    
    /**
     * Returns a detailed description of this resource.
     * 
     * <p>The description should explain what data the resource contains, its purpose,
     * and any relevant context that helps AI clients understand when to access it.
     * This is particularly useful for resource discovery.</p>
     * 
     * @return the resource description, must be non-null and non-empty
     */
    String getDescription();
    
    /**
     * Returns the MIME type of the resource content.
     * 
     * <p>The MIME type indicates the format of the data returned by {@link #read()}.
     * Common MIME types include:</p>
     * <ul>
     *   <li>text/plain for plain text files</li>
     *   <li>application/json for JSON data</li>
     *   <li>application/xml for XML documents</li>
     *   <li>text/html for HTML content</li>
     * </ul>
     * 
     * @return the MIME type, must be non-null and follow MIME type conventions
     */
    String getMimeType();
    
    /**
     * Reads and returns the content of this resource.
     * 
     * <p>This method performs the actual resource reading operation. It should return
     * the complete resource content as a string. The format of the content should match
     * the MIME type returned by {@link #getMimeType()}.</p>
     * 
     * <p>The method should handle resource access errors by throwing IOException with
     * a descriptive error message. This ensures consistent error handling across the
     * MCP protocol.</p>
     * 
     * <p>Implementation considerations:</p>
     * <ul>
     *   <li>Ensure proper resource cleanup (close streams, connections, etc.)</li>
     *   <li>Use appropriate character encoding (typically UTF-8)</li>
     *   <li>Consider caching for frequently accessed resources</li>
     *   <li>Handle large resources appropriately (streaming, pagination, etc.)</li>
     * </ul>
     * 
     * @return the resource content as a string
     * @throws IOException if the resource cannot be read due to I/O errors
     */
    String read() throws IOException;
}
