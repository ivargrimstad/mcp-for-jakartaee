# MCP Server for Jakarta EE

A Model Context Protocol (MCP) server implementation for Jakarta EE 11 applications. This server enables AI models and assistants to interact with Jakarta EE applications through a standardized JSON-RPC 2.0 based protocol, exposing capabilities for tool execution, resource access, and prompt management.

## Features

- **MCP Protocol Compliance**: Full implementation of the Model Context Protocol specification
- **JSON-RPC 2.0**: Standards-compliant JSON-RPC communication
- **Tool Execution**: Register and execute custom tools with JSON Schema validation
- **Resource Access**: Expose application data and configurations to AI clients
- **Prompt Management**: Define and serve parameterized prompt templates
- **Multi-Server Support**: Compatible with GlassFish, Payara, WildFly, and Open Liberty
- **Jakarta EE 11**: Built on modern Jakarta EE 11 APIs (JAX-RS 4.0, CDI 4.1, JSON-B 3.0)
- **Thread-Safe**: Concurrent request handling with proper isolation
- **Extensible**: Easy-to-use APIs for creating custom tools, resources, and prompts

## Requirements

- Java 23 or later (Java 25 for GlassFish)
- Maven 3.8 or later
- One of the supported Jakarta EE servers:
  - GlassFish 8.0.0 or later
  - Payara 7.2025.2 or later
  - WildFly 39.0.1.Final or later
  - Open Liberty 26.0.0.3-beta or later

## Quick Start

### 1. Clone and Build

```bash
git clone <repository-url>
cd mcp-for-jakartaee
mvn clean package
```

### 2. Run on Your Preferred Server

#### GlassFish
```bash
mvn clean package cargo:run -Pglassfish
```

#### Payara
```bash
mvn clean package cargo:run -Ppayara
```

#### WildFly
```bash
mvn clean package wildfly:run
```

#### Open Liberty
```bash
mvn clean package liberty:run
```

### 3. Test the Server

The MCP endpoint is available at: `http://localhost:8080/dukes-mcp/mcp`

Send a test request:

```bash
curl -X POST http://localhost:8080/dukes-mcp/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "1",
    "method": "initialize",
    "params": {
      "protocolVersion": "1.0",
      "capabilities": {}
    }
  }'
```

## Architecture

The MCP Server follows a layered architecture:

```
┌─────────────────────────────────────┐
│      AI Client (MCP Client)         │
└─────────────────┬───────────────────┘
                  │ HTTP/JSON-RPC
┌─────────────────▼───────────────────┐
│      JAX-RS Endpoint (/mcp)         │
└─────────────────┬───────────────────┘
                  │
┌─────────────────▼───────────────────┐
│      MCP Protocol Handler           │
└─────┬───────────┬───────────┬───────┘
      │           │           │
┌─────▼─────┐ ┌──▼──────┐ ┌──▼──────────┐
│   Tool    │ │Resource │ │   Prompt    │
│ Registry  │ │ Manager │ │   Manager   │
└─────┬─────┘ └──┬──────┘ └──┬──────────┘
      │          │            │
┌─────▼──────────▼────────────▼─────┐
│    Jakarta EE Services (CDI,      │
│    JPA, JSON-B, etc.)              │
└────────────────────────────────────┘
```

## Core Concepts

### Tools

Tools are callable functions that AI clients can execute. Each tool has:
- A unique name
- A description
- A JSON Schema defining input parameters
- An execute method that performs the operation

### Resources

Resources are data entities (files, configurations, database records) that AI clients can read. Each resource has:
- A unique URI
- A name and description
- A MIME type
- A read method that returns the content

### Prompts

Prompts are parameterized templates that AI clients can retrieve and use. Each prompt has:
- A unique name
- A description
- A list of required and optional arguments
- A render method that substitutes argument values

## Creating Custom Tools

### 1. Implement the Tool Interface

