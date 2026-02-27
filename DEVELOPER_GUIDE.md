# Developer Guide

This guide provides detailed information for developers who want to extend the MCP Server for Jakarta EE with custom tools, resources, and prompts.

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Development Setup](#development-setup)
- [Creating Custom Tools](#creating-custom-tools)
- [Creating Custom Resources](#creating-custom-resources)
- [Creating Custom Prompts](#creating-custom-prompts)
- [JSON Schema Guide](#json-schema-guide)
- [Testing Your Components](#testing-your-components)
- [Best Practices](#best-practices)
- [Debugging](#debugging)

## Architecture Overview

### Component Hierarchy

```
MCPEndpoint (JAX-RS)
    ↓
MCPProtocolHandler (Protocol Logic)
    ↓
┌──────────────┬──────────────┬──────────────┐
│              │              │              │
ToolRegistry   ResourceManager  PromptManager
│              │              │              │
Tool           Resource       Prompt
(Interface)    (Interface)    (Interface)
```

### Request Flow

1. **Client** sends JSON-RPC request to `/mcp` endpoint
2. **MCPEndpoint** receives and parses the request
3. **MCPProtocolHandler** validates and routes the request
4. **Registry/Manager** looks up the requested component
5. **Component** executes and returns result
6. **Response** is formatted as JSON-RPC and sent back

### Key Interfaces

- **Tool**: Executable operations with input validation
- **Resource**: Readable data sources with MIME types
- **Prompt**: Parameterized templates for AI interactions

## Development Setup

### IDE Configuration

#### IntelliJ IDEA

1. Import as Maven project
2. Enable annotation processing
3. Configure Jakarta EE facets
4. Set Java SDK to 23+

#### Eclipse

1. Import as Maven project
2. Install Jakarta EE tools
3. Configure Java 23+ JDK
4. Enable CDI support

### Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── dukes/
│   │       └── mcp/
│   │           ├── endpoint/      # JAX-RS endpoints
│   │           ├── model/         # Data models
│   │           ├── service/       # Core services
│   │           ├── tools/         # Tool implementations
│   │           ├── resources/     # Resource implementations
│   │           └── prompts/       # Prompt implementations
│   ├── resources/
│   │   └── META-INF/
│   │       └── beans.xml          # CDI configuration
│   └── webapp/
│       └── WEB-INF/
│           └── web.xml            # Web application config
└── test/
    └── java/
        └── dukes/
            └── mcp/
                ├── service/       # Unit tests
                └── endpoint/      # Integration tests
```

### Dependencies

Key dependencies in `pom.xml`:

```xml
<!-- Jakarta EE 11 -->
<dependency>
    <groupId>jakarta.platform</groupId>
    <artifactId>jakarta.jakartaee-web-api</artifactId>
    <version>11.0.0</version>
    <scope>provided</scope>
</dependency>

<!-- MCP Server REST -->
<dependency>
    <groupId>za.co.sindi</groupId>
    <artifactId>sindi-ai-mcp-server-rest</artifactId>
    <version>0.0.3</version>
    <type>war</type>
</dependency>

<!-- JSON Schema Validation -->
<dependency>
    <groupId>com.networknt</groupId>
    <artifactId>json-schema-validator</artifactId>
    <version>1.5.3</version>
</dependency>
```

## Creating Custom Tools

### Tool Interface

```java
public interface Tool {
    String getName();
    String getDescription();
    Object getInputSchema();
    ToolResult execute(Map<String, Object> arguments);
}
```

### Step-by-Step Guide

#### 1. Create Tool Class

```java
package com.example.tools;

import dukes.mcp.model.ToolResult;
import dukes.mcp.service.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class MyTool implements Tool {
    
    @Override
    public String getName() {
        // Unique identifier (lowercase, underscores)
        return "my_tool";
    }
    
    @Override
    public String getDescription() {
        // Clear description of what the tool does
        return "Description of my tool's functionality";
    }
    
    @Override
    public Object getInputSchema() {
        // JSON Schema for input validation
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "param1", Map.of(
                    "type", "string",
                    "description", "First parameter"
                )
            ),
            "required", List.of("param1")
        );
    }
    
    @Override
    public ToolResult execute(Map<String, Object> arguments) {
        try {
            // Extract parameters
            String param1 = (String) arguments.get("param1");
            
            // Perform operation
            String result = performOperation(param1);
            
            // Return success
            return ToolResult.success(result);
            
        } catch (Exception e) {
            // Return error
            return ToolResult.error("Operation failed: " + e.getMessage());
        }
    }
    
    private String performOperation(String param1) {
        // Your implementation
        return "Result";
    }
}
```

#### 2. Inject Dependencies

```java
@ApplicationScoped
public class DatabaseTool implements Tool {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Inject
    private SomeService someService;
    
    // ... rest of implementation
}
```

#### 3. Register the Tool

**Option A: Automatic Registration (CDI)**

If your tool is a CDI bean (`@ApplicationScoped`), you can register it automatically:

```java
@ApplicationScoped
public class ToolInitializer {
    
    @Inject
    private ToolRegistry toolRegistry;
    
    @Inject
    private MyTool myTool;
    
    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        toolRegistry.registerTool(myTool);
    }
}
```

**Option B: Manual Registration**

```java
@ApplicationScoped
public class ManualRegistration {
    
    @Inject
    private ToolRegistry toolRegistry;
    
    public void registerTools(@Observes @Initialized(ApplicationScoped.class) Object init) {
        toolRegistry.registerTool(new MyTool());
    }
}
```

### Advanced Tool Patterns

#### Async Tool Execution

```java
@ApplicationScoped
public class AsyncTool implements Tool {
    
    @Resource
    private ManagedExecutorService executorService;
    
    @Override
    public ToolResult execute(Map<String, Object> arguments) {
        try {
            // Submit async task
            CompletableFuture<String> future = CompletableFuture.supplyAsync(
                () -> performLongRunningOperation(arguments),
                executorService
            );
            
            // Wait with timeout
            String result = future.get(30, TimeUnit.SECONDS);
            return ToolResult.success(result);
            
        } catch (TimeoutException e) {
            return ToolResult.error("Operation timed out");
        } catch (Exception e) {
            return ToolResult.error("Operation failed: " + e.getMessage());
        }
    }
}
```

#### Tool with Caching

```java
@ApplicationScoped
public class CachedTool implements Tool {
    
    private final Map<String, CachedResult> cache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL = 300_000; // 5 minutes
    
    @Override
    public ToolResult execute(Map<String, Object> arguments) {
        String cacheKey = generateCacheKey(arguments);
        
        CachedResult cached = cache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return cached.getResult();
        }
        
        ToolResult result = performOperation(arguments);
        cache.put(cacheKey, new CachedResult(result, System.currentTimeMillis() + CACHE_TTL));
        
        return result;
    }
    
    private static class CachedResult {
        private final ToolResult result;
        private final long expiryTime;
        
        CachedResult(ToolResult result, long expiryTime) {
            this.result = result;
            this.expiryTime = expiryTime;
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
        
        ToolResult getResult() {
            return result;
        }
    }
}
```

#### Tool with Transaction Management

```java
@ApplicationScoped
public class TransactionalTool implements Tool {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Inject
    private UserTransaction userTransaction;
    
    @Override
    public ToolResult execute(Map<String, Object> arguments) {
        try {
            userTransaction.begin();
            
            // Perform database operations
            String result = performDatabaseOperation(arguments);
            
            userTransaction.commit();
            return ToolResult.success(result);
            
        } catch (Exception e) {
            try {
                userTransaction.rollback();
            } catch (SystemException se) {
                // Log rollback failure
            }
            return ToolResult.error("Transaction failed: " + e.getMessage());
        }
    }
}
```

## Creating Custom Resources

### Resource Interface

```java
public interface Resource {
    String getUri();
    String getName();
    String getDescription();
    String getMimeType();
    String read() throws IOException;
}
```

### Step-by-Step Guide

#### 1. Create Resource Class

```java
package com.example.resources;

import dukes.mcp.service.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;

@ApplicationScoped
public class MyResource implements Resource {
    
    @Override
    public String getUri() {
        // Unique URI (use custom scheme)
        return "custom://my-resource";
    }
    
    @Override
    public String getName() {
        // Human-readable name
        return "My Custom Resource";
    }
    
    @Override
    public String getDescription() {
        // Description of what this resource provides
        return "Description of the resource content";
    }
    
    @Override
    public String getMimeType() {
        // MIME type of the content
        return "application/json"; // or "text/plain", etc.
    }
    
    @Override
    public String read() throws IOException {
        try {
            // Fetch and return content
            return fetchContent();
        } catch (Exception e) {
            throw new IOException("Failed to read resource: " + e.getMessage(), e);
        }
    }
    
    private String fetchContent() {
        // Your implementation
        return "{\"data\": \"example\"}";
    }
}
```

#### 2. Register the Resource

```java
@ApplicationScoped
public class ResourceInitializer {
    
    @Inject
    private ResourceManager resourceManager;
    
    @Inject
    private MyResource myResource;
    
    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        resourceManager.registerResource(myResource);
    }
}
```

### Advanced Resource Patterns

#### Dynamic Resource

```java
@ApplicationScoped
public class DynamicResource implements Resource {
    
    @Override
    public String getUri() {
        return "dynamic://current-time";
    }
    
    @Override
    public String read() throws IOException {
        // Return current data
        return Json.createObjectBuilder()
            .add("timestamp", System.currentTimeMillis())
            .add("datetime", Instant.now().toString())
            .build()
            .toString();
    }
}
```

#### Streaming Resource

```java
@ApplicationScoped
public class StreamingResource implements Resource {
    
    @Override
    public String getMimeType() {
        return "text/plain";
    }
    
    @Override
    public String read() throws IOException {
        try (InputStream is = getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            
            return reader.lines()
                .collect(Collectors.joining("\n"));
        }
    }
    
    private InputStream getInputStream() throws IOException {
        // Return stream to large data source
        return new FileInputStream("/path/to/large/file");
    }
}
```

#### Cached Resource

```java
@ApplicationScoped
public class CachedResource implements Resource {
    
    private volatile String cachedContent;
    private volatile long lastUpdate;
    private static final long CACHE_DURATION = 60_000; // 1 minute
    
    @Override
    public String read() throws IOException {
        long now = System.currentTimeMillis();
        
        if (cachedContent == null || (now - lastUpdate) > CACHE_DURATION) {
            synchronized (this) {
                if (cachedContent == null || (now - lastUpdate) > CACHE_DURATION) {
                    cachedContent = fetchFreshContent();
                    lastUpdate = now;
                }
            }
        }
        
        return cachedContent;
    }
    
    private String fetchFreshContent() throws IOException {
        // Fetch from slow source
        return "Fresh content";
    }
}
```

## Creating Custom Prompts

### Prompt Interface

```java
public interface Prompt {
    String getName();
    String getDescription();
    List<PromptArgument> getArguments();
    List<PromptMessage> render(Map<String, String> arguments);
}
```

### Step-by-Step Guide

#### 1. Create Prompt Class

```java
package com.example.prompts;

import dukes.mcp.model.PromptArgument;
import dukes.mcp.model.PromptMessage;
import dukes.mcp.service.Prompt;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class MyPrompt implements Prompt {
    
    @Override
    public String getName() {
        return "my_prompt";
    }
    
    @Override
    public String getDescription() {
        return "Description of what this prompt does";
    }
    
    @Override
    public List<PromptArgument> getArguments() {
        return List.of(
            new PromptArgument("param1", "Description of param1", true),
            new PromptArgument("param2", "Description of param2", false)
        );
    }
    
    @Override
    public List<PromptMessage> render(Map<String, String> arguments) {
        // Validate required arguments
        String param1 = arguments.get("param1");
        if (param1 == null || param1.trim().isEmpty()) {
            throw new IllegalArgumentException("Required argument 'param1' is missing");
        }
        
        // Get optional arguments
        String param2 = arguments.getOrDefault("param2", "default value");
        
        // Build prompt content
        String content = String.format(
            "Prompt template with %s and %s",
            param1, param2
        );
        
        return List.of(PromptMessage.user(content));
    }
}
```

#### 2. Register the Prompt

```java
@ApplicationScoped
public class PromptInitializer {
    
    @Inject
    private PromptManager promptManager;
    
    @Inject
    private MyPrompt myPrompt;
    
    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        promptManager.registerPrompt(myPrompt);
    }
}
```

### Advanced Prompt Patterns

#### Multi-Message Prompt

```java
@ApplicationScoped
public class ConversationPrompt implements Prompt {
    
    @Override
    public List<PromptMessage> render(Map<String, String> arguments) {
        String topic = arguments.get("topic");
        
        return List.of(
            PromptMessage.system("You are a helpful assistant."),
            PromptMessage.user("Tell me about " + topic),
            PromptMessage.assistant("I'd be happy to explain " + topic + "."),
            PromptMessage.user("Please provide more details.")
        );
    }
}
```

#### Template-Based Prompt

```java
@ApplicationScoped
public class TemplateProm implements Prompt {
    
    private static final String TEMPLATE = """
        Please analyze the following {data_type}:
        
        Goal: {goal}
        Context: {context}
        
        Provide:
        1. Summary of key findings
        2. Detailed analysis
        3. Recommendations
        """;
    
    @Override
    public List<PromptMessage> render(Map<String, String> arguments) {
        String content = TEMPLATE
            .replace("{data_type}", arguments.get("data_type"))
            .replace("{goal}", arguments.get("goal"))
            .replace("{context}", arguments.getOrDefault("context", "general"));
        
        return List.of(PromptMessage.user(content));
    }
}
```

## JSON Schema Guide

### Basic Types

```java
// String
Map.of("type", "string")

// Number
Map.of("type", "number")

// Integer
Map.of("type", "integer")

// Boolean
Map.of("type", "boolean")

// Array
Map.of("type", "array", "items", Map.of("type", "string"))

// Object
Map.of("type", "object")
```

### String Constraints

```java
Map.of(
    "type", "string",
    "minLength", 1,
    "maxLength", 100,
    "pattern", "^[a-zA-Z]+$",
    "format", "email" // or "uri", "date-time", etc.
)
```

### Number Constraints

```java
Map.of(
    "type", "number",
    "minimum", 0,
    "maximum", 100,
    "exclusiveMinimum", true,
    "multipleOf", 5
)
```

### Enum Values

```java
Map.of(
    "type", "string",
    "enum", List.of("option1", "option2", "option3")
)
```

### Complex Objects

```java
Map.of(
    "type", "object",
    "properties", Map.of(
        "name", Map.of("type", "string"),
        "age", Map.of("type", "integer", "minimum", 0),
        "email", Map.of("type", "string", "format", "email")
    ),
    "required", List.of("name", "email"),
    "additionalProperties", false
)
```

### Arrays with Constraints

```java
Map.of(
    "type", "array",
    "items", Map.of("type", "string"),
    "minItems", 1,
    "maxItems", 10,
    "uniqueItems", true
)
```

## Testing Your Components

### Unit Testing Tools

```java
@ExtendWith(MockitoExtension.class)
class MyToolTest {
    
    @InjectMocks
    private MyTool tool;
    
    @Mock
    private SomeDependency dependency;
    
    @Test
    void testExecuteSuccess() {
        // Arrange
        Map<String, Object> arguments = Map.of("param1", "value1");
        when(dependency.doSomething()).thenReturn("result");
        
        // Act
        ToolResult result = tool.execute(arguments);
        
        // Assert
        assertFalse(result.isError());
        assertEquals("expected result", result.getContent());
    }
    
    @Test
    void testExecuteError() {
        // Arrange
        Map<String, Object> arguments = Map.of("param1", "invalid");
        
        // Act
        ToolResult result = tool.execute(arguments);
        
        // Assert
        assertTrue(result.isError());
        assertNotNull(result.getContent());
    }
}
```

### Integration Testing

```java
@RunWith(Arquillian.class)
public class MyToolIntegrationTest {
    
    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
            .addClasses(MyTool.class, ToolRegistry.class)
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }
    
    @Inject
    private ToolRegistry toolRegistry;
    
    @Test
    public void testToolRegistration() {
        Optional<Tool> tool = toolRegistry.getTool("my_tool");
        assertTrue(tool.isPresent());
    }
}
```

## Best Practices

### Tool Development

1. **Naming**: Use lowercase with underscores (e.g., `my_tool`)
2. **Descriptions**: Be clear and concise about what the tool does
3. **Error Handling**: Always catch exceptions and return meaningful errors
4. **Validation**: Validate inputs even if schema validation is in place
5. **Logging**: Log important operations and errors
6. **Performance**: Consider timeouts for long-running operations
7. **Security**: Validate and sanitize all inputs

### Resource Development

1. **URI Schemes**: Use custom schemes (e.g., `custom://`, `app://`)
2. **MIME Types**: Use correct MIME types for content
3. **Error Handling**: Throw IOException with descriptive messages
4. **Caching**: Consider caching for expensive operations
5. **Size Limits**: Be mindful of resource size
6. **Security**: Don't expose sensitive information

### Prompt Development

1. **Arguments**: Clearly document required vs optional arguments
2. **Validation**: Validate all required arguments
3. **Templates**: Use clear, structured templates
4. **Flexibility**: Support optional parameters for customization
5. **Examples**: Provide usage examples in documentation

## Debugging

### Enable Debug Logging

```java
import java.util.logging.Logger;
import java.util.logging.Level;

public class MyTool implements Tool {
    private static final Logger LOGGER = Logger.getLogger(MyTool.class.getName());
    
    @Override
    public ToolResult execute(Map<String, Object> arguments) {
        LOGGER.log(Level.FINE, "Executing tool with arguments: {0}", arguments);
        
        try {
            // ... implementation
            LOGGER.log(Level.FINE, "Tool executed successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Tool execution failed", e);
        }
    }
}
```

### Remote Debugging

Start server with debug options:

```bash
# GlassFish/Payara
./bin/asadmin create-jvm-options "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=9009"

# WildFly
./bin/standalone.sh --debug 9009

# Open Liberty
export WLP_DEBUG_ADDRESS=9009
./bin/server debug mcpServer
```

Connect your IDE to port 9009.

### Testing with cURL

```bash
# Test tool execution
curl -X POST http://localhost:8080/dukes-mcp/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "1",
    "method": "tools/call",
    "params": {
      "name": "my_tool",
      "arguments": {"param1": "test"}
    }
  }' | jq .
```

---

This developer guide provides the foundation for extending the MCP Server. For more examples, see EXAMPLES.md.
