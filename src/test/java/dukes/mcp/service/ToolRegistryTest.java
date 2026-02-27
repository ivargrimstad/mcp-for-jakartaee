package dukes.mcp.service;

import dukes.mcp.model.ToolDefinition;
import dukes.mcp.model.ToolResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ToolRegistry.
 * 
 * Tests cover:
 * - Tool registration and replacement
 * - Tool unregistration
 * - Tool execution with valid and invalid arguments
 * - Tool not found scenarios
 * - Concurrent tool execution
 * 
 * Validates: Requirements 6.2, 6.3, 6.4, 6.5, 5.3, 5.4, 5.5, 15.1, 19.1
 */
class ToolRegistryTest {
    
    private ToolRegistry registry;
    
    @BeforeEach
    void setUp() {
        registry = new ToolRegistry();
    }
    
    /**
     * Test tool registration.
     * Validates: Requirement 6.2
     */
    @Test
    void testRegisterTool() {
        Tool tool = createSimpleTool("test_tool", "Test tool");
        
        registry.registerTool(tool);
        
        Optional<Tool> retrieved = registry.getTool("test_tool");
        assertTrue(retrieved.isPresent(), "Registered tool should be retrievable");
        assertEquals("test_tool", retrieved.get().getName());
    }
    
    /**
     * Test that registered tool appears in list.
     * Validates: Requirement 6.2
     */
    @Test
    void testRegisteredToolAppearsInList() {
        Tool tool = createSimpleTool("test_tool", "Test tool");
        
        registry.registerTool(tool);
        
        List<ToolDefinition> tools = registry.listTools();
        assertEquals(1, tools.size(), "List should contain one tool");
        assertEquals("test_tool", tools.get(0).getName());
        assertEquals("Test tool", tools.get(0).getDescription());
        assertNotNull(tools.get(0).getInputSchema());
    }
    
    /**
     * Test tool replacement on duplicate registration.
     * Validates: Requirement 6.4
     */
    @Test
    void testToolReplacement() {
        Tool tool1 = createSimpleTool("test_tool", "First version");
        Tool tool2 = createSimpleTool("test_tool", "Second version");
        
        registry.registerTool(tool1);
        registry.registerTool(tool2);
        
        List<ToolDefinition> tools = registry.listTools();
        assertEquals(1, tools.size(), "Should only have one tool with that name");
        assertEquals("Second version", tools.get(0).getDescription(),
                "Second tool should replace first");
    }
    
    /**
     * Test tool unregistration.
     * Validates: Requirement 6.5
     */
    @Test
    void testUnregisterTool() {
        Tool tool = createSimpleTool("test_tool", "Test tool");
        
        registry.registerTool(tool);
        assertTrue(registry.getTool("test_tool").isPresent(), "Tool should be registered");
        
        registry.unregisterTool("test_tool");
        
        assertFalse(registry.getTool("test_tool").isPresent(),
                "Tool should be unregistered");
        assertEquals(0, registry.listTools().size(),
                "Tool should not appear in list");
    }
    
    /**
     * Test unregistering non-existent tool does nothing.
     * Validates: Requirement 6.5
     */
    @Test
    void testUnregisterNonExistentTool() {
        assertDoesNotThrow(() -> registry.unregisterTool("non_existent"),
                "Unregistering non-existent tool should not throw");
    }
    
    /**
     * Test tool execution with valid arguments.
     * Validates: Requirements 5.1, 5.2, 6.3
     */
    @Test
    void testExecuteToolWithValidArguments() {
        Tool tool = createSimpleTool("test_tool", "Test tool");
        registry.registerTool(tool);
        
        ToolResult result = registry.executeTool("test_tool", Map.of());
        
        assertNotNull(result, "Result should not be null");
        assertFalse(result.getIsError(), "Execution should succeed");
        assertNotNull(result.getContent(), "Result should have content");
    }
    