```java
package com.example.tools;

import dukes.mcp.model.ToolResult;
import dukes.mcp.service.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class MyCustomTool implements Tool {
    
    @Override
    public String getName() {
        return "my_custom_tool";
    }
    
    @Override
    public String getDescription() {
        return "Description of what this tool does";
    }
    
    @Override
    public Object getInputSchema() {
        // Define JSON Schema for input validation
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "param1", Map.of(
                    "type", "string",
                    "description", "First parameter"
                ),
                "param2", Map.of(
                    "type", "integer",
                    "description", "Second parameter",
                    "minimum", 1
                )
            ),
            "required", List.of("param1"),
            "additionalProperties", false
        );
    }
    
    @Override
    public ToolResult execute(Map<String, Object> arguments) {
        try {
            // Extract parameters
            String param1 = (String) arguments.get("param1");
            Integer param2 = arguments.containsKey("param2") 
                ? ((Number) arguments.get("param2")).intValue() 
                : 10; // default value
            
            // Perform your operation
            String result = performOperation(param1, param2);
            
            // Return success result
            return ToolResult.success(result);
            
        } catch (Exception e) {
            // Return error result
            return ToolResult.error("Operation failed: " + e.getMessage());
        }
    }
    
    private String performOperation(String param1, int param2) {
        // Your implementation here
        return "Result: " + param1 + " x " + param2;
    }
}
```

### 2. Register the Tool

Tools are automatically registered if they are CDI beans. Alternatively, register manually:

```java
@ApplicationScoped
public class MyInitializer {
    
    @Inject
    private ToolRegistry toolRegistry;
    
    @Inject
    private MyCustomTool myCustomTool;
    
    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        toolRegistry.registerTool(myCustomTool);
    }
}
```

### 3. Use the Tool

AI clients can now discover and execute your tool:

```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "method": "tools/call",
  "params": {
    "name": "my_custom_tool",
    "arguments": {
      "param1": "test",
      "param2": 42
    }
  }
}
```

## Creating Custom Resources

### 1. Implement the Resource Interface

```java
package com.example.resources;

import dukes.mcp.service.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;

@ApplicationScoped
public class MyCustomResource implements Resource {
    
    @Override
    public String getUri() {
        return "custom://my-resource";
    }
    
    @Override
    public String getName() {
        return "My Custom Resource";
    }
    
    @Override
    public String getDescription() {
        return "Description of what this resource provides";
    }
    
    @Override
    public String getMimeType() {
        return "application/json"; // or "text/plain", etc.
    }
    
    @Override
    public String read() throws IOException {
        try {
            // Fetch your data
            String data = fetchData();
            return data;
            
        } catch (Exception e) {
            throw new IOException("Failed to read resource: " + e.getMessage(), e);
        }
    }
    
    private String fetchData() {
        // Your implementation here
        return "{\"status\": \"ok\", \"data\": \"example\"}";
    }
}
```

### 2. Register the Resource

```java
@ApplicationScoped
public class MyInitializer {
    
    @Inject
    private ResourceManager resourceManager;
    
    @Inject
    private MyCustomResource myCustomResource;
    
    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        resourceManager.registerResource(myCustomResource);
    }
}
```

### 3. Access the Resource

```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "method": "resources/read",
  "params": {
    "uri": "custom://my-resource"
  }
}
```

## Creating Custom Prompts

### 1. Implement the Prompt Interface

```java
package com.example.prompts;

import dukes.mcp.model.PromptArgument;
import dukes.mcp.model.PromptMessage;
import dukes.mcp.service.Prompt;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class MyCustomPrompt implements Prompt {
    
    @Override
    public String getName() {
        return "my_custom_prompt";
    }
    
    @Override
    public String getDescription() {
        return "Description of what this prompt does";
    }
    
    @Override
    public List<PromptArgument> getArguments() {
        return List.of(
            new PromptArgument("topic", "The topic to discuss", true),
            new PromptArgument("style", "The writing style (optional)", false)
        );
    }
    
    @Override
    public List<PromptMessage> render(Map<String, String> arguments) {
        // Validate required arguments
        String topic = arguments.get("topic");
        if (topic == null || topic.trim().isEmpty()) {
            throw new IllegalArgumentException("Required argument 'topic' is missing");
        }
        
        // Get optional arguments
        String style = arguments.getOrDefault("style", "professional");
        
        // Build prompt content
        String content = String.format(
            "Please write about %s in a %s style. " +
            "Include relevant examples and explain key concepts clearly.",
            topic, style
        );
        
        return List.of(PromptMessage.user(content));
    }
}
```

### 2. Register the Prompt

```java
@ApplicationScoped
public class MyInitializer {
    
    @Inject
    private PromptManager promptManager;
    
    @Inject
    private MyCustomPrompt myCustomPrompt;
    
    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        promptManager.registerPrompt(myCustomPrompt);
    }
}
```

### 3. Use the Prompt

```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "method": "prompts/get",
  "params": {
    "name": "my_custom_prompt",
    "arguments": {
      "topic": "Jakarta EE",
      "style": "beginner-friendly"
    }
  }
}
```

## Example Client Code

