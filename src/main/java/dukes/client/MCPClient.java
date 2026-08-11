package dukes.client;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application-scoped CDI bean that acts as an MCP client against the
 * {@code dukes-mcp} server running at {@code http://localhost:8080/dukes-mcp/mcp}.
 *
 * <p>On startup the bean performs the two-step MCP handshake:</p>
 * <ol>
 *   <li>Sends an {@code initialize} request and records the server's
 *       protocol version and capabilities.</li>
 *   <li>Sends the {@code notifications/initialized} notification so the
 *       server transitions to its ready state.</li>
 * </ol>
 *
 * <p>After initialization callers can invoke the convenience methods
 * {@link #callSystemInfo} and {@link #callDatabaseQuery} which handle the
 * full JSON-RPC {@code tools/call} round-trip and return the plain-text
 * result produced by the server.</p>
 */
@ApplicationScoped
public class MCPClient {

    private static final Logger LOGGER = Logger.getLogger(MCPClient.class.getName());

    /** Base URL of the MCP server endpoint. */
    static final String MCP_URL = "http://localhost:8080/dukes-mcp/mcp";

    /** Monotonically increasing request-id counter. */
    private final AtomicInteger requestId = new AtomicInteger(1);

    private Client httpClient;

    // ------------------------------------------------------------------ //
    //  Lifecycle                                                           //
    // ------------------------------------------------------------------ //

    @PostConstruct
    void init() {
        httpClient = ClientBuilder.newClient();
        try {
            handshake();
        } catch (Exception e) {
            // Log but do not prevent deployment — the server may not be up yet
            LOGGER.log(Level.WARNING,
                    "MCP handshake failed at startup (server may not be running): {0}",
                    e.getMessage());
        }
    }

    @PreDestroy
    void destroy() {
        if (httpClient != null) {
            httpClient.close();
        }
    }

    // ------------------------------------------------------------------ //
    //  Public API                                                          //
    // ------------------------------------------------------------------ //

    /**
     * Calls the {@code system_info} tool on the MCP server.
     *
     * @param includeMemory     whether to include heap / non-heap memory details
     * @param includeProperties whether to include extra system properties
     * @return the text content returned by the tool, or an error message
     */
    public String callSystemInfo(boolean includeMemory, boolean includeProperties) {
        JsonObject args = Json.createObjectBuilder()
                .add("includeMemory", includeMemory)
                .add("includeProperties", includeProperties)
                .build();
        return toolsCall("system_info", args);
    }

    /**
     * Calls the {@code database_query} tool on the MCP server.
     *
     * @param query      the JPQL query to execute (read-only)
     * @param maxResults maximum number of rows to return (1–1000)
     * @return the text content returned by the tool, or an error message
     */
    public String callDatabaseQuery(String query, int maxResults) {
        JsonObject args = Json.createObjectBuilder()
                .add("query", query)
                .add("maxResults", maxResults)
                .build();
        return toolsCall("database_query", args);
    }

    // ------------------------------------------------------------------ //
    //  MCP handshake                                                       //
    // ------------------------------------------------------------------ //

    /**
     * Performs the MCP initialization handshake:
     * {@code initialize} request followed by the
     * {@code notifications/initialized} notification.
     */
    void handshake() {
        // Step 1 – initialize
        JsonObject initParams = Json.createObjectBuilder()
                .add("protocolVersion", "2024-11-05")
                .add("clientInfo", Json.createObjectBuilder()
                        .add("name", "dukes-mcp-client")
                        .add("version", "1.0.0"))
                .add("capabilities", Json.createObjectBuilder())
                .build();

        JsonObject initResponse = post(buildRequest("initialize", initParams));
        JsonValue initError = initResponse.get("error");
        if (initError != null && initError.getValueType() == JsonValue.ValueType.OBJECT) {
            LOGGER.log(Level.WARNING, "MCP initialize returned error: {0}", initError);
        } else {
            LOGGER.log(Level.INFO, "MCP server initialized. Protocol version: {0}",
                    initResponse.getJsonObject("result")
                                .getString("protocolVersion", "unknown"));
        }

        // Step 2 – notifications/initialized  (no id → notification, expects no response)
        JsonObject notification = Json.createObjectBuilder()
                .add("jsonrpc", "2.0")
                .add("method", "notifications/initialized")
                .build();

        try (Response r = httpClient.target(MCP_URL)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(notification.toString()))) {
            LOGGER.log(Level.FINE, "notifications/initialized HTTP status: {0}", r.getStatus());
        }
    }

    // ------------------------------------------------------------------ //
    //  Internal helpers                                                    //
    // ------------------------------------------------------------------ //

    /**
     * Sends a {@code tools/call} request and extracts the first text-content
     * item from the result, falling back to an error message if the call
     * failed.
     */
    private String toolsCall(String toolName, JsonObject arguments) {
        JsonObject params = Json.createObjectBuilder()
                .add("name", toolName)
                .add("arguments", arguments)
                .build();

        JsonObject response = post(buildRequest("tools/call", params));

        // The server serialises the error field as `false` when absent, so check
        // for a JsonObject (not just key presence) to detect a real error.
        JsonValue errorVal = response.get("error");
        if (errorVal != null && errorVal.getValueType() == JsonValue.ValueType.OBJECT) {
            JsonObject error = errorVal.asJsonObject();
            return "Error %d: %s".formatted(
                    error.getInt("code", 0),
                    error.getString("message", "unknown error"));
        }

        // result.content[0].text
        try {
            return response.getJsonObject("result")
                           .getJsonArray("content")
                           .getJsonObject(0)
                           .getString("text");
        } catch (Exception e) {
            return response.getJsonObject("result").toString();
        }
    }

    /**
     * Builds a JSON-RPC 2.0 request object with an auto-incremented id.
     */
    private JsonObject buildRequest(String method, JsonObject params) {
        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("jsonrpc", "2.0")
                .add("id", requestId.getAndIncrement())
                .add("method", method);
        if (params != null) {
            builder.add("params", params);
        }
        return builder.build();
    }

    /**
     * POSTs a JSON-RPC request to the MCP endpoint and returns the parsed
     * response body.
     */
    private JsonObject post(JsonObject request) {
        try (Response r = httpClient.target(MCP_URL)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(request.toString()))) {

            String body = r.readEntity(String.class);
            LOGGER.log(Level.FINE, "MCP response [{0}]: {1}", new Object[]{r.getStatus(), body});
            return Json.createReader(new java.io.StringReader(body)).readObject();
        }
    }
}
