package dukes.mcp.model;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for prompt-related MCP data models.
 * Tests serialization, deserialization, and validation.
 */
class PromptModelsTest {
    
    private Jsonb jsonb;
    
    @BeforeEach
    void setUp() {
        jsonb = JsonbBuilder.create();
    }
    
    @Test
    void testPromptArgumentSerialization() {
        PromptArgument arg = new PromptArgument("username", "The user's name", true);
        
        String json = jsonb.toJson(arg);
        assertTrue(json.contains("username"));
        assertTrue(json.contains("The user's name"));
        
        PromptArgument deserialized = jsonb.fromJson(json, PromptArgument.class);
        assertEquals(arg, deserialized);
        assertEquals("username", deserialized.getName());
        assertTrue(deserialized.getRequired());
    }
    
    @Test
    void testPromptArgumentOptional() {
        PromptArgument arg = new PromptArgument("email", "User email", false);
        
        String json = jsonb.toJson(arg);
        PromptArgument deserialized = jsonb.fromJson(json, PromptArgument.class);
        
        assertFalse(deserialized.getRequired());
    }
    
    @Test
    void testPromptDefinitionSerialization() {
        List<PromptArgument> arguments = List.of(
            new PromptArgument("name", "User name", true),
            new PromptArgument("role", "User role", false)
        );
        PromptDefinition prompt = new PromptDefinition(
            "greeting",
            "Generate a greeting message",
            arguments
        );
        
        String json = jsonb.toJson(prompt);
        assertTrue(json.contains("greeting"));
        assertTrue(json.contains("Generate a greeting message"));
        
        PromptDefinition deserialized = jsonb.fromJson(json, PromptDefinition.class);
        assertEquals(prompt, deserialized);
        assertEquals(2, deserialized.getArguments().size());
    }
    
    @Test
    void testPromptDefinitionNoArguments() {
        PromptDefinition prompt = new PromptDefinition(
            "simple_prompt",
            "A simple prompt",
            List.of()
        );
        
        String json = jsonb.toJson(prompt);
        PromptDefinition deserialized = jsonb.fromJson(json, PromptDefinition.class);
        
        assertNotNull(deserialized.getArguments());
        assertTrue(deserialized.getArguments().isEmpty());
    }
    
    @Test
    void testPromptGetParamsSerialization() {
        Map<String, String> arguments = Map.of(
            "name", "John",
            "role", "admin"
        );
        PromptGetParams params = new PromptGetParams("greeting", arguments);
        
        String json = jsonb.toJson(params);
        assertTrue(json.contains("greeting"));
        assertTrue(json.contains("John"));
        
        PromptGetParams deserialized = jsonb.fromJson(json, PromptGetParams.class);
        assertEquals(params, deserialized);
        assertEquals("greeting", deserialized.getName());
        assertEquals(2, deserialized.getArguments().size());
    }
    
    @Test
    void testPromptMessageSerialization() {
        ContentItem content = new ContentItem("text", "Hello, how can I help?");
        PromptMessage message = new PromptMessage("assistant", content);
        
        String json = jsonb.toJson(message);
        assertTrue(json.contains("assistant"));
        assertTrue(json.contains("Hello, how can I help?"));
        
        PromptMessage deserialized = jsonb.fromJson(json, PromptMessage.class);
        assertEquals(message, deserialized);
        assertEquals("assistant", deserialized.getRole());
    }
    
    @Test
    void testPromptMessageUserFactory() {
        PromptMessage message = PromptMessage.user("What is the weather?");
        
        assertEquals("user", message.getRole());
        assertEquals("text", message.getContent().getType());
        assertEquals("What is the weather?", message.getContent().getText());
    }
    
    @Test
    void testPromptMessageAssistantFactory() {
        PromptMessage message = PromptMessage.assistant("The weather is sunny.");
        
        assertEquals("assistant", message.getRole());
        assertEquals("text", message.getContent().getType());
        assertEquals("The weather is sunny.", message.getContent().getText());
    }
    
    @Test
    void testPromptGetResultSerialization() {
        List<PromptMessage> messages = List.of(
            PromptMessage.user("Hello"),
            PromptMessage.assistant("Hi there!")
        );
        PromptGetResult result = new PromptGetResult(messages);
        
        String json = jsonb.toJson(result);
        assertTrue(json.contains("Hello"));
        assertTrue(json.contains("Hi there!"));
        
        PromptGetResult deserialized = jsonb.fromJson(json, PromptGetResult.class);
        assertEquals(2, deserialized.getMessages().size());
    }
    
    @Test
    void testPromptListResultSerialization() {
        List<PromptDefinition> prompts = List.of(
            new PromptDefinition("prompt1", "First prompt", List.of()),
            new PromptDefinition("prompt2", "Second prompt", List.of())
        );
        PromptListResult result = new PromptListResult(prompts);
        
        String json = jsonb.toJson(result);
        assertTrue(json.contains("prompt1"));
        assertTrue(json.contains("prompt2"));
        
        PromptListResult deserialized = jsonb.fromJson(json, PromptListResult.class);
        assertEquals(2, deserialized.getPrompts().size());
    }
    
    @Test
    void testPromptListResultEmpty() {
        PromptListResult result = new PromptListResult(List.of());
        
        String json = jsonb.toJson(result);
        PromptListResult deserialized = jsonb.fromJson(json, PromptListResult.class);
        
        assertNotNull(deserialized.getPrompts());
        assertTrue(deserialized.getPrompts().isEmpty());
    }
}
