package dukes.mcp.resources;

import dukes.mcp.service.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Example resource that demonstrates configuration file access through MCP.
 * 
 * <p>This resource allows AI clients to read the application configuration file
 * (application.properties). It demonstrates a simple resource implementation that
 * reads a file from the classpath and returns it with the appropriate MIME type.</p>
 * 
 * <p>The resource handles common error scenarios:
 * <ul>
 *   <li>File not found - throws IOException with descriptive message</li>
 *   <li>I/O errors during reading - propagates IOException</li>
 * </ul>
 * </p>
 * 
 * <p><b>Validates: Requirements 8.1, 8.2, 9.1</b></p>
 */
@ApplicationScoped
public class ConfigurationResource implements Resource {
    
    private static final String CONFIG_FILE_PATH = "/application.properties";
    private static final String RESOURCE_URI = "config://application.properties";
    
    @Override
    public String getUri() {
        return RESOURCE_URI;
    }
    
    @Override
    public String getName() {
        return "Application Configuration";
    }
    
    @Override
    public String getDescription() {
        return "Main application configuration properties file. Contains settings for the MCP server " +
               "and Jakarta EE application configuration.";
    }
    
    @Override
    public String getMimeType() {
        return "text/plain";
    }
    
    @Override
    public String read() throws IOException {
        try (InputStream is = getClass().getResourceAsStream(CONFIG_FILE_PATH)) {
            if (is == null) {
                throw new IOException("Configuration file not found: " + CONFIG_FILE_PATH);
            }
            
            byte[] bytes = is.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
            
        } catch (IOException e) {
            // Re-throw IOException with context
            throw new IOException("Failed to read configuration file: " + e.getMessage(), e);
        }
    }
}
