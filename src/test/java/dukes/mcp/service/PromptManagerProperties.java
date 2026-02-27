package dukes.mcp.service;

import dukes.mcp.model.PromptArgument;
import dukes.mcp.model.PromptDefinition;
import dukes.mcp.model.PromptGetResult;
import dukes.mcp.model.PromptMessage;
import net.jqwik.api.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for PromptManager.
 * 
 * These tests validate:
 * - Property 21: Prompt Registration Makes Prompts Available
 * - Property 22: Prompt List Completeness
 * - Property 23: Prompt Retrieval with Valid Arguments
 * - Property 26: Prompt Name Uniqueness
 * 
 * **Validates: Requirements 12.2, 12.3, 10.1, 10.2, 11.1, 11.4, 12.4**
 */
class PromptManagerProperties {
    
    /**
     * Property 21: Prompt Registration Makes Prompts Available
     * 
     * For any prompt that is registered, it must appear in the prompts/list response
     * and be retrievable via prompts/get.
     * 
     * **Validates: Requirements 12.2, 12.3**
     */
    @Property
    void registeredPromptIsAvailableForListingAndRetrieval(
            @ForAll("promptNames") String promptName,
            @ForAll("promptDescriptions") String description) {
        
        PromptManager manager = new PromptManager();
        Prompt prompt = createSimplePrompt(promptName, description);
        
        // Register the prompt
        manager.registerPrompt(prompt);
        
        // Assert: Prompt appears in list
        List<PromptDefinition> prompts = manager.listPrompts();
        assertTrue(prompts.stream().anyMatch(p -> p.getName().equals(promptName)),
                "Registered prompt must appear in prompts list");
        
        // Assert: Prompt can be retrieved
        assertTrue(manager.getPrompt(promptName).isPresent(),
                "Registered prompt must be retrievable by name");
        
        // Assert: Prompt can be rendered via getPromptContent
        PromptGetResult result = manager.getPromptContent(promptName, Map.of());
        assertNotNull(result, "Prompt retrieval must return a result");
        assertNotNull(result.getMessages(), "Prompt result must have messages");
        assertFalse(result.getMessages().isEmpty(), "Prompt result messages must not be empty");
    }
    
    /**
     * Property 22: Prompt List Completeness
     * 
     * For any set of registered prompts, calling prompts/list must return all
     * registered prompts with their name, description, and required arguments.
     * 
     * **Validates: Requirements 10.1, 10.2**
     */
    @Property
    void listPromptsReturnsAllRegisteredPrompts(
            @ForAll("promptLists") List<SimplePrompt> promptsToRegister) {
        
        PromptManager manager = new PromptManager();
        
        // Register all prompts
        for (SimplePrompt prompt : promptsToRegister) {
            manager.registerPrompt(prompt);
        }
        
        // Get prompt list
        List<PromptDefinition> listedPrompts = manager.listPrompts();
        
        // Assert: All registered prompts appear in the list
        assertEquals(promptsToRegister.size(), listedPrompts.size(),
                "List must contain all registered prompts");
        
        for (SimplePrompt prompt : promptsToRegister) {
            boolean found = listedPrompts.stream()
                    .anyMatch(def -> def.getName().equals(prompt.getName()) &&
                                   def.getDescription().equals(prompt.getDescription()) &&
                                   def.getArguments() != null);
            assertTrue(found, "Prompt " + prompt.getName() + " must be in the list with complete metadata");
        }
    }
    
    /**
     * Property 23: Prompt Retrieval with Valid Arguments
     * 
     * For any registered prompt and complete set of required arguments,
     * calling prompts/get must return the rendered prompt content with
     * argument values substituted.
     * 
     * **Validates: Requirements 11.1, 11.4**
     */
    @Property
    void promptRetrievalWithValidArgumentsSucceeds(
            @ForAll("promptNames") String promptName,
            @ForAll("validPromptArguments") Map<String, String> arguments) {
        
        PromptManager manager = new PromptManager();
        
        // Create a prompt that accepts any arguments
        Prompt prompt = new SimplePrompt(promptName, "Test prompt", List.of());
        manager.registerPrompt(prompt);
        
        // Retrieve prompt with valid arguments
        PromptGetResult result = manager.getPromptContent(promptName, arguments);
        
        // Assert: Retrieval returns a result
        assertNotNull(result, "Prompt retrieval must return a result");
        assertNotNull(result.getMessages(), "Result must have messages");
        assertFalse(result.getMessages().isEmpty(), "Result messages must not be empty");
        
        // Assert: Messages have proper structure
        for (PromptMessage message : result.getMessages()) {
            assertNotNull(message.getRole(), "Message must have a role");
            assertNotNull(message.getContent(), "Message must have content");
        }
    }
    