### Java Client Example

```java
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class MCPClient {
    
    private static final String MCP_ENDPOINT = "http://localhost:8080/dukes-mcp/mcp";
    private final Client client;
    
    public MCPClient() {
        this.client = ClientBuilder.newClient();
    }
    
    public JsonObject initialize() {
        JsonObject request = Json.createObjectBuilder()
            .add("jsonrpc", "2.0")
            .add("id", "1")
            .add("method", "initialize")
            .add("params", Json.createObjectBuilder()
                .add("protocolVersion", "1.0")
                .add("capabilities", Json.createObjectBuilder()))
            .build();
        
        return sendRequest(request);
    }
    
    public JsonObject listTools() {
        JsonObject request = Json.createObjectBuilder()
            .add("jsonrpc", "2.0")
            .add("id", "2")
            .add("method", "tools/list")
            .build();
        
        return sendRequest(request);
    }
    
    public JsonObject callTool(String toolName, JsonObject arguments) {
        JsonObject request = Json.createObjectBuilder()
            .add("jsonrpc", "2.0")
            .add("id", "3")
            .add("method", "tools/call")
            .add("params", Json.createObjectBuilder()
                .add("name", toolName)
                .add("arguments", arguments))
            .build();
        
        return sendRequest(request);
    }
    
    private JsonObject sendRequest(JsonObject request) {
        try (Response response = client.target(MCP_ENDPOINT)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(request.toString()))) {
            
            return response.readEntity(JsonObject.class);
        }
    }
    
    public void close() {
        client.close();
    }
    
    public static void main(String[] args) {
        MCPClient client = new MCPClient();
        
        try {
            // Initialize
            System.out.println("Initializing...");
            JsonObject initResponse = client.initialize();
            System.out.println(initResponse);
            
            // List tools
            System.out.println("\nListing tools...");
            JsonObject toolsResponse = client.listTools();
            System.out.println(toolsResponse);
            
            // Call system_info tool
            System.out.println("\nCalling system_info tool...");
            JsonObject args = Json.createObjectBuilder()
                .add("includeMemory", true)
                .add("includeProperties", false)
                .build();
            JsonObject toolResponse = client.callTool("system_info", args);
            System.out.println(toolResponse);
            
        } finally {
            client.close();
        }
    }
}
```

### Python Client Example

```python
import requests
import json

class MCPClient:
    def __init__(self, endpoint="http://localhost:8080/dukes-mcp/mcp"):
        self.endpoint = endpoint
        self.session = requests.Session()
        self.request_id = 0
    
    def _send_request(self, method, params=None):
        self.request_id += 1
        request = {
            "jsonrpc": "2.0",
            "id": str(self.request_id),
            "method": method
        }
        if params:
            request["params"] = params
        
        response = self.session.post(
            self.endpoint,
            json=request,
            headers={"Content-Type": "application/json"}
        )
        return response.json()
    
    def initialize(self):
        return self._send_request("initialize", {
            "protocolVersion": "1.0",
            "capabilities": {}
        })
    
    def list_tools(self):
        return self._send_request("tools/list")
    
    def call_tool(self, tool_name, arguments):
        return self._send_request("tools/call", {
            "name": tool_name,
            "arguments": arguments
        })
    
    def list_resources(self):
        return self._send_request("resources/list")
    
    def read_resource(self, uri):
        return self._send_request("resources/read", {
            "uri": uri
        })
    
    def list_prompts(self):
        return self._send_request("prompts/list")
    
    def get_prompt(self, prompt_name, arguments):
        return self._send_request("prompts/get", {
            "name": prompt_name,
            "arguments": arguments
        })

# Example usage
if __name__ == "__main__":
    client = MCPClient()
    
    # Initialize
    print("Initializing...")
    init_response = client.initialize()
    print(json.dumps(init_response, indent=2))
    
    # List and call tools
    print("\nListing tools...")
    tools_response = client.list_tools()
    print(json.dumps(tools_response, indent=2))
    
    print("\nCalling system_info tool...")
    tool_response = client.call_tool("system_info", {
        "includeMemory": True,
        "includeProperties": False
    })
    print(json.dumps(tool_response, indent=2))
    
    # List and read resources
    print("\nListing resources...")
    resources_response = client.list_resources()
    print(json.dumps(resources_response, indent=2))
    
    # List and get prompts
    print("\nListing prompts...")
    prompts_response = client.list_prompts()
    print(json.dumps(prompts_response, indent=2))
```

### cURL Examples