    /**
     * Test tool execution with tool not found.
     * Validates: Requirement 5.3
     */
    @Test
    void testExecuteNonExistentTool() {
        ToolResult result = registry.executeTool("non_existent", Map.of());
        
        assertNotNull(result, "Result should not be null");
        assertTrue(result.getIsError(), "Result should indicate error");
        assertTrue(result.getContent().get(0).getText().contains("Tool not found"),
                "Error message should indicate tool not found");
    }
    
    /**
     * Test tool execution with invalid arguments (schema validation failure).
     * Validates: Requirements 5.4, 16.3
     */
    @Test
    void testExecuteToolWithInvalidArguments() {
        Tool tool = createToolWithRequiredField("test_tool", "Test tool", "query");
        registry.registerTool(tool);
        
        // Execute without required field
        ToolResult result = registry.executeTool("test_tool", Map.of());
        
        assertNotNull(result, "Result should not be null");
        assertTrue(result.getIsError(), "Result should indicate error");
        assertTrue(result.getContent().get(0).getText().contains("Invalid arguments"),
                "Error message should indicate invalid arguments");
    }
    
    /**
     * Test tool execution with exception during execution.
     * Validates: Requirement 5.5
     */
    @Test
    void testExecuteToolWithException() {
        Tool tool = new FailingTool("failing_tool", "Tool that throws exception");
        registry.registerTool(tool);
        
        ToolResult result = registry.executeTool("failing_tool", Map.of());
        
        assertNotNull(result, "Result should not be null");
        assertTrue(result.getIsError(), "Result should indicate error");
        assertTrue(result.getContent().get(0).getText().contains("Tool execution failed"),
                "Error message should indicate execution failure");
    }
    
