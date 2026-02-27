package dukes.mcp.tools;

import dukes.mcp.model.ToolResult;
import dukes.mcp.service.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import java.util.List;
import java.util.Map;

/**
 * Example tool that demonstrates database query execution through MCP.
 * 
 * <p>This tool allows AI clients to execute read-only database queries using JPQL.
 * It demonstrates proper integration with Jakarta EE EntityManager, JSON Schema
 * definition for input validation, and graceful error handling for SQL exceptions.</p>
 * 
 * <p>Security Note: In production, this tool should be restricted to read-only
 * queries and implement proper authorization checks.</p>
 * 
 * <p><b>Validates: Requirements 5.1, 5.2, 6.1, 14.5, 16.1</b></p>
 */
@ApplicationScoped
public class DatabaseQueryTool implements Tool {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public String getName() {
        return "database_query";
    }
    
    @Override
    public String getDescription() {
        return "Execute a read-only database query using JPQL. Returns query results as a formatted string. " +
               "Use this tool to retrieve data from the database for analysis or reporting.";
    }
    
    @Override
    public Object getInputSchema() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "query", Map.of(
                    "type", "string",
                    "description", "The JPQL query to execute (read-only queries only)",
                    "minLength", 1
                ),
                "maxResults", Map.of(
                    "type", "integer",
                    "description", "Maximum number of results to return (optional, default: 100)",
                    "minimum", 1,
                    "maximum", 1000,
                    "default", 100
                )
            ),
            "required", List.of("query"),
            "additionalProperties", false
        );
    }
    
    @Override
    public ToolResult execute(Map<String, Object> arguments) {
        // Extract query parameter
        String query = (String) arguments.get("query");
        
        // Validate query is not null or empty (should be caught by schema validation)
        if (query == null || query.trim().isEmpty()) {
            return ToolResult.error("Query parameter is required and cannot be empty");
        }
        
        // Extract optional maxResults parameter
        Integer maxResults = 100; // default value
        if (arguments.containsKey("maxResults")) {
            Object maxResultsObj = arguments.get("maxResults");
            if (maxResultsObj instanceof Number) {
                maxResults = ((Number) maxResultsObj).intValue();
            }
        }
        
        // Validate query is read-only (basic check)
        String normalizedQuery = query.trim().toUpperCase();
        if (normalizedQuery.startsWith("UPDATE") || 
            normalizedQuery.startsWith("DELETE") || 
            normalizedQuery.startsWith("INSERT") ||
            normalizedQuery.startsWith("DROP") ||
            normalizedQuery.startsWith("CREATE") ||
            normalizedQuery.startsWith("ALTER")) {
            return ToolResult.error("Only read-only queries (SELECT) are allowed");
        }
        
        try {
            // Execute the query
            List<?> results = entityManager.createQuery(query)
                .setMaxResults(maxResults)
                .getResultList();
            
            // Format results
            if (results.isEmpty()) {
                return ToolResult.success("Query executed successfully. No results found.");
            }
            
            StringBuilder output = new StringBuilder();
            output.append("Query executed successfully. Found ")
                  .append(results.size())
                  .append(" result(s):\n\n");
            
            // Format each result
            for (int i = 0; i < results.size(); i++) {
                Object result = results.get(i);
                output.append(i + 1).append(". ");
                
                if (result == null) {
                    output.append("null");
                } else if (result instanceof Object[]) {
                    // Handle multi-column results
                    Object[] row = (Object[]) result;
                    output.append("[");
                    for (int j = 0; j < row.length; j++) {
                        if (j > 0) output.append(", ");
                        output.append(formatValue(row[j]));
                    }
                    output.append("]");
                } else {
                    // Handle single-column or entity results
                    output.append(formatValue(result));
                }
                output.append("\n");
            }
            
            // Add truncation notice if results were limited
            if (results.size() == maxResults) {
                output.append("\n(Results limited to ").append(maxResults).append(" rows)");
            }
            
            return ToolResult.success(output.toString());
            
        } catch (PersistenceException e) {
            // Handle SQL/JPA exceptions gracefully
            String errorMessage = "Database query failed: " + e.getMessage();
            
            // Extract more specific error information if available
            Throwable cause = e.getCause();
            if (cause != null && cause.getMessage() != null) {
                errorMessage = "Database query failed: " + cause.getMessage();
            }
            
            return ToolResult.error(errorMessage);
            
        } catch (IllegalArgumentException e) {
            // Handle invalid JPQL syntax
            return ToolResult.error("Invalid query syntax: " + e.getMessage());
            
        } catch (Exception e) {
            // Handle any other unexpected exceptions
            return ToolResult.error("Unexpected error executing query: " + e.getMessage());
        }
    }
    
    /**
     * Formats a single value for display in the results.
     * 
     * @param value the value to format
     * @return formatted string representation
     */
    private String formatValue(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof String) {
            return "\"" + value + "\"";
        } else {
            return value.toString();
        }
    }
}
