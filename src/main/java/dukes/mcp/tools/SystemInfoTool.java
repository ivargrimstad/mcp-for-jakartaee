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
            String output = """
                    === System Information ===

                    Java Version: %s
                    Java Vendor: %s
                    Java Home: %s
                    Java VM: %s (%s)

                    Operating System: %s
                    OS Version: %s
                    OS Architecture: %s
                    Available Processors: %d
                    """.formatted(
                    System.getProperty("java.version"),
                    System.getProperty("java.vendor"),
                    System.getProperty("java.home"),
                    System.getProperty("java.vm.name"),
                    System.getProperty("java.vm.version"),
                    System.getProperty("os.name"),
                    System.getProperty("os.version"),
                    System.getProperty("os.arch"),
                    Runtime.getRuntime().availableProcessors());

            // Memory information (if requested)
            if (includeMemory) {
                Runtime runtime = Runtime.getRuntime();
                long maxMemory = runtime.maxMemory();
                long totalMemory = runtime.totalMemory();
                long freeMemory = runtime.freeMemory();
                long usedMemory = totalMemory - freeMemory;

                MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
                MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
                MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();

                output += """
                        === Memory Information ===

                        Max Memory: %s
                        Total Memory: %s
                        Used Memory: %s
                        Free Memory: %s

                        Heap Memory:
                          Used: %s
                          Committed: %s
                          Max: %s

                        Non-Heap Memory:
                          Used: %s
                          Committed: %s
                          Max: %s

                        """.formatted(
                        formatBytes(maxMemory),
                        formatBytes(totalMemory),
                        formatBytes(usedMemory),
                        formatBytes(freeMemory),
                        formatBytes(heapUsage.getUsed()),
                        formatBytes(heapUsage.getCommitted()),
                        formatBytes(heapUsage.getMax()),
                        formatBytes(nonHeapUsage.getUsed()),
                        formatBytes(nonHeapUsage.getCommitted()),
                        formatBytes(nonHeapUsage.getMax()));
            }

            // Additional system properties (if requested)
            if (includeProperties) {
                output += """
                        === Additional Properties ===

                        User Name: %s
                        User Home: %s
                        User Directory: %s
                        File Separator: %s
                        Path Separator: %s
                        Line Separator: %s
                        """.formatted(
                        System.getProperty("user.name"),
                        System.getProperty("user.home"),
                        System.getProperty("user.dir"),
                        System.getProperty("file.separator"),
                        System.getProperty("path.separator"),
                        escapeLineBreaks(System.getProperty("line.separator")));
            }

            return ToolResult.success(output);
            
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
