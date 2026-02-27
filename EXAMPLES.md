# MCP Server Examples

This document provides detailed examples of using the MCP Server for Jakarta EE, including complete implementations and usage scenarios.

## Table of Contents

- [Basic Tool Examples](#basic-tool-examples)
- [Advanced Tool Examples](#advanced-tool-examples)
- [Resource Examples](#resource-examples)
- [Prompt Examples](#prompt-examples)
- [Integration Examples](#integration-examples)
- [Error Handling Examples](#error-handling-examples)

## Basic Tool Examples

### Simple Calculator Tool

A basic tool that performs arithmetic operations:

```java
package com.example.tools;

import dukes.mcp.model.ToolResult;
import dukes.mcp.service.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class CalculatorTool implements Tool {
    
    @Override
    public String getName() {
        return "calculator";
    }
    
    @Override
    public String getDescription() {
        return "Performs basic arithmetic operations (add, subtract, multiply, divide)";
    }
    
    @Override
    public Object getInputSchema() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "operation", Map.of(
                    "type", "string",
                    "enum", List.of("add", "subtract", "multiply", "divide"),
                    "description", "The arithmetic operation to perform"
                ),
                "a", Map.of(
                    "type", "number",
                    "description", "First operand"
                ),
                "b", Map.of(
                    "type", "number",
                    "description", "Second operand"
                )
            ),
            "required", List.of("operation", "a", "b")
        );
    }
    
    @Override
    public ToolResult execute(Map<String, Object> arguments) {
        String operation = (String) arguments.get("operation");
        double a = ((Number) arguments.get("a")).doubleValue();
        double b = ((Number) arguments.get("b")).doubleValue();
        
        double result;
        switch (operation) {
            case "add":
                result = a + b;
                break;
            case "subtract":
                result = a - b;
                break;
            case "multiply":
                result = a * b;
                break;
            case "divide":
                if (b == 0) {
                    return ToolResult.error("Division by zero is not allowed");
                }
                result = a / b;
                break;
            default:
                return ToolResult.error("Unknown operation: " + operation);
        }
        
        return ToolResult.success(String.format("%s %s %s = %s", a, operation, b, result));
    }
}
```

**Usage:**

```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "method": "tools/call",
  "params": {
    "name": "calculator",
    "arguments": {
      "operation": "multiply",
      "a": 42,
      "b": 2.5
    }
  }
}
```

### String Manipulation Tool

A tool for common string operations:

```java
package com.example.tools;

import dukes.mcp.model.ToolResult;
import dukes.mcp.service.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class StringTool implements Tool {
    
    @Override
    public String getName() {
        return "string_manipulator";
    }
    
    @Override
    public String getDescription() {
        return "Performs string manipulation operations (uppercase, lowercase, reverse, length)";
    }
    
    @Override
    public Object getInputSchema() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "operation", Map.of(
                    "type", "string",
                    "enum", List.of("uppercase", "lowercase", "reverse", "length", "trim"),
                    "description", "The string operation to perform"
                ),
                "text", Map.of(
                    "type", "string",
                    "description", "The input text"
                )
            ),
            "required", List.of("operation", "text")
        );
    }
    
    @Override
    public ToolResult execute(Map<String, Object> arguments) {
        String operation = (String) arguments.get("operation");
        String text = (String) arguments.get("text");
        
        String result;
        switch (operation) {
            case "uppercase":
                result = text.toUpperCase();
                break;
            case "lowercase":
                result = text.toLowerCase();
                break;
            case "reverse":
                result = new StringBuilder(text).reverse().toString();
                break;
            case "length":
                result = String.valueOf(text.length());
                break;
            case "trim":
                result = text.trim();
                break;
            default:
                return ToolResult.error("Unknown operation: " + operation);
        }
        
        return ToolResult.success(result);
    }
}
```

## Advanced Tool Examples

### REST API Client Tool

A tool that makes HTTP requests to external APIs:

```java
package com.example.tools;

import dukes.mcp.model.ToolResult;
import dukes.mcp.service.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class RestApiTool implements Tool {
    
    private final Client client = ClientBuilder.newClient();
    
    @Override
    public String getName() {
        return "rest_api_call";
    }
    
    @Override
    public String getDescription() {
        return "Makes HTTP GET requests to external REST APIs";
    }
    
    @Override
    public Object getInputSchema() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "url", Map.of(
                    "type", "string",
                    "format", "uri",
                    "description", "The URL to request"
                ),
                "headers", Map.of(
                    "type", "object",
                    "description", "Optional HTTP headers",
                    "additionalProperties", Map.of("type", "string")
                )
            ),
            "required", List.of("url")
        );
    }
    
    @Override
    public ToolResult execute(Map<String, Object> arguments) {
        String url = (String) arguments.get("url");
        
        try {
            var requestBuilder = client.target(url).request();
            
            // Add custom headers if provided
            if (arguments.containsKey("headers")) {
                @SuppressWarnings("unchecked")
                Map<String, String> headers = (Map<String, String>) arguments.get("headers");
                headers.forEach(requestBuilder::header);
            }
            
            try (Response response = requestBuilder.get()) {
                String body = response.readEntity(String.class);
                
                String result = String.format(
                    "Status: %d\nContent-Type: %s\n\nBody:\n%s",
                    response.getStatus(),
                    response.getHeaderString("Content-Type"),
                    body
                );
                
                return ToolResult.success(result);
            }
            
        } catch (Exception e) {
            return ToolResult.error("HTTP request failed: " + e.getMessage());
        }
    }
}
```

### File System Tool

A tool for reading files from the server's file system:

```java
package com.example.tools;

import dukes.mcp.model.ToolResult;
import dukes.mcp.service.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class FileReaderTool implements Tool {
    
    private static final String BASE_DIR = "/var/app/data"; // Configure as needed
    
    @Override
    public String getName() {
        return "file_reader";
    }
    
    @Override
    public String getDescription() {
        return "Reads text files from the server's file system";
    }
    
    @Override
    public Object getInputSchema() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "path", Map.of(
                    "type", "string",
                    "description", "Relative path to the file (within the base directory)"
                ),
                "encoding", Map.of(
                    "type", "string",
                    "enum", List.of("UTF-8", "ISO-8859-1", "US-ASCII"),
                    "default", "UTF-8",
                    "description", "Character encoding of the file"
                )
            ),
            "required", List.of("path")
        );
    }
    
    @Override
    public ToolResult execute(Map<String, Object> arguments) {
        String relativePath = (String) arguments.get("path");
        String encoding = arguments.containsKey("encoding") 
            ? (String) arguments.get("encoding") 
            : "UTF-8";
        
        // Security: Prevent path traversal attacks
        if (relativePath.contains("..") || relativePath.startsWith("/")) {
            return ToolResult.error("Invalid path: path traversal not allowed");
        }
        
        try {
            Path fullPath = Paths.get(BASE_DIR, relativePath);
            
            // Verify the resolved path is still within BASE_DIR
            if (!fullPath.normalize().startsWith(Paths.get(BASE_DIR).normalize())) {
                return ToolResult.error("Invalid path: outside base directory");
            }
            
            if (!Files.exists(fullPath)) {
                return ToolResult.error("File not found: " + relativePath);
            }
            
            if (!Files.isRegularFile(fullPath)) {
                return ToolResult.error("Not a regular file: " + relativePath);
            }
            
            String content = Files.readString(fullPath, java.nio.charset.Charset.forName(encoding));
            return ToolResult.success(content);
            
        } catch (IOException e) {
            return ToolResult.error("Failed to read file: " + e.getMessage());
        }
    }
}
```

### Email Sender Tool

A tool that sends emails using Jakarta Mail:

```java
package com.example.tools;

import dukes.mcp.model.ToolResult;
import dukes.mcp.service.Tool;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class EmailTool implements Tool {
    
    @Resource(lookup = "java:comp/DefaultMailSession")
    private Session mailSession;
    
    @Override
    public String getName() {
        return "send_email";
    }
    
    @Override
    public String getDescription() {
        return "Sends an email message";
    }
    
    @Override
    public Object getInputSchema() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "to", Map.of(
                    "type", "string",
                    "format", "email",
                    "description", "Recipient email address"
                ),
                "subject", Map.of(
                    "type", "string",
                    "description", "Email subject"
                ),
                "body", Map.of(
                    "type", "string",
                    "description", "Email body content"
                )
            ),
            "required", List.of("to", "subject", "body")
        );
    }
    
    @Override
    public ToolResult execute(Map<String, Object> arguments) {
        String to = (String) arguments.get("to");
        String subject = (String) arguments.get("subject");
        String body = (String) arguments.get("body");
        
        try {
            MimeMessage message = new MimeMessage(mailSession);
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);
            message.setText(body);
            
            Transport.send(message);
            
            return ToolResult.success("Email sent successfully to " + to);
            
        } catch (Exception e) {
            return ToolResult.error("Failed to send email: " + e.getMessage());
        }
    }
}
```

## Resource Examples

### JSON Configuration Resource

A resource that provides configuration as JSON:

```java
package com.example.resources;

import dukes.mcp.service.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import java.io.IOException;

@ApplicationScoped
public class JsonConfigResource implements Resource {
    
    @Override
    public String getUri() {
        return "config://app-settings.json";
    }
    
    @Override
    public String getName() {
        return "Application Settings (JSON)";
    }
    
    @Override
    public String getDescription() {
        return "Application configuration in JSON format";
    }
    
    @Override
    public String getMimeType() {
        return "application/json";
    }
    
    @Override
    public String read() throws IOException {
        JsonObject config = Json.createObjectBuilder()
            .add("application", Json.createObjectBuilder()
                .add("name", "MCP Server")
                .add("version", "1.0.0")
                .add("environment", "production"))
            .add("features", Json.createObjectBuilder()
                .add("tools", true)
                .add("resources", true)
                .add("prompts", true))
            .add("limits", Json.createObjectBuilder()
                .add("maxToolExecutionTime", 30000)
                .add("maxResourceSize", 10485760))
            .build();
        
        return config.toString();
    }
}
```

### Dynamic Log Resource

A resource that provides recent log entries:

```java
package com.example.resources;

import dukes.mcp.service.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class LogResource implements Resource {
    
    private static final String LOG_FILE = "/var/log/app/application.log";
    private static final int MAX_LINES = 100;
    
    @Override
    public String getUri() {
        return "logs://application";
    }
    
    @Override
    public String getName() {
        return "Application Logs";
    }
    
    @Override
    public String getDescription() {
        return "Recent application log entries (last " + MAX_LINES + " lines)";
    }
    
    @Override
    public String getMimeType() {
        return "text/plain";
    }
    
    @Override
    public String read() throws IOException {
        Path logPath = Paths.get(LOG_FILE);
        
        if (!Files.exists(logPath)) {
            return "Log file not found";
        }
        
        try {
            List<String> allLines = Files.readAllLines(logPath);
            
            // Get last N lines
            int startIndex = Math.max(0, allLines.size() - MAX_LINES);
            List<String> recentLines = allLines.subList(startIndex, allLines.size());
            
            return recentLines.stream()
                .collect(Collectors.joining("\n"));
                
        } catch (IOException e) {
            throw new IOException("Failed to read log file: " + e.getMessage(), e);
        }
    }
}
```

### Metrics Resource

A resource that provides application metrics:

```java
package com.example.resources;

import dukes.mcp.service.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;

@ApplicationScoped
public class MetricsResource implements Resource {
    
    @Override
    public String getUri() {
        return "metrics://application";
    }
    
    @Override
    public String getName() {
        return "Application Metrics";
    }
    
    @Override
    public String getDescription() {
        return "Real-time application performance metrics";
    }
    
    @Override
    public String getMimeType() {
        return "application/json";
    }
    
    @Override
    public String read() throws IOException {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        
        JsonObjectBuilder metrics = Json.createObjectBuilder()
            .add("timestamp", System.currentTimeMillis())
            .add("memory", Json.createObjectBuilder()
                .add("heap", Json.createObjectBuilder()
                    .add("used", memoryBean.getHeapMemoryUsage().getUsed())
                    .add("max", memoryBean.getHeapMemoryUsage().getMax())
                    .add("committed", memoryBean.getHeapMemoryUsage().getCommitted()))
                .add("nonHeap", Json.createObjectBuilder()
                    .add("used", memoryBean.getNonHeapMemoryUsage().getUsed())
                    .add("max", memoryBean.getNonHeapMemoryUsage().getMax())
                    .add("committed", memoryBean.getNonHeapMemoryUsage().getCommitted())))
            .add("threads", Json.createObjectBuilder()
                .add("count", threadBean.getThreadCount())
                .add("peak", threadBean.getPeakThreadCount())
                .add("daemon", threadBean.getDaemonThreadCount()))
            .add("runtime", Json.createObjectBuilder()
                .add("uptime", ManagementFactory.getRuntimeMXBean().getUptime())
                .add("startTime", ManagementFactory.getRuntimeMXBean().getStartTime()));
        
        return metrics.build().toString();
    }
}
```

## Prompt Examples

### Bug Report Prompt

A prompt for generating bug reports:

```java
package com.example.prompts;

import dukes.mcp.model.PromptArgument;
import dukes.mcp.model.PromptMessage;
import dukes.mcp.service.Prompt;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class BugReportPrompt implements Prompt {
    
    @Override
    public String getName() {
        return "bug_report";
    }
    
    @Override
    public String getDescription() {
        return "Generates a structured bug report template";
    }
    
    @Override
    public List<PromptArgument> getArguments() {
        return List.of(
            new PromptArgument("component", "The component or module where the bug occurs", true),
            new PromptArgument("severity", "Bug severity (critical, high, medium, low)", true),
            new PromptArgument("environment", "Environment where bug was found (optional)", false)
        );
    }
    
    @Override
    public List<PromptMessage> render(Map<String, String> arguments) {
        String component = arguments.get("component");
        String severity = arguments.get("severity");
        String environment = arguments.getOrDefault("environment", "production");
        
        if (component == null || component.trim().isEmpty()) {
            throw new IllegalArgumentException("Required argument 'component' is missing");
        }
        if (severity == null || severity.trim().isEmpty()) {
            throw new IllegalArgumentException("Required argument 'severity' is missing");
        }
        
        String content = String.format(
            "Please create a detailed bug report for the %s component.\n\n" +
            "Severity: %s\n" +
            "Environment: %s\n\n" +
            "Include the following sections:\n" +
            "1. Summary - Brief description of the issue\n" +
            "2. Steps to Reproduce - Detailed steps to reproduce the bug\n" +
            "3. Expected Behavior - What should happen\n" +
            "4. Actual Behavior - What actually happens\n" +
            "5. Impact - How this affects users or the system\n" +
            "6. Possible Cause - Initial analysis of what might be causing the issue\n" +
            "7. Suggested Fix - Recommendations for fixing the bug\n" +
            "8. Additional Context - Any relevant logs, screenshots, or data",
            component, severity, environment
        );
        
        return List.of(PromptMessage.user(content));
    }
}
```

### API Documentation Prompt

A prompt for generating API documentation:

```java
package com.example.prompts;

import dukes.mcp.model.PromptArgument;
import dukes.mcp.model.PromptMessage;
import dukes.mcp.service.Prompt;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ApiDocPrompt implements Prompt {
    
    @Override
    public String getName() {
        return "api_documentation";
    }
    
    @Override
    public String getDescription() {
        return "Generates API documentation for endpoints";
    }
    
    @Override
    public List<PromptArgument> getArguments() {
        return List.of(
            new PromptArgument("endpoint", "The API endpoint path", true),
            new PromptArgument("method", "HTTP method (GET, POST, PUT, DELETE)", true),
            new PromptArgument("format", "Documentation format (markdown, openapi, html)", false)
        );
    }
    
    @Override
    public List<PromptMessage> render(Map<String, String> arguments) {
        String endpoint = arguments.get("endpoint");
        String method = arguments.get("method");
        String format = arguments.getOrDefault("format", "markdown");
        
        if (endpoint == null || endpoint.trim().isEmpty()) {
            throw new IllegalArgumentException("Required argument 'endpoint' is missing");
        }
        if (method == null || method.trim().isEmpty()) {
            throw new IllegalArgumentException("Required argument 'method' is missing");
        }
        
        String content = String.format(
            "Please generate comprehensive API documentation for the following endpoint:\n\n" +
            "Endpoint: %s %s\n" +
            "Format: %s\n\n" +
            "Include:\n" +
            "- Description of what the endpoint does\n" +
            "- Request parameters (path, query, body)\n" +
            "- Request body schema (if applicable)\n" +
            "- Response format and status codes\n" +
            "- Example requests and responses\n" +
            "- Authentication requirements\n" +
            "- Error responses and handling\n" +
            "- Rate limiting information\n" +
            "- Usage examples in multiple languages",
            method.toUpperCase(), endpoint, format
        );
        
        return List.of(PromptMessage.user(content));
    }
}
```

## Integration Examples

### Complete Application Initializer

Example of registering all custom components:

```java
package com.example;

import com.example.tools.*;
import com.example.resources.*;
import com.example.prompts.*;
import dukes.mcp.service.ToolRegistry;
import dukes.mcp.service.ResourceManager;
import dukes.mcp.service.PromptManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class CustomMCPInitializer {
    
    @Inject
    private ToolRegistry toolRegistry;
    
    @Inject
    private ResourceManager resourceManager;
    
    @Inject
    private PromptManager promptManager;
    
    // Tools
    @Inject private CalculatorTool calculatorTool;
    @Inject private StringTool stringTool;
    @Inject private RestApiTool restApiTool;
    @Inject private FileReaderTool fileReaderTool;
    @Inject private EmailTool emailTool;
    
    // Resources
    @Inject private JsonConfigResource jsonConfigResource;
    @Inject private LogResource logResource;
    @Inject private MetricsResource metricsResource;
    
    // Prompts
    @Inject private BugReportPrompt bugReportPrompt;
    @Inject private ApiDocPrompt apiDocPrompt;
    
    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        // Register tools
        toolRegistry.registerTool(calculatorTool);
        toolRegistry.registerTool(stringTool);
        toolRegistry.registerTool(restApiTool);
        toolRegistry.registerTool(fileReaderTool);
        toolRegistry.registerTool(emailTool);
        
        // Register resources
        resourceManager.registerResource(jsonConfigResource);
        resourceManager.registerResource(logResource);
        resourceManager.registerResource(metricsResource);
        
        // Register prompts
        promptManager.registerPrompt(bugReportPrompt);
        promptManager.registerPrompt(apiDocPrompt);
        
        System.out.println("Custom MCP components registered successfully");
    }
}
```

## Error Handling Examples

### Robust Tool with Comprehensive Error Handling

```java
package com.example.tools;

import dukes.mcp.model.ToolResult;
import dukes.mcp.service.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class RobustTool implements Tool {
    
    private static final Logger LOGGER = Logger.getLogger(RobustTool.class.getName());
    
    @Override
    public String getName() {
        return "robust_example";
    }
    
    @Override
    public String getDescription() {
        return "Example tool with comprehensive error handling";
    }
    
    @Override
    public Object getInputSchema() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "input", Map.of(
                    "type", "string",
                    "minLength", 1,
                    "maxLength", 1000
                )
            ),
            "required", List.of("input")
        );
    }
    
    @Override
    public ToolResult execute(Map<String, Object> arguments) {
        try {
            // Validate arguments (even though schema validation should catch this)
            if (arguments == null || arguments.isEmpty()) {
                LOGGER.warning("Tool called with null or empty arguments");
                return ToolResult.error("Arguments are required");
            }
            
            String input = (String) arguments.get("input");
            
            // Additional validation
            if (input == null) {
                LOGGER.warning("Tool called with null input");
                return ToolResult.error("Input parameter is required");
            }
            
            if (input.trim().isEmpty()) {
                LOGGER.warning("Tool called with empty input");
                return ToolResult.error("Input cannot be empty");
            }
            
            // Perform operation with error handling
            String result = performOperation(input);
            
            LOGGER.info("Tool executed successfully");
            return ToolResult.success(result);
            
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid argument", e);
            return ToolResult.error("Invalid input: " + e.getMessage());
            
        } catch (SecurityException e) {
            LOGGER.log(Level.SEVERE, "Security violation", e);
            return ToolResult.error("Security error: operation not permitted");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error in tool execution", e);
            return ToolResult.error("Unexpected error: " + e.getClass().getSimpleName());
        }
    }
    
    private String performOperation(String input) {
        // Your implementation
        return "Processed: " + input;
    }
}
```

---

These examples demonstrate the flexibility and power of the MCP Server for Jakarta EE. You can combine and extend these patterns to create custom tools, resources, and prompts that fit your specific use cases.
