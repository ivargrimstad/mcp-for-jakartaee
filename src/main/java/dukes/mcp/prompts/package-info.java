/**
 * Example prompt implementations for the MCP Server.
 * 
 * <p>This package contains concrete implementations of the {@link dukes.mcp.service.Prompt}
 * interface that demonstrate how to create prompts for AI assistants. Prompts are templates
 * with parameters that AI clients can retrieve and use for generating AI interactions.</p>
 * 
 * <p>Each prompt implementation:</p>
 * <ul>
 *   <li>Implements the {@link dukes.mcp.service.Prompt} interface</li>
 *   <li>Is annotated with {@code @ApplicationScoped} for CDI management</li>
 *   <li>Defines required and optional arguments</li>
 *   <li>Renders prompt content by substituting argument values</li>
 *   <li>Validates required arguments before rendering</li>
 * </ul>
 * 
 * <p>Example prompts included:</p>
 * <ul>
 *   <li>{@link dukes.mcp.prompts.CodeReviewPrompt} - Generates code review prompts</li>
 * </ul>
 * 
 * @see dukes.mcp.service.Prompt
 * @see dukes.mcp.service.PromptManager
 */
package dukes.mcp.prompts;
