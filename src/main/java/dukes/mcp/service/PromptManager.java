package dukes.mcp.service;

import dukes.mcp.model.PromptArgument;
import dukes.mcp.model.PromptDefinition;
import dukes.mcp.model.PromptGetResult;
import dukes.mcp.model.PromptMessage;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * ApplicationScoped CDI bean that manages the registration, discovery, and retrieval of MCP prompts.
 * 
 * <p>The PromptManager provides a centralized registry for prompts that can be discovered and retrieved
 * through the MCP protocol. It supports dynamic prompt registration/unregistration, thread-safe
 * concurrent access, enforces unique prompt names, and validates required arguments before rendering.</p>
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li>Thread-safe prompt storage using ConcurrentHashMap</li>
 *   <li>Dynamic prompt registration and unregistration</li>
 *   <li>Unique prompt name enforcement</li>
 *   <li>Required argument validation before rendering</li>
 *   <li>Prompt discovery through listPrompts()</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * @Inject
 * private PromptManager promptManager;
 * 
 * public void init() {
 *     promptManager.registerPrompt(new CodeReviewPrompt());
 *     List<PromptDefinition> prompts = promptManager.listPrompts();
 *     PromptGetResult result = promptManager.getPromptContent("code_review", Map.of("language", "Java"));
 * }
 * }</pre>
 * 
 * @see Prompt
 * @see PromptDefinition
 * @see PromptGetResult
 */
@ApplicationScoped
public class PromptManager {
    
    private static final Logger LOGGER = Logger.getLogger(PromptManager.class.getName());
    
    /**
     * Thread-safe map storing registered prompts by name.
     */
    private final ConcurrentHashMap<String, Prompt> prompts = new ConcurrentHashMap<>();
    
    /**
     * Registers a prompt in the registry.
     * 
     * <p>If a prompt with the same name already exists, it will be replaced with the new prompt.
     * This allows for dynamic prompt updates at runtime.</p>
     * 
     * @param prompt the prompt to register, must not be null
     * @throws NullPointerException if prompt is null
     * @throws IllegalArgumentException if prompt name is null or empty
     */
    public void registerPrompt(Prompt prompt) {
        if (prompt == null) {
            throw new NullPointerException("Prompt cannot be null");
        }
        
        String promptName = prompt.getName();
        if (promptName == null || promptName.trim().isEmpty()) {
            throw new IllegalArgumentException("Prompt name cannot be null or empty");
        }
        
        Prompt previousPrompt = prompts.put(promptName, prompt);
        if (previousPrompt != null) {
            LOGGER.log(Level.INFO, "Replaced existing prompt: {0}", promptName);
        } else {
            LOGGER.log(Level.INFO, "Registered new prompt: {0}", promptName);
        }
    }
    
    /**
     * Unregisters a prompt from the registry by name.
     * 
     * <p>If the prompt does not exist, this method does nothing.</p>
     * 
     * @param promptName the name of the prompt to unregister, must not be null
     * @throws NullPointerException if promptName is null
     */
    public void unregisterPrompt(String promptName) {
        if (promptName == null) {
            throw new NullPointerException("Prompt name cannot be null");
        }
        
        Prompt removedPrompt = prompts.remove(promptName);
        if (removedPrompt != null) {
            LOGGER.log(Level.INFO, "Unregistered prompt: {0}", promptName);
        }
    }
    
    /**
     * Retrieves a prompt by name.
     * 
     * @param promptName the name of the prompt to retrieve
     * @return an Optional containing the prompt if found, or empty if not found
     */
    public Optional<Prompt> getPrompt(String promptName) {
        return Optional.ofNullable(prompts.get(promptName));
    }
    
    /**
     * Lists all registered prompts.
     * 
     * <p>Returns a list of PromptDefinition objects containing the name, description,
     * and arguments for each registered prompt. The list is a snapshot of the current
     * registry state and is safe to iterate even if prompts are registered/unregistered
     * concurrently.</p>
     * 
     * @return a list of all registered prompt definitions, never null
     */
    public List<PromptDefinition> listPrompts() {
        return prompts.values().stream()
                .map(prompt -> new PromptDefinition(
                        prompt.getName(),
                        prompt.getDescription(),
                        prompt.getArguments()
                ))
                .collect(Collectors.toList());
    }
    
