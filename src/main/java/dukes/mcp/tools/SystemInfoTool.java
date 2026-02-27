package dukes.mcp.tools;

import dukes.mcp.model.ToolResult;
import dukes.mcp.service.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;
import java.util.Map;

/**
 * Example tool that demonstrates system information retrieval through MCP.
 * 
 * <p>This tool allows AI clients to retrieve system information including Java version,
 * operating system details, and memory usage. It demonstrates a simple tool implementation
 * with optional parameters for controlling the level of detail returned.</p>
 * 
 * <p>The tool supports optional parameters to customize the output:
 * <ul>
 *   <li>includeMemory - Include detailed memory usage information</li>
 *   <li>includeProperties - Include additional system properties</li>
 * </ul>
 * </p>
 * 
 * <p><b>Validates: Requirements 5.1, 5.2, 6.1</b></p>
 */
@ApplicationScoped
public class SystemInfoTool implements Tool {
    
    @Override
    public String getName() {
        return "system_info";
    }
    
    @Override
    public String getDescription() {
        return "Returns system information including Java version, operating system details, and memory usage. " +
               "Use this tool to gather information about the runtime environment.";
    }
    
    @Override
    public Object getInputSchema() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "includeMemory", Map.of(
                    "type", "boolean",
                    "description", "Include detailed memory usage information (optional, default: true)",
                    "default", true
                ),
                "includeProperties", Map.of(
                    "type", "boolean",
                    "description", "Include additional system properties (optional, default: false)",
                    "default", false
                )
            ),
            "additionalProperties", false
        );
    }
    
    @Override
    public ToolResult execute(Map<String, Object> arguments) {
        try {
            // Extract optional parameters with defaults
            boolean includeMemory = true;
            if (arguments.containsKey("includeMemory")) {
                Object value = arguments.get("includeMemory");
                if (value instanceof Boolean) {
                    includeMemory = (Boolean) value;
                }
            }
            
            boolean includeProperties = false;
            if (arguments.containsKey("includeProperties")) {
                Object value = arguments.get("includeProperties");
                if (value instanceof Boolean) {
                    includeProperties = (Boolean) value;
                }
            }
            
            // Build system information output
            StringBuilder output = new StringBuilder();
            output.append("=== System Information ===\n\n");
            
            // Java information
            output.append("Java Version: ").append(System.getProperty("java.version")).append("\n");
            output.append("Java Vendor: ").append(System.getProperty("java.vendor")).append("\n");
            output.append("Java Home: ").append(System.getProperty("java.home")).append("\n");
            output.append("Java VM: ").append(System.getProperty("java.vm.name"))
                  .append(" (").append(System.getProperty("java.vm.version")).append(")\n\n");
            
            // Operating system information
            output.append("Operating System: ").append(System.getProperty("os.name")).append("\n");
            output.append("OS Version: ").append(System.getProperty("os.version")).append("\n");
            output.append("OS Architecture: ").append(System.getProperty("os.arch")).append("\n");
            output.append("Available Processors: ").append(Runtime.getRuntime().availableProcessors()).append("\n\n");
            
            // Memory information (if requested)
            if (includeMemory) {
                output.append("=== Memory Information ===\n\n");
                
                Runtime runtime = Runtime.getRuntime();
                long maxMemory = runtime.maxMemory();
                long totalMemory = runtime.totalMemory();
                long freeMemory = runtime.freeMemory();
                long usedMemory = totalMemory - freeMemory;
                
                output.append("Max Memory: ").append(formatBytes(maxMemory)).append("\n");
                output.append("Total Memory: ").append(formatBytes(totalMemory)).append("\n");
                output.append("Used Memory: ").append(formatBytes(usedMemory)).append("\n");
                output.append("Free Memory: ").append(formatBytes(freeMemory)).append("\n");
                
                // Add heap memory details
                MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
                MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
                MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
                
                output.append("\nHeap Memory:\n");
                output.append("  Used: ").append(formatBytes(heapUsage.getUsed())).append("\n");
                output.append("  Committed: ").append(formatBytes(heapUsage.getCommitted())).append("\n");
                output.append("  Max: ").append(formatBytes(heapUsage.getMax())).append("\n");
                
                output.append("\nNon-Heap Memory:\n");
                output.append("  Used: ").append(formatBytes(nonHeapUsage.getUsed())).append("\n");
                output.append("  Committed: ").append(formatBytes(nonHeapUsage.getCommitted())).append("\n");
                output.append("  Max: ").append(formatBytes(nonHeapUsage.getMax())).append("\n\n");
            }
            
            // Additional system properties (if requested)
            if (includeProperties) {
                output.append("=== Additional Properties ===\n\n");
                output.append("User Name: ").append(System.getProperty("user.name")).append("\n");
                output.append("User Home: ").append(System.getProperty("user.home")).append("\n");
                output.append("User Directory: ").append(System.getProperty("user.dir")).append("\n");
                output.append("File Separator: ").append(System.getProperty("file.separator")).append("\n");
                output.append("Path Separator: ").append(System.getProperty("path.separator")).append("\n");
                output.append("Line Separator: ").append(escapeLineBreaks(System.getProperty("line.separator"))).append("\n");
            }
            
            return ToolResult.success(output.toString());
            
        } catch (SecurityException e) {
            // Handle security exceptions when accessing system properties
            return ToolResult.error("Security error accessing system information: " + e.getMessage());
            
        } catch (Exception e) {
            // Handle any other unexpected exceptions
            return ToolResult.error("Unexpected error retrieving system information: " + e.getMessage());
        }
    }
    
    /**
     * Formats a byte count into a human-readable string with appropriate units.
     * 
     * @param bytes the number of bytes to format
     * @return formatted string with units (B, KB, MB, GB)
     */
    private String formatBytes(long bytes) {
        if (bytes < 0) {
            return "N/A";
        }
        
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    /**
     * Escapes line break characters for display.
     * 
     * @param value the string to escape
     * @return escaped string representation
     */
    private String escapeLineBreaks(String value) {
        if (value == null) {
            return "null";
        }
        return value.replace("\n", "\\n").replace("\r", "\\r");
    }
}
