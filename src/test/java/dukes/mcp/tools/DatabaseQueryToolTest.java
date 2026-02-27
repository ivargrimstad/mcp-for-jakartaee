package dukes.mcp.tools;

import dukes.mcp.model.ToolResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DatabaseQueryTool.
 * 
 * <p>Tests tool metadata, input schema validation, and basic error handling.
 * Full integration tests with a real database are performed separately.</p>
 */
class DatabaseQueryToolTest {
    
    private DatabaseQueryTool tool;
    
    @BeforeEach
    void setUp() {
        tool = new DatabaseQueryTool();
    }
    
    @Test
    void testGetName() {
        assertEquals("database_query", tool.getName());
    }
    
    @Test
    void testGetDescription() {
        String description = tool.getDescription();
        assertNotNull(description);
        assertFalse(description.isEmpty());
        assertTrue(description.contains("database") || description.contains("query"));
    }
    
    @Test
    void testGetInputSchema() {
        Object schema = tool.getInputSchema();
        assertNotNull(schema);
        assertTrue(schema instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> schemaMap = (Map<String, Object>) schema;
        
        assertEquals("object", schemaMap.get("type"));
        assertTrue(schemaMap.containsKey("properties"));
        assertTrue(schemaMap.containsKey("required"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) schemaMap.get("properties");
        assertTrue(properties.containsKey("query"));
        assertTrue(properties.containsKey("maxResults"));
    }
    
    @Test
    void testExecuteRejectsUpdateQuery() {
        // Arrange
        String updateQuery = "UPDATE User u SET u.name = 'test'";
        Map<String, Object> arguments = Map.of("query", updateQuery);
        
        // Act
        ToolResult result = tool.execute(arguments);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getIsError());
        
        String errorMessage = result.getContent().get(0).getText();
        assertTrue(errorMessage.contains("read-only") || errorMessage.contains("not allowed"));
    }
    
    @Test
    void testExecuteRejectsDeleteQuery() {
        // Arrange
        String deleteQuery = "DELETE FROM User u WHERE u.id = 1";
        Map<String, Object> arguments = Map.of("query", deleteQuery);
        
        // Act
        ToolResult result = tool.execute(arguments);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getIsError());
        
        String errorMessage = result.getContent().get(0).getText();
        assertTrue(errorMessage.contains("read-only") || errorMessage.contains("not allowed"));
    }
    
    @Test
    void testExecuteRejectsInsertQuery() {
        // Arrange
        String insertQuery = "INSERT INTO User (name) VALUES ('test')";
        Map<String, Object> arguments = Map.of("query", insertQuery);
        
        // Act
        ToolResult result = tool.execute(arguments);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getIsError());
        
        String errorMessage = result.getContent().get(0).getText();
        assertTrue(errorMessage.contains("read-only") || errorMessage.contains("not allowed"));
    }
    
    @Test
    void testExecuteRejectsDropQuery() {
        // Arrange
        String dropQuery = "DROP TABLE User";
        Map<String, Object> arguments = Map.of("query", dropQuery);
        
        // Act
        ToolResult result = tool.execute(arguments);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getIsError());
    }
    
    @Test
    void testExecuteHandlesNullQuery() {
        // Arrange
        Map<String, Object> arguments = Map.of();
        
        // Act
        ToolResult result = tool.execute(arguments);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getIsError());
        
        String errorMessage = result.getContent().get(0).getText();
        assertTrue(errorMessage.contains("required") || errorMessage.contains("missing"));
    }
    
    @Test
    void testExecuteHandlesEmptyQuery() {
        // Arrange
        Map<String, Object> arguments = Map.of("query", "   ");
        
        // Act
        ToolResult result = tool.execute(arguments);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getIsError());
        
        String errorMessage = result.getContent().get(0).getText();
        assertTrue(errorMessage.contains("empty") || errorMessage.contains("blank"));
    }
    
    @Test
    void testExecuteWithoutEntityManagerReturnsError() {
        // Arrange - tool without injected EntityManager
        String jpqlQuery = "SELECT u FROM User u";
        Map<String, Object> arguments = Map.of("query", jpqlQuery);
        
        // Act
        ToolResult result = tool.execute(arguments);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getIsError());
        
        String errorMessage = result.getContent().get(0).getText();
        assertTrue(errorMessage.contains("EntityManager") || 
                   errorMessage.contains("not available") ||
                   errorMessage.contains("failed"));
    }
}