    /**
     * Retrieves and renders the content of a prompt with the provided arguments.
     * 
     * <p>This method performs the following steps:</p>
     * <ol>
     *   <li>Looks up the prompt by name</li>
     *   <li>Validates that all required arguments are provided</li>
     *   <li>Renders the prompt with the provided arguments</li>
     *   <li>Returns the rendered prompt messages</li>
     * </ol>
     * 
     * <p>All errors are caught and wrapped in appropriate exceptions with descriptive messages.
     * This ensures consistent error handling across the MCP protocol.</p>
     * 
     * @param promptName the name of the prompt to retrieve
     * @param arguments the arguments to pass to the prompt template
     * @return a PromptGetResult containing the rendered prompt messages
     * @throws PromptNotFoundException if the prompt name is not registered
     * @throws PromptArgumentException if required arguments are missing or invalid
     */
    public PromptGetResult getPromptContent(String promptName, Map<String, String> arguments) {
        // Step 1: Lookup prompt by name
        Optional<Prompt> promptOpt = getPrompt(promptName);
        if (promptOpt.isEmpty()) {
            LOGGER.log(Level.WARNING, "Prompt not found: {0}", promptName);
            throw new PromptNotFoundException("Prompt not found: " + promptName);
        }
        Prompt prompt = promptOpt.get();
        
        // Step 2: Validate required arguments
        List<String> missingArgs = validateRequiredArguments(prompt, arguments);
        if (!missingArgs.isEmpty()) {
            String errorMsg = "Missing required arguments: " + String.join(", ", missingArgs);
            LOGGER.log(Level.WARNING, "Prompt {0}: {1}", new Object[]{promptName, errorMsg});
            throw new PromptArgumentException(errorMsg);
        }
        
        // Step 3: Render prompt with arguments
        try {
            LOGGER.log(Level.FINE, "Rendering prompt: {0}", promptName);
            List<PromptMessage> messages = prompt.render(arguments);
            
            // Step 4: Return prompt content
            return new PromptGetResult(messages);
            
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid arguments for prompt " + promptName, e);
            throw new PromptArgumentException("Invalid arguments: " + e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to render prompt: " + promptName, e);
            throw new PromptRenderException("Failed to render prompt: " + promptName, e);
        }
    }
    
    /**
     * Validates that all required arguments are provided.
     * 
     * <p>Checks the prompt's argument definitions and verifies that all required
     * arguments are present in the provided arguments map.</p>
     * 
     * @param prompt the prompt to validate arguments for
     * @param arguments the provided arguments
     * @return a list of missing required argument names (empty if all required arguments are present)
     */
    private List<String> validateRequiredArguments(Prompt prompt, Map<String, String> arguments) {
        List<PromptArgument> promptArgs = prompt.getArguments();
        if (promptArgs == null || promptArgs.isEmpty()) {
            return List.of();
        }
        
        Map<String, String> safeArguments = (arguments != null) ? arguments : Map.of();
        
        return promptArgs.stream()
                .filter(arg -> Boolean.TRUE.equals(arg.getRequired()))
                .map(PromptArgument::getName)
                .filter(argName -> !safeArguments.containsKey(argName) || 
                                   safeArguments.get(argName) == null || 
                                   safeArguments.get(argName).trim().isEmpty())
                .collect(Collectors.toList());
    }
    
    /**
     * Exception thrown when a requested prompt is not found in the registry.
     */
    public static class PromptNotFoundException extends RuntimeException {
        public PromptNotFoundException(String message) {
            super(message);
        }
    }
    
    /**
     * Exception thrown when prompt arguments are missing or invalid.
     */
    public static class PromptArgumentException extends RuntimeException {
        public PromptArgumentException(String message) {
            super(message);
        }
        
        public PromptArgumentException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    /**
     * Exception thrown when a prompt cannot be rendered due to errors.
     */
    public static class PromptRenderException extends RuntimeException {
        public PromptRenderException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