    /**
     * Property 26: Prompt Name Uniqueness
     * 
     * For any two prompts in the registry with the same name, they must be
     * the same prompt instance.
     * 
     * **Validates: Requirement 12.4**
     */
    @Property
    void promptNameUniquenessIsEnforced(
            @ForAll("promptNames") String promptName) {
        
        PromptManager manager = new PromptManager();
        
        // Register first prompt
        Prompt prompt1 = createSimplePrompt(promptName, "First version");
        manager.registerPrompt(prompt1);
        
        // Register second prompt with same name
        Prompt prompt2 = createSimplePrompt(promptName, "Second version");
        manager.registerPrompt(prompt2);
        
        // Assert: Only one prompt with that name exists
        List<PromptDefinition> prompts = manager.listPrompts();
        long count = prompts.stream().filter(p -> p.getName().equals(promptName)).count();
        assertEquals(1, count, "Only one prompt with the name should exist");
        
        // Assert: The second prompt replaced the first
        PromptDefinition definition = prompts.stream()
                .filter(p -> p.getName().equals(promptName))
                .findFirst()
                .orElseThrow();
        assertEquals("Second version", definition.getDescription(),
                "The second prompt should have replaced the first");
    }
    
    /**
     * Property: Prompt unregistration removes prompt
     * 
     * For any registered prompt, unregistering it by name must remove it from
     * the registry so it no longer appears in prompts/list and is not retrievable.
     * 
     * **Validates: Requirement 12.5**
     */
    @Property
    void unregisteredPromptIsNotAvailable(
            @ForAll("promptNames") String promptName) {
        
        PromptManager manager = new PromptManager();
        
        // Register prompt
        Prompt prompt = createSimplePrompt(promptName, "Test prompt");
        manager.registerPrompt(prompt);
        
        // Verify it's registered
        assertTrue(manager.getPrompt(promptName).isPresent(),
                "Prompt should be registered");
        
        // Unregister prompt
        manager.unregisterPrompt(promptName);
        
        // Assert: Prompt is no longer in registry
        assertFalse(manager.getPrompt(promptName).isPresent(),
                "Unregistered prompt should not be retrievable");
        
        // Assert: Prompt does not appear in list
        List<PromptDefinition> prompts = manager.listPrompts();
        assertFalse(prompts.stream().anyMatch(p -> p.getName().equals(promptName)),
                "Unregistered prompt should not appear in prompts list");
        
        // Assert: Prompt retrieval throws exception
        assertThrows(PromptManager.PromptNotFoundException.class,
                () -> manager.getPromptContent(promptName, Map.of()),
                "Retrieving unregistered prompt should throw PromptNotFoundException");
    }
    
    /**
     * Property: Non-existent prompt rejection
     * 
     * For any prompt name that is not registered, calling prompts/get must
     * throw PromptNotFoundException.
     * 
     * **Validates: Requirement 11.2**
     */
    @Property
    void nonExistentPromptThrowsException(
            @ForAll("promptNames") String promptName) {
        
        PromptManager manager = new PromptManager();
        
        // Assert: Retrieving non-existent prompt throws exception
        assertThrows(PromptManager.PromptNotFoundException.class,
                () -> manager.getPromptContent(promptName, Map.of()),
                "Retrieving non-existent prompt should throw PromptNotFoundException");
    }
    
