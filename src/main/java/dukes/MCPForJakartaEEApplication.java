package dukes;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS application entry point.
 *
 * <p>The application is rooted at "/" so that the MCP endpoint is reachable at
 * the context-root path directly, e.g. {@code http://localhost:8080/dukes-mcp/mcp}.
 * Using "" and "/" are equivalent on all compliant Jakarta EE servers.</p>
 */
@ApplicationPath("/")
public class MCPForJakartaEEApplication extends Application {
}