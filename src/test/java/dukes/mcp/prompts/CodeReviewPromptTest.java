package dukes.mcp.prompts;

import dukes.mcp.model.PromptArgument;
import dukes.mcp.model.PromptMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CodeReviewPrompt.
 */
class CodeReviewPromptTest {
    
    private CodeReviewPrompt prompt;
    
    @BeforeEach
    void setUp() {
        prompt = new CodeReviewPrompt();
    }
    
    /**
     * Test that getName returns the correct prompt name.
     */
    @Test
    void testGetName() {
        assertEquals("code_review", prompt.getName(),
                "Prompt name should be 'code_review'");
    }
    
    /**
     * Test that getDescription returns a non-empty description.
     */
    @Test
    void testGetDescription() {
        String description = prompt.getDescription();
        assertNotNull(description, "Description should not be null");
        assertFalse(description.isEmpty(), "Description should not be empty");
        assertTrue(description.contains("code"), "Description should mention code");
    }
    
    /**
     * Test that getArguments returns the correct argument definitions.
     */
    @Test
    void testGetArguments() {
        List<PromptArgument> arguments = prompt.getArguments();
        
        assertNotNull(arguments, "Arguments list should not be null");
        assertEquals(2, arguments.size(), "Should have 2 arguments");
        
        // Check language argument (required)
        PromptArgument languageArg = arguments.get(0);
        assertEquals("language", languageArg.getName());
        assertTrue(languageArg.getRequired(), "Language argument should be required");
        
        // Check context argument (optional)
        PromptArgument contextArg = arguments.get(1);
        assertEquals("context", contextArg.getName());
        assertFalse(contextArg.getRequired(), "Context argument should be optional");
    }
    
    /**
     * Test rendering with only the required language argument.
     */
    @Test
    void testRenderWithLanguageOnly() {
        Map<String, String> arguments = Map.of("language", "Java");
        
        List<PromptMessage> messages = prompt.render(arguments);
        
        assertNotNull(messages, "Messages should not be null");
        assertEquals(1, messages.size(), "Should return one message");
        
        PromptMessage message = messages.get(0);
        assertEquals("user", message.getRole(), "Message role should be 'user'");
        assertNotNull(message.getContent(), "Message content should not be null");
        
        String content = message.getContent().getText();
        assertTrue(content.contains("Java"), "Content should mention Java");
        assertTrue(content.contains("review"), "Content should mention review");
    }
    
    /**
     * Test rendering with both language and context arguments.
     */
    @Test
    void testRenderWithLanguageAndContext() {
        Map<String, String> arguments = Map.of(
            "language", "Python",
            "context", "security vulnerabilities and performance"
        );
        
        List<PromptMessage> messages = prompt.render(arguments);
        
        assertNotNull(messages, "Messages should not be null");
        assertEquals(1, messages.size(), "Should return one message");
        
        PromptMessage message = messages.get(0);
        String content = message.getContent().getText();
        
        assertTrue(content.contains("Python"), "Content should mention Python");
        assertTrue(content.contains("security vulnerabilities and performance"),
                "Content should include the context");
    }
    
    /**
     * Test that rendering without language argument throws IllegalArgumentException.
     */
    @Test
    void testRenderWithoutLanguage() {
        Map<String, String> arguments = Map.of("context", "some context");
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> prompt.render(arguments),
            "Should throw IllegalArgumentException when language is missing"
        );
        
        assertTrue(exception.getMessage().contains("language"),
                "Error message should mention 'language'");
    }
    
    /**
     * Test that rendering with empty language throws IllegalArgumentException.
     */
    @Test
    void testRenderWithEmptyLanguage() {
        Map<String, String> arguments = Map.of("language", "");
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> prompt.render(arguments),
            "Should throw IllegalArgumentException when language is empty"
        );
        
        assertTrue(exception.getMessage().contains("language"),
                "Error message should mention 'language'");
    }
    
    /**
     * Test that rendering with whitespace-only language throws IllegalArgumentException.
     */
    @Test
    void testRenderWithWhitespaceLanguage() {
        Map<String, String> arguments = Map.of("language", "   ");
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> prompt.render(arguments),
            "Should throw IllegalArgumentException when language is whitespace"
        );
        
        assertTrue(exception.getMessage().contains("language"),
                "Error message should mention 'language'");
    }
    
    /**
     * Test that rendering with null arguments map throws NullPointerException.
     */
    @Test
    void testRenderWithNullArguments() {
        assertThrows(
            NullPointerException.class,
            () -> prompt.render(null),
            "Should throw NullPointerException when arguments map is null"
        );
    }
    
    /**
     * Test rendering with empty context (should use default text).
     */
    @Test
    void testRenderWithEmptyContext() {
        Map<String, String> arguments = Map.of(
            "language", "JavaScript",
            "context", ""
        );
        
        List<PromptMessage> messages = prompt.render(arguments);
        
        assertNotNull(messages, "Messages should not be null");
        PromptMessage message = messages.get(0);
        String content = message.getContent().getText();
        
        assertTrue(content.contains("JavaScript"), "Content should mention JavaScript");
        assertTrue(content.contains("best practices"),
                "Content should include default text when context is empty");
    }
    
    /**
     * Test that rendered content includes standard review points.
     */
    @Test
    void testRenderIncludesReviewPoints() {
        Map<String, String> arguments = Map.of("language", "C++");
        
        List<PromptMessage> messages = prompt.render(arguments);
        String content = messages.get(0).getContent().getText().toLowerCase();
        
        assertTrue(content.contains("correctness"), "Should mention correctness");
        assertTrue(content.contains("style"), "Should mention style");
        assertTrue(content.contains("performance"), "Should mention performance");
        assertTrue(content.contains("security"), "Should mention security");
    }
}
