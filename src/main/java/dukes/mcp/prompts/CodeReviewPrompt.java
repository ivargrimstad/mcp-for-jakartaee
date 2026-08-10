package dukes.mcp.prompts;

import dukes.mcp.model.PromptArgument;
import dukes.mcp.model.PromptMessage;
import dukes.mcp.service.Prompt;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;

/**
 * Example prompt for code review scenarios.
 * 
 * <p>This prompt generates a code review request for AI assistants, allowing
 * specification of the programming language and optional context about what
 * aspects of the code should be reviewed.</p>
 * 
 * <p>The prompt accepts two parameters:</p>
 * <ul>
 *   <li><b>language</b> (required): The programming language of the code to review</li>
 *   <li><b>context</b> (optional): Additional context about what to focus on during review</li>
 * </ul>
 * 
 * <p>Example usage through MCP protocol:</p>
 * <pre>{@code
 * {
 *   "jsonrpc": "2.0",
 *   "id": "1",
 *   "method": "prompts/get",
 *   "params": {
 *     "name": "code_review",
 *     "arguments": {
 *       "language": "Java",
 *       "context": "security vulnerabilities and performance issues"
 *     }
 *   }
 * }
 * }</pre>
 * 
 * @see Prompt
 * @see PromptArgument
 * @see PromptMessage
 */
@ApplicationScoped
public class CodeReviewPrompt implements Prompt {
    
    /**
     * Returns the unique identifier for this prompt.
     * 
     * @return "code_review"
     */
    @Override
    public String getName() {
        return "code_review";
    }
    
    /**
     * Returns a description of what this prompt does.
     * 
     * @return a description explaining the prompt's purpose
     */
    @Override
    public String getDescription() {
        return "Generates a prompt for reviewing code in a specific programming language with optional focus areas";
    }
    
    /**
     * Returns the list of arguments this prompt accepts.
     * 
     * <p>Arguments:</p>
     * <ul>
     *   <li><b>language</b> (required): Programming language of the code</li>
     *   <li><b>context</b> (optional): Additional context or focus areas for the review</li>
     * </ul>
     * 
     * @return list of prompt arguments
     */
    @Override
    public List<PromptArgument> getArguments() {
        return List.of(
            new PromptArgument("language", "Programming language of the code to review", true),
            new PromptArgument("context", "Additional context about what to focus on during review (e.g., security, performance, best practices)", false)
        );
    }
    
    /**
     * Renders the prompt by substituting argument values into the template.
     * 
     * <p>This method validates that the required "language" argument is provided,
     * then generates a code review prompt. If the optional "context" argument is
     * provided, it includes specific focus areas in the prompt.</p>
     * 
     * @param arguments map of argument names to values
     * @return list containing a single user prompt message with the rendered content
     * @throws IllegalArgumentException if the required "language" argument is missing or empty
     * @throws NullPointerException if arguments is null
     */
    @Override
    public List<PromptMessage> render(Map<String, String> arguments) {
        if (arguments == null) {
            throw new NullPointerException("Arguments map cannot be null");
        }
        
        // Validate required argument
        String language = arguments.get("language");
        if (language == null || language.trim().isEmpty()) {
            throw new IllegalArgumentException("Required argument 'language' is missing or empty");
        }
        
        // Get optional context argument
        String context = arguments.get("context");
        
        // Build the prompt content
        String focus = (context != null && !context.trim().isEmpty())
                ? "with focus on: " + context
                : "for best practices, potential bugs, and code quality";

        String content = """
                Please review the following %s code %s.

                Provide specific feedback on:
                - Code correctness and potential bugs
                - Code style and readability
                - Performance considerations
                - Security vulnerabilities
                - Best practices for %s""".formatted(language, focus, language);

        return List.of(PromptMessage.user(content));
    }
}
