package dukes.mcp;

import dukes.mcp.prompts.CodeReviewPrompt;
import dukes.mcp.prompts.DataAnalysisPrompt;
import dukes.mcp.resources.ConfigurationResource;
import dukes.mcp.resources.DatabaseSchemaResource;
import dukes.mcp.service.PromptManager;
import dukes.mcp.service.ResourceManager;
import dukes.mcp.service.ToolRegistry;
import dukes.mcp.tools.DatabaseQueryTool;
import dukes.mcp.tools.SystemInfoTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MCPServerInitializer}.
 * 
 * <p>These tests verify that the initializer correctly registers all example
 * tools, resources, and prompts when the ApplicationScoped context is initialized.</p>
 * 
 * <p><b>Validates: Requirements 6.2, 9.2, 12.2, 14.1, 14.2, 14.3, 14.4, 14.5</b></p>
 */
class MCPServerInitializerTest {
    
    private ToolRegistry toolRegistry;
    private ResourceManager resourceManager;
    private PromptManager promptManager;
    private DatabaseQueryTool databaseQueryTool;
    private SystemInfoTool systemInfoTool;
    private ConfigurationResource configurationResource;
    private DatabaseSchemaResource databaseSchemaResource;
    private CodeReviewPrompt codeReviewPrompt;
    private DataAnalysisPrompt dataAnalysisPrompt;
    private MCPServerInitializer initializer;
    
    @BeforeEach
    void setUp() {
        // Create real instances
        toolRegistry = new ToolRegistry();
        resourceManager = new ResourceManager();
        promptManager = new PromptManager();
        databaseQueryTool = new DatabaseQueryTool();
        systemInfoTool = new SystemInfoTool();
        configurationResource = new ConfigurationResource();
        databaseSchemaResource = new DatabaseSchemaResource();
        codeReviewPrompt = new CodeReviewPrompt();
        dataAnalysisPrompt = new DataAnalysisPrompt();
        
        // Create initializer with real dependencies
        initializer = new MCPServerInitializer();
        
        // Inject dependencies using reflection
        injectField(initializer, "toolRegistry", toolRegistry);
        injectField(initializer, "resourceManager", resourceManager);
        injectField(initializer, "promptManager", promptManager);
        injectField(initializer, "databaseQueryTool", databaseQueryTool);
        injectField(initializer, "systemInfoTool", systemInfoTool);
        injectField(initializer, "configurationResource", configurationResource);
        injectField(initializer, "databaseSchemaResource", databaseSchemaResource);
        injectField(initializer, "codeReviewPrompt", codeReviewPrompt);
        injectField(initializer, "dataAnalysisPrompt", dataAnalysisPrompt);
    }
    
    private void injectField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            fail("Failed to inject field " + fieldName + ": " + e.getMessage());
        }
    }
    
    /**
     * Tests that the initialize method registers all example tools.
     * 
     * <p><b>Validates: Requirements 6.2, 14.2, 14.5</b></p>
     */
    @Test
    void testInitializeRegistersAllTools() {
        // When
        initializer.initialize(new Object());
        
        // Then
        assertTrue(toolRegistry.getTool("database_query").isPresent());
        assertTrue(toolRegistry.getTool("system_info").isPresent());
        assertEquals(2, toolRegistry.listTools().size());
    }
    
    /**
     * Tests that the initialize method registers all example resources.
     * 
     * <p><b>Validates: Requirements 9.2, 14.3</b></p>
     */
    @Test
    void testInitializeRegistersAllResources() {
        // When
        initializer.initialize(new Object());
        
        // Then
        assertTrue(resourceManager.getResource("config://application.properties").isPresent());
        assertTrue(resourceManager.getResource("db://schema").isPresent());
        assertEquals(2, resourceManager.listResources().size());
    }
    
    /**
     * Tests that the initialize method registers all example prompts.
     * 
     * <p><b>Validates: Requirements 12.2, 14.4</b></p>
     */
    @Test
    void testInitializeRegistersAllPrompts() {
        // When
        initializer.initialize(new Object());
        
        // Then
        assertTrue(promptManager.getPrompt("code_review").isPresent());
        assertTrue(promptManager.getPrompt("data_analysis").isPresent());
        assertEquals(2, promptManager.listPrompts().size());
    }
}
