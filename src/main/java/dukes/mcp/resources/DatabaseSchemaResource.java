package dukes.mcp.resources;

import dukes.mcp.service.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Example resource that demonstrates database schema metadata access through MCP.
 * 
 * <p>This resource allows AI clients to read database schema information including
 * tables, columns, and their data types. It demonstrates integration with Jakarta EE
 * EntityManager and JDBC metadata APIs to provide structured schema information as JSON.</p>
 * 
 * <p>The resource queries database metadata and returns:
 * <ul>
 *   <li>Database product name and version</li>
 *   <li>List of tables with their types</li>
 *   <li>Column information for each table (name, type, size, nullable)</li>
 * </ul>
 * </p>
 * 
 * <p>Error handling:
 * <ul>
 *   <li>Database connection errors - throws IOException with descriptive message</li>
 *   <li>Metadata access errors - throws IOException with error details</li>
 * </ul>
 * </p>
 * 
 * <p><b>Validates: Requirements 8.1, 8.2, 9.1</b></p>
 */
@ApplicationScoped
public class DatabaseSchemaResource implements Resource {
    
    private static final String RESOURCE_URI = "db://schema";
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public String getUri() {
        return RESOURCE_URI;
    }
    
    @Override
    public String getName() {
        return "Database Schema";
    }
    
    @Override
    public String getDescription() {
        return "Database schema metadata including tables, columns, and data types. " +
               "Provides a complete overview of the database structure for analysis and understanding.";
    }
    
    @Override
    public String getMimeType() {
        return "application/json";
    }
    
    @Override
    public String read() throws IOException {
        try {
            // Get the underlying JDBC connection from EntityManager
            Connection connection = entityManager.unwrap(Connection.class);
            DatabaseMetaData metaData = connection.getMetaData();
            
            // Build JSON structure for schema information
            JsonObjectBuilder schemaBuilder = Json.createObjectBuilder();
            
            // Add database information
            schemaBuilder.add("databaseProductName", metaData.getDatabaseProductName());
            schemaBuilder.add("databaseProductVersion", metaData.getDatabaseProductVersion());
            schemaBuilder.add("driverName", metaData.getDriverName());
            schemaBuilder.add("driverVersion", metaData.getDriverVersion());
            
            // Get all tables
            JsonArrayBuilder tablesBuilder = Json.createArrayBuilder();
            
            try (ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE", "VIEW"})) {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    String tableType = tables.getString("TABLE_TYPE");
                    
                    JsonObjectBuilder tableBuilder = Json.createObjectBuilder();
                    tableBuilder.add("name", tableName);
                    tableBuilder.add("type", tableType);
                    
                    // Get columns for this table
                    JsonArrayBuilder columnsBuilder = Json.createArrayBuilder();
                    
                    try (ResultSet columns = metaData.getColumns(null, null, tableName, "%")) {
                        while (columns.next()) {
                            String columnName = columns.getString("COLUMN_NAME");
                            String columnType = columns.getString("TYPE_NAME");
                            int columnSize = columns.getInt("COLUMN_SIZE");
                            String nullable = columns.getString("IS_NULLABLE");
                            
                            JsonObjectBuilder columnBuilder = Json.createObjectBuilder();
                            columnBuilder.add("name", columnName);
                            columnBuilder.add("type", columnType);
                            columnBuilder.add("size", columnSize);
                            columnBuilder.add("nullable", "YES".equalsIgnoreCase(nullable));
                            
                            // Add default value if available
                            String defaultValue = columns.getString("COLUMN_DEF");
                            if (defaultValue != null) {
                                columnBuilder.add("defaultValue", defaultValue);
                            }
                            
                            columnsBuilder.add(columnBuilder);
                        }
                    }
                    
                    tableBuilder.add("columns", columnsBuilder);
                    tablesBuilder.add(tableBuilder);
                }
            }
            
            schemaBuilder.add("tables", tablesBuilder);
            
            // Convert to JSON string
            return schemaBuilder.build().toString();
            
        } catch (SQLException e) {
            throw new IOException("Failed to read database schema metadata: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new IOException("Unexpected error reading database schema: " + e.getMessage(), e);
        }
    }
}
