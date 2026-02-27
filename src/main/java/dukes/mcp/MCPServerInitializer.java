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
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.util.logging.Logger;

/**
 * CDI initialization bean that automatically registers all example tools, resources,
 * and prompts when the application starts.
 * 
 * <p>This bean observes the {@link ApplicationScoped} initialization event and uses
 * CDI injection to obtain instances of the registries/managers and example implementations.
 * It then registers all examples, making them immediately available through the MCP
 * protocol endpoints.</p>
 * 
 * <p>The initialization process:</p>
 * <ol>
 *   <li>CDI container starts and creates ApplicationScoped beans</li>
 *   <li>This bean observes the initialization event</li>
 *   <li>Example tools, resources, and prompts are injected</li>
 *   <li>All examples are registered with their respective managers</li>
 *   <li>MCP server is ready to accept requests</li>
 * </ol>
 * 
 * <p>Registered examples:</p>
 * <ul>
 *   <li><b>Tools:</b> DatabaseQueryTool, SystemInfoTool</li>
 *   <li><b>Resources:</b> ConfigurationResource, DatabaseSchemaResource</li>
 *   <li><b>Prompts:</b> CodeReviewPrompt, DataAnalysisPrompt</li>
 * </ul>
 * 
 * <p><b>Validates: Requirements 6.2, 9.2, 12.2, 14.1, 14.2, 14.3, 14.4, 14.5</b></p>
 * 
 * @see ToolRegistry
 * @see ResourceManager
 * @see PromptManager
 * @see DatabaseQueryTool
 * @see SystemInfoTool
 * @see ConfigurationResource
 * @see DatabaseSchemaResource
 * @see CodeReviewPrompt
 * @see DataAnalysisPrompt
 */
@ApplicationScoped
public class MCPServerInitializer {
    
    private static final Logger LOGGER = Logger.getLogger(MCPServerInitializer.class.getName());
    
    @Inject
    private ToolRegistry toolRegistry;
    
    @Inject
    private ResourceManager resourceManager;
    
    @Inject
    private PromptManager promptManager;
    
    // Example tool implementations
    @Inject
    private DatabaseQueryTool databaseQueryTool;
    
    @Inject
    private SystemInfoTool systemInfoTool;
    
    // Example resource implementations
    @Inject
    private ConfigurationResource configurationResource;
    
    @Inject
    private DatabaseSchemaResource databaseSchemaResource;
    
    // Example prompt implementations
    @Inject
    private CodeReviewPrompt codeReviewPrompt;
    
    @Inject
    private DataAnalysisPrompt dataAnalysisPrompt;
    
    /**
     * Initializes the MCP server by registering all example tools, resources, and prompts.
     * 
     * <p>This method is automatically invoked by the CDI container when the ApplicationScoped
     * context is initialized. It registers all injected example implementations with their
     * respective managers, making them available through the MCP protocol.</p>
     * 
     * <p>The method logs the registration of each component for debugging and monitoring
     * purposes. If any registration fails, the exception will be logged but will not
     * prevent other components from being registered.</p>
     * 
     * @param init the initialization event (unused, but required for CDI event observation)
     */
    public void initialize(@Observes @Initialized(ApplicationScoped.class) Object init) {
        LOGGER.info("Initializing MCP Server - registering example tools, resources, and prompts");
        
        // Register example tools
        try {
            toolRegistry.registerTool(databaseQueryTool);
            LOGGER.info("Registered tool: " + databaseQueryTool.getName());
        } catch (Exception e) {
            LOGGER.severe("Failed to register DatabaseQueryTool: " + e.getMessage());
        }
        
        try {
            toolRegistry.registerTool(systemInfoTool);
            LOGGER.info("Registered tool: " + systemInfoTool.getName());
        } catch (Exception e) {
            LOGGER.severe("Failed to register SystemInfoTool: " + e.getMessage());
        }
        
        // Register example resources
        try {
            resourceManager.registerResource(configurationResource);
            LOGGER.info("Registered resource: " + configurationResource.getUri());
        } catch (Exception e) {
            LOGGER.severe("Failed to register ConfigurationResource: " + e.getMessage());
        }
        
        try {
            resourceManager.registerResource(databaseSchemaResource);
            LOGGER.info("Registered resource: " + databaseSchemaResource.getUri());
        } catch (Exception e) {
            LOGGER.severe("Failed to register DatabaseSchemaResource: " + e.getMessage());
        }
        
        // Register example prompts
        try {
            promptManager.registerPrompt(codeReviewPrompt);
            LOGGER.info("Registered prompt: " + codeReviewPrompt.getName());
        } catch (Exception e) {
            LOGGER.severe("Failed to register CodeReviewPrompt: " + e.getMessage());
        }
        
        try {
            promptManager.registerPrompt(dataAnalysisPrompt);
            LOGGER.info("Registered prompt: " + dataAnalysisPrompt.getName());
        } catch (Exception e) {
            LOGGER.severe("Failed to register DataAnalysisPrompt: " + e.getMessage());
        }
        
        LOGGER.info("MCP Server initialization complete - " +
                    toolRegistry.listTools().size() + " tools, " +
                    resourceManager.listResources().size() + " resources, " +
                    promptManager.listPrompts().size() + " prompts registered");
    }
}