```bash
# Initialize the server
curl -X POST http://localhost:8080/dukes-mcp/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "1",
    "method": "initialize",
    "params": {
      "protocolVersion": "1.0",
      "capabilities": {}
    }
  }'

# List available tools
curl -X POST http://localhost:8080/dukes-mcp/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "2",
    "method": "tools/list"
  }'

# Call the system_info tool
curl -X POST http://localhost:8080/dukes-mcp/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "3",
    "method": "tools/call",
    "params": {
      "name": "system_info",
      "arguments": {
        "includeMemory": true,
        "includeProperties": false
      }
    }
  }'

# List available resources
curl -X POST http://localhost:8080/dukes-mcp/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "4",
    "method": "resources/list"
  }'

# Read a resource
curl -X POST http://localhost:8080/dukes-mcp/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "5",
    "method": "resources/read",
    "params": {
      "uri": "config://application.properties"
    }
  }'

# List available prompts
curl -X POST http://localhost:8080/dukes-mcp/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "6",
    "method": "prompts/list"
  }'

# Get a prompt
curl -X POST http://localhost:8080/dukes-mcp/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "7",
    "method": "prompts/get",
    "params": {
      "name": "code_review",
      "arguments": {
        "language": "Java",
        "context": "security and performance"
      }
    }
  }'
```

## Deployment Instructions

### GlassFish 8.0.0

1. **Download and Install GlassFish**
   ```bash
   wget https://github.com/eclipse-ee4j/glassfish/releases/download/8.0.0/glassfish-8.0.0.zip
   unzip glassfish-8.0.0.zip
   ```

2. **Build the Application**
   ```bash
   mvn clean package -Pglassfish
   ```

3. **Deploy**
   ```bash
   # Using Cargo (automatic)
   mvn cargo:run -Pglassfish
   
   # Or manually
   ./glassfish8/bin/asadmin start-domain
   ./glassfish8/bin/asadmin deploy target/dukes-mcp.war
   ```

4. **Access**
   - Application: http://localhost:8080/dukes-mcp/
   - MCP Endpoint: http://localhost:8080/dukes-mcp/mcp
   - Admin Console: http://localhost:4848

5. **Undeploy**
   ```bash
   ./glassfish8/bin/asadmin undeploy dukes-mcp
   ./glassfish8/bin/asadmin stop-domain
   ```

### Payara 7.2025.2

1. **Download and Install Payara**
   ```bash
   wget https://repo1.maven.org/maven2/fish/payara/distributions/payara/7.2025.2/payara-7.2025.2.zip
   unzip payara-7.2025.2.zip
   ```

2. **Build the Application**
   ```bash
   mvn clean package -Ppayara
   ```

3. **Deploy**
   ```bash
   # Using Cargo (automatic)
   mvn cargo:run -Ppayara
   
   # Or manually
   ./payara7/bin/asadmin start-domain
   ./payara7/bin/asadmin deploy target/dukes-mcp.war
   ```

4. **Access**
   - Application: http://localhost:8080/dukes-mcp/
   - MCP Endpoint: http://localhost:8080/dukes-mcp/mcp
   - Admin Console: http://localhost:4848

5. **Undeploy**
   ```bash
   ./payara7/bin/asadmin undeploy dukes-mcp
   ./payara7/bin/asadmin stop-domain
   ```

### WildFly 39.0.1.Final

1. **Build and Run**
   ```bash
   mvn clean package wildfly:run
   ```
   
   This will automatically download WildFly and start the server with the application deployed.

2. **Access**
   - Application: http://localhost:8080/dukes-mcp/
   - MCP Endpoint: http://localhost:8080/dukes-mcp/mcp
   - Admin Console: http://localhost:9990

3. **Manual Deployment** (if WildFly is already installed)
   ```bash
   # Start WildFly
   ./wildfly-39.0.1.Final/bin/standalone.sh
   
   # Deploy
   cp target/dukes-mcp.war ./wildfly-39.0.1.Final/standalone/deployments/
   ```

4. **Stop**
   ```bash
   # If using wildfly:run, press Ctrl+C
   
   # If running standalone
   ./wildfly-39.0.1.Final/bin/jboss-cli.sh --connect command=:shutdown
   ```

### Open Liberty 26.0.0.3-beta

1. **Build and Run**
   ```bash
   mvn clean package liberty:run
   ```
   
   This will automatically download Open Liberty and start the server with the application deployed.

2. **Access**
   - Application: http://localhost:9080/dukes-mcp/
   - MCP Endpoint: http://localhost:9080/dukes-mcp/mcp

