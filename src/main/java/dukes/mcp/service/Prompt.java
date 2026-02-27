package dukes.mcp.service;

import dukes.mcp.model.PromptArgument;
import dukes.mcp.model.PromptMessage;
import java.util.List;
import java.util.Map;

/**
 * Interface defining the contract for MCP prompt implementations.
 * 
 * <p>Prompts are templates with parameters that AI clients can retrieve and use
 * for generating AI interactions. Each prompt must provide metadata (name, description,
 * arguments) and implement the rendering logic to substitute argument values into
 * the template.</p>
 * 
 * <p>Prompt implementations should:</p>
 * <ul>
 *   <li>Return a unique name that identifies the prompt</li>
 *   <li>Provide a clear, descriptive explanation for AI clients</li>
 *   <li>Define required and optional arguments with descriptions</li>
 *   <li>Render the prompt by substituting argument values into the template</li>
 *   <li>Validate that required arguments are provided before rendering</li>
 * </ul>
 * 
 * <p>Example implementation:</p>
 * <pre>{@code
 * @ApplicationScoped
 * public class CodeReviewPrompt implements Prompt {
 *     @Override
 *     public String getName() {
 *         return "code_review";
 *     }
 *     
 *     @Override
 *     public String getDescription() {
 *         return "Generates a prompt for reviewing code in a specific language";
 *     }
 *     
 *     @Override
 *     public List<PromptArgument> getArguments() {
 *         return List.of(
 *             new PromptArgument("language", "Programming language of the code", true),
 *             new PromptArgument("context", "Additional context about the code", false)
 *         );
 *     }
 *     
 *     @Override
 *     public List<PromptMessage> render(Map<String, String> arguments) {
 *         String language = arguments.get("language");
 *         String context = arguments.getOrDefault("context", "");
 *         
 *         String content = "Please review the following " + language + " code";
 *         if (!context.isEmpty()) {
 *             content += " with focus on: " + context;
 *         }
 *         
 *         return List.of(PromptMessage.user(content));
 *     }
 * }
 * }</pre>
 * 
 * @see PromptArgument
 * @see PromptMessage
 * @see dukes.mcp.model.PromptDefinition
 */
public interface Prompt {
    
    /**
     * Returns the unique name of this prompt.
     * 
     * <p>The name is used to identify and retrieve the prompt through the MCP protocol.
     * It must be unique within the prompt registry and should follow naming conventions
     * (e.g., snake_case like "code_review" or "data_analysis").</p>
     * 
     * @return the prompt name, must be non-null and non-empty
     */
    String getName();
    
    /**
     * Returns a human-readable description of what this prompt does.
     * 
     * <p>The description should be clear and informative, helping AI clients understand
     * the purpose of the prompt and when to use it. It should describe what kind of
     * interaction or task the prompt is designed for.</p>
     * 
     * @return the prompt description, must be non-null and non-empty
     */
    String getDescription();
    
    /**
     * Returns the list of arguments that this prompt accepts.
     * 
     * <p>Arguments define the parameters that can be passed to the prompt template.
     * Each argument specifies its name, description, and whether it is required.
     * Required arguments must be provided when rendering the prompt, while optional
     * arguments may be omitted.</p>
     * 
     * <p>Example arguments list:</p>
     * <pre>{@code
     * List.of(
     *     new PromptArgument("language", "Programming language", true),
     *     new PromptArgument("context", "Additional context", false)
     * )
     * }</pre>
     * 
     * @return the list of prompt arguments, must be non-null (may be empty for prompts without parameters)
     */
    List<PromptArgument> getArguments();
    
    /**
     * Renders the prompt by substituting the provided argument values into the template.
     * 
     * <p>This method generates the actual prompt content by taking the argument values
     * and producing a list of prompt messages. The messages typically include a role
     * (e.g., "user", "assistant") and content with the rendered text.</p>
     * 
     * <p>The implementation should:</p>
     * <ul>
     *   <li>Validate that all required arguments are present in the arguments map</li>
     *   <li>Use default values or omit optional arguments if not provided</li>
     *   <li>Substitute argument values into the prompt template</li>
     *   <li>Return a list of PromptMessage objects with the rendered content</li>
     * </ul>
     * 
     * <p>If required arguments are missing, the implementation should throw an
     * IllegalArgumentException with a descriptive error message indicating which
     * arguments are missing.</p>
     * 
     * <p>Example rendering:</p>
     * <pre>{@code
     * public List<PromptMessage> render(Map<String, String> arguments) {
     *     String language = arguments.get("language");
     *     if (language == null || language.isEmpty()) {
     *         throw new IllegalArgumentException("Required argument 'language' is missing");
     *     }
     *     
     *     String content = "Review this " + language + " code for best practices";
     *     return List.of(PromptMessage.user(content));
     * }
     * }</pre>
     * 
     * @param arguments a map of argument names to their values
     * @return a list of prompt messages with the rendered content
     * @throws IllegalArgumentException if required arguments are missing or invalid
     * @throws NullPointerException if arguments is null
     */
    List<PromptMessage> render(Map<String, String> arguments);
}