    /**
     * Test concurrent tool execution.
     * Validates: Requirements 15.1, 19.1
     */
    @Test
    void testConcurrentToolExecution() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);
        Tool tool = new CountingTool("counter_tool", "Counting tool", counter);
        registry.registerTool(tool);
        
        int numThreads = 10;
        int executionsPerThread = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads * executionsPerThread);
        
        // Launch concurrent executions
        for (int i = 0; i < numThreads * executionsPerThread; i++) {
            executor.submit(() -> {
                try {
                    registry.executeTool("counter_tool", Map.of());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Wait for all executions to complete
        assertTrue(latch.await(5, TimeUnit.SECONDS),
                "All executions should complete within timeout");
        
        // Verify all executions completed
        assertEquals(numThreads * executionsPerThread, counter.get(),
                "All executions should have been counted");
        
        executor.shutdown();
    }
    
    /**
     * Test registering null tool throws exception.
     */
    @Test
    void testRegisterNullTool() {
        assertThrows(NullPointerException.class,
                () -> registry.registerTool(null),
                "Registering null tool should throw NullPointerException");
    }
    
    /**
     * Test registering tool with null name throws exception.
     */
    @Test
    void testRegisterToolWithNullName() {
        Tool tool = new SimpleTool(null, "Description", Map.of("type", "object"));
        
        assertThrows(IllegalArgumentException.class,
                () -> registry.registerTool(tool),
                "Registering tool with null name should throw IllegalArgumentException");
    }
    
    /**
     * Test registering tool with empty name throws exception.
     */
    @Test
    void testRegisterToolWithEmptyName() {
        Tool tool = new SimpleTool("", "Description", Map.of("type", "object"));
        
        assertThrows(IllegalArgumentException.class,
                () -> registry.registerTool(tool),
                "Registering tool with empty name should throw IllegalArgumentException");
    }
    
    /**
     * Test unregistering with null name throws exception.
     */
    @Test
    void testUnregisterNullName() {
        assertThrows(NullPointerException.class,
                () -> registry.unregisterTool(null),
                "Unregistering with null name should throw NullPointerException");
    }
    
    /**
     * Test listing tools when registry is empty.
     * Validates: Requirement 4.4
     */
    @Test
    void testListToolsWhenEmpty() {
        List<ToolDefinition> tools = registry.listTools();
        
        assertNotNull(tools, "List should not be null");
        assertTrue(tools.isEmpty(), "List should be empty");
    }
    
    /**
     * Test listing multiple tools.
     * Validates: Requirements 4.1, 4.2
     */
    @Test
    void testListMultipleTools() {
        registry.registerTool(createSimpleTool("tool1", "First tool"));
        registry.registerTool(createSimpleTool("tool2", "Second tool"));
        registry.registerTool(createSimpleTool("tool3", "Third tool"));
        
        List<ToolDefinition> tools = registry.listTools();
        
        assertEquals(3, tools.size(), "Should have three tools");
        assertTrue(tools.stream().anyMatch(t -> t.getName().equals("tool1")));
        assertTrue(tools.stream().anyMatch(t -> t.getName().equals("tool2")));
        assertTrue(tools.stream().anyMatch(t -> t.getName().equals("tool3")));
    }
    
    /**
     * Test tool execution validates arguments against schema.
     * Validates: Requirements 16.2, 16.3
     */
    @Test
    void testSchemaValidation() {
        // Create tool with specific schema requirements
        Tool tool = createToolWithRequiredField("query_tool", "Query tool", "query");
        registry.registerTool(tool);
        
        // Test with valid arguments
        ToolResult validResult = registry.executeTool("query_tool",
                Map.of("query", "SELECT * FROM users"));
        assertFalse(validResult.getIsError(), "Valid arguments should succeed");
        
        // Test with missing required field
        ToolResult invalidResult = registry.executeTool("query_tool", Map.of());
        assertTrue(invalidResult.getIsError(), "Missing required field should fail");
        
        // Test with wrong type
        ToolResult wrongTypeResult = registry.executeTool("query_tool",
                Map.of("query", 123));
        assertTrue(wrongTypeResult.getIsError(), "Wrong type should fail validation");
    }
    
    // ========== Helper Methods and Classes ==========
    
    private Tool createSimpleTool(String name, String description) {
        return new SimpleTool(name, description, createFlexibleSchema());
    }
    
    private Tool createToolWithRequiredField(String name, String description, String fieldName) {
        Object schema = Map.of(
                "type", "object",
                "properties", Map.of(
                        fieldName, Map.of("type", "string")
                ),
                "required", List.of(fieldName)
        );
        return new SimpleTool(name, description, schema);
    }
    
    private Object createFlexibleSchema() {
        return Map.of(
                "type", "object",
                "properties", Map.of(),
                "additionalProperties", true
        );
    }
    
    /**
     * Simple tool implementation for testing.
     */
    static class SimpleTool implements Tool {
        private final String name;
        private final String description;
        private final Object inputSchema;
        
        public SimpleTool(String name, String description, Object inputSchema) {
            this.name = name;
            this.description = description;
            this.inputSchema = inputSchema;
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
        public Object getInputSchema() {
            return inputSchema;
        }
        
        @Override
        public ToolResult execute(Map<String, Object> arguments) {
            return ToolResult.success("Executed " + name);
        }
    }
    
    /**
     * Tool that throws an exception during execution.
     */
    static class FailingTool implements Tool {
        private final String name;
        private final String description;
        
        public FailingTool(String name, String description) {
            this.name = name;
            this.description = description;
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
        public Object getInputSchema() {
            return Map.of("type", "object", "properties", Map.of());
        }
        
        @Override
        public ToolResult execute(Map<String, Object> arguments) {
            throw new RuntimeException("Simulated tool execution failure");
        }
    }
    
    /**
     * Tool that counts executions for concurrency testing.
     */
    static class CountingTool implements Tool {
        private final String name;
        private final String description;
        private final AtomicInteger counter;
        
        public CountingTool(String name, String description, AtomicInteger counter) {
            this.name = name;
            this.description = description;
            this.counter = counter;
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
        public Object getInputSchema() {
            return Map.of("type", "object", "properties", Map.of());
        }
        
        @Override
        public ToolResult execute(Map<String, Object> arguments) {
            // Simulate some work
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            counter.incrementAndGet();
            return ToolResult.success("Count: " + counter.get());
        }
    }
}