3. **Configuration**
   
   The server configuration is in `src/main/liberty/config/server.xml` (if you need to customize it).

4. **Stop**
   ```bash
   # Press Ctrl+C in the terminal where liberty:run is running
   
   # Or use
   mvn liberty:stop
   ```

## Built-in Examples

The server includes several example implementations:

### Tools

1. **database_query** - Execute read-only JPQL queries
   - Parameters: `query` (string, required), `maxResults` (integer, optional)
   - Example: Query database entities for analysis

2. **system_info** - Retrieve system information
   - Parameters: `includeMemory` (boolean, optional), `includeProperties` (boolean, optional)
   - Example: Get Java version, OS details, memory usage

### Resources

1. **config://application.properties** - Application configuration file
   - MIME Type: text/plain
   - Content: Application properties

2. **db://schema** - Database schema metadata
   - MIME Type: application/json
   - Content: Tables, columns, and data types

### Prompts

1. **code_review** - Code review prompt template
   - Parameters: `language` (required), `context` (optional)
   - Example: Generate code review requests

2. **data_analysis** - Data analysis prompt template
   - Parameters: `data_type` (required), `analysis_goal` (required)
   - Example: Structure data analysis requests

## Testing

### Run Unit Tests

```bash
mvn test
```

### Run Integration Tests

```bash
mvn verify
```

### Run Property-Based Tests

Property-based tests are included in the test suite and run automatically with `mvn test`.

## Configuration

### Database Configuration

If using the `database_query` tool or `DatabaseSchemaResource`, configure your database in `persistence.xml`:

```xml
<persistence-unit name="default">
    <jta-data-source>java:comp/DefaultDataSource</jta-data-source>
    <!-- Your entity classes -->
</persistence-unit>
```

### Application Properties

Create `src/main/resources/application.properties` for custom configuration:

```properties
# MCP Server Configuration
mcp.server.name=My MCP Server
mcp.server.version=1.0.0

# Add your custom properties here
```

## Security Considerations

⚠️ **Important**: This is a reference implementation. For production use, consider:

1. **Authentication**: Implement Jakarta Security to authenticate MCP clients
2. **Authorization**: Use role-based access control for tools and resources
3. **Input Validation**: Strictly validate all tool arguments and resource URIs
4. **Rate Limiting**: Implement rate limiting to prevent abuse
5. **HTTPS**: Enforce HTTPS for all MCP endpoints
6. **Audit Logging**: Log all tool executions and resource accesses
7. **Tool Sandboxing**: Isolate tool execution to prevent unauthorized access

Example security configuration:

```java
@ApplicationScoped
public class SecureToolRegistry extends ToolRegistry {
    
    @Inject
    private SecurityContext securityContext;
    
    @Override
    public ToolResult executeTool(String toolName, Map<String, Object> arguments) {
        // Check authentication
        if (!securityContext.isAuthenticated()) {
            return ToolResult.error("Authentication required");
        }
        
        // Check authorization
        if (!securityContext.isUserInRole("MCP_USER")) {
            return ToolResult.error("Insufficient permissions");
        }
        
        // Log the execution
        auditLog.log("Tool executed: " + toolName + " by " + securityContext.getUserPrincipal());
        
        return super.executeTool(toolName, arguments);
    }
}
```

## Troubleshooting

### Common Issues

1. **Port Already in Use**
   - Change the port in your server configuration
   - Or stop the process using the port

2. **ClassNotFoundException**
   - Ensure all dependencies are in `pom.xml`
   - Run `mvn clean install` to rebuild

3. **JSON-RPC Errors**
   - Verify request format matches JSON-RPC 2.0 specification
   - Check that `jsonrpc` field is "2.0"
   - Ensure `method` field is present

4. **Tool Execution Failures**
   - Check tool arguments match the input schema
   - Review server logs for detailed error messages
   - Verify CDI beans are properly injected

## Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## License

See LICENSE file for details.

## Resources

- [Model Context Protocol Specification](https://spec.modelcontextprotocol.io/)
- [Jakarta EE 11 Documentation](https://jakarta.ee/specifications/platform/11/)
- [JSON-RPC 2.0 Specification](https://www.jsonrpc.org/specification)
- [JSON Schema](https://json-schema.org/)

## Support

For issues and questions:
- Open an issue on GitHub
- Check existing documentation
- Review the example implementations

---

**Built with Jakarta EE 11 | Compatible with GlassFish, Payara, WildFly, and Open Liberty**