    /**
     * Property: Prompt with required arguments validates arguments
     * 
     * For any prompt with required arguments, calling prompts/get without
     * providing all required arguments must throw PromptArgumentException.
     * 
     * **Validates: Requirement 11.3**
     */
    @Property
    void promptWithRequiredArgumentsValidatesArguments(
            @ForAll("promptNames") String promptName,
            @ForAll("requiredArgumentName") String requiredArgName) {
        
        PromptManager manager = new PromptManager();
        
        // Create a prompt with a required argument
        List<PromptArgument> arguments = List.of(
                new PromptArgument(requiredArgName, "Required argument", true)
        );
        Prompt prompt = new SimplePrompt(promptName, "Test prompt", arguments);
        manager.registerPrompt(prompt);
        
        // Assert: Calling without required argument throws exception
        assertThrows(PromptManager.PromptArgumentException.class,
                () -> manager.getPromptContent(promptName, Map.of()),
                "Retrieving prompt without required arguments should throw PromptArgumentException");
        
        // Assert: Calling with required argument succeeds
        Map<String, String> validArgs = Map.of(requiredArgName, "test value");
        PromptGetResult result = manager.getPromptContent(promptName, validArgs);
        assertNotNull(result, "Prompt retrieval with required arguments should succeed");
    }
    
    // ========== Arbitraries (Data Generators) ==========
    
    @Provide
    Arbitrary<String> promptNames() {
        return Arbitraries.of(
                "code_review",
                "data_analysis",
                "bug_report",
                "feature_request",
                "documentation",
                "test_generation",
                "refactoring",
                "security_audit"
        );
    }
    
    @Provide
    Arbitrary<String> promptDescriptions() {
        return Arbitraries.of(
                "Generates a prompt for code review",
                "Analyzes data and provides insights",
                "Creates a structured bug report",
                "Formats a feature request",
                "Generates documentation",
                "Creates test cases",
                "Suggests refactoring improvements",
                "Performs security analysis"
        );
    }
    
    @Provide
    Arbitrary<String> requiredArgumentName() {
        return Arbitraries.of(
                "language",
                "context",
                "data_type",
                "severity",
                "module",
                "test_type"
        );
    }
    
    @Provide
    Arbitrary<Map<String, String>> validPromptArguments() {
        return Arbitraries.oneOf(
                Arbitraries.just(Map.of()),
                Arbitraries.just(Map.of("language", "Java")),
                Arbitraries.just(Map.of("context", "authentication module")),
                Arbitraries.just(Map.of("data_type", "user records")),
                Arbitraries.just(Map.of("language", "Python", "context", "API endpoint"))
        );
    }
    
    @Provide
    Arbitrary<List<SimplePrompt>> promptLists() {
        return Combinators.combine(
                promptNames(),
                promptDescriptions()
        ).as((name, desc) -> new SimplePrompt(name, desc, List.of()))
         .list().ofMinSize(1).ofMaxSize(5)
         .map(list -> {
             // Ensure unique prompt names
             Map<String, SimplePrompt> uniquePrompts = new ConcurrentHashMap<>();
             for (SimplePrompt prompt : list) {
                 uniquePrompts.put(prompt.getName(), prompt);
             }
             return List.copyOf(uniquePrompts.values());
         });
    }
    
    // ========== Helper Methods and Classes ==========
    
    private Prompt createSimplePrompt(String name, String description) {
        return new SimplePrompt(name, description, List.of());
    }
    
    /**
     * Simple prompt implementation for testing.
     */
    static class SimplePrompt implements Prompt {
        private final String name;
        private final String description;
        private final List<PromptArgument> arguments;
        
        public SimplePrompt(String name, String description, List<PromptArgument> arguments) {
            this.name = name;
            this.description = description;
            this.arguments = arguments;
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public String getDescription() {
            return description;
        }
        
        @Override
        public List<PromptArgument> getArguments() {
            return arguments;
        }
        
        @Override
        public List<PromptMessage> render(Map<String, String> arguments) {
            // Simple rendering: just return a user message with the prompt name
            String content = "Prompt: " + name;
            
            // Add any provided arguments to the content
            if (arguments != null && !arguments.isEmpty()) {
                content += " with arguments: " + arguments;
            }
            
            return List.of(PromptMessage.user(content));
        }
    }
}
