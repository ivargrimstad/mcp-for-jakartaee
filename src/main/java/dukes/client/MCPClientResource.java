package dukes.client;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

/**
 * JAX-RS resource that exposes the MCP server's tools over a simple REST API.
 *
 * <h2>Endpoints</h2>
 * <ul>
 *   <li>{@code GET /client/system-info} – invokes the {@code system_info} MCP tool</li>
 *   <li>{@code GET /client/database-query} – invokes the {@code database_query} MCP tool</li>
 * </ul>
 *
 * <h2>Example requests</h2>
 * <pre>
 * # Basic system info (memory included, extra properties excluded)
 * GET /client/system-info
 *
 * # Full system info with extra properties
 * GET /client/system-info?includeMemory=true&includeProperties=true
 *
 * # Database query
 * GET /client/database-query?query=SELECT+e+FROM+Employee+e&maxResults=10
 * </pre>
 */
@Path("/client")
@RequestScoped
@Produces(MediaType.TEXT_PLAIN)
public class MCPClientResource {

    @Inject
    private MCPClient mcpClient;

    /**
     * Invokes the {@code system_info} MCP tool and returns its output as plain text.
     *
     * @param includeMemory     include heap/non-heap memory details (default: {@code true})
     * @param includeProperties include extra system properties (default: {@code false})
     * @return plain-text output from the tool
     */
    @GET
    @Path("/system-info")
    public String systemInfo(
            @QueryParam("includeMemory") @DefaultValue("true") boolean includeMemory,
            @QueryParam("includeProperties") @DefaultValue("false") boolean includeProperties) {

        return mcpClient.callSystemInfo(includeMemory, includeProperties);
    }

    /**
     * Invokes the {@code database_query} MCP tool and returns its output as plain text.
     *
     * @param query      the JPQL query to execute (required)
     * @param maxResults maximum rows to return, 1–1000 (default: {@code 100})
     * @return plain-text output from the tool
     */
    @GET
    @Path("/database-query")
    public String databaseQuery(
            @QueryParam("query") String query,
            @QueryParam("maxResults") @DefaultValue("100") int maxResults) {

        if (query == null || query.isBlank()) {
            return "Missing required query parameter: ?query=<JPQL>";
        }
        return mcpClient.callDatabaseQuery(query, maxResults);
    }
}
