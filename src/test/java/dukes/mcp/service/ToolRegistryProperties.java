package dukes.mcp.service;

import dukes.mcp.model.ContentItem;
import dukes.mcp.model.ToolDefinition;
import dukes.mcp.model.ToolResult;
import net.jqwik.api.*;
import net.jqwik.api.constraints.NotBlank;
import net.jqwik.api.constraints.NotEmpty;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for ToolRegistry.
 * 
 * These tests validate:
 * - Property 6: Tool Registration Makes Tools Available
 * - Property 7: Tool List Completeness
 * - Property 8: Tool Execution with Valid Arguments
 * - Property 14: Tool Execution Isolation
 * 
 * **Validates: Requirements 6.2, 6.3, 4.1, 4.2, 5.1, 5.2, 15.1, 15.3**
 */
class ToolRegistryProperties {
    
    /**
     * Property 6: Tool Registration Makes Tools Available
     * 
     * For any tool that is registered, it must appear in the tools/list response
     * and be executable via tools/call.
     * 
     * **Validates: Requirements 6.2, 6.3**
     */
    @Property
    void registeredToolIsAvailableForListingAndExecution(
            @ForAll("toolNames") String toolName,
            @ForAll("toolDescriptions") String description) {
        
        ToolRegistry registry = new ToolRegistry();
        Tool tool = createSimpleTool(toolName, description);
        
        // Register the tool
        registry.registerTool(tool);
        
        // Assert: Tool appears in list
        List<ToolDefinition> tools = registry.listTools();
        assertTrue(tools.stream().anyMatch(t -> t.getName().equals(toolName)),
                "Registered tool must appear in tools list");
        
        // Assert: Tool can be retrieved
        assertTrue(registry.getTool(toolName).isPresent(),
                "Registered tool must be retrievable by name");
        
        // Assert: Tool can be executed
        ToolResult result = registry.executeTool(toolName, Map.of());
        assertNotNull(result, "Tool execution must return a result");
        assertFalse(result.getIsError(), "Tool execution should succeed");
    }
    
    /**
     * Property 7: Tool List Completeness
     * 
     * For any set of registered tools, calling tools/list must return all
     * registered tools with their name, description, and input schema.
     * 
     * **Validates: Requirements 4.1, 4.2**
     */
    @Property
    void listToolsReturnsAllRegisteredTools(
            @ForAll("toolLists") List<SimpleTool> toolsToRegister) {
        
        ToolRegistry registry = new ToolRegistry();
        
        // Register all tools
        for (SimpleTool tool : toolsToRegister) {
            registry.registerTool(tool);
        }
        
        // Get tool list
        List<ToolDefinition> listedTools = registry.listTools();
        
        // Assert: All registered tools appear in the list
        assertEquals(toolsToRegister.size(), listedTools.size(),
                "List must contain all registered tools");
        
        for (SimpleTool tool : toolsToRegister) {
            boolean found = listedTools.stream()
                    .anyMatch(def -> def.getName().equals(tool.getName()) &&
                                   def.getDescription().equals(tool.getDescription()) &&
                                   def.getInputSchema() != null);
            assertTrue(found, "Tool " + tool.getName() + " must be in the list with complete metadata");
        }
    }
    
    /**
     * Property 8: Tool Execution with Valid Arguments
     * 
     * For any registered tool and arguments that match its input schema,
     * calling tools/call must execute the tool and return a result.
     * 
     * **Validates: Requirements 5.1, 5.2**
     */
    @Property
    void toolExecutionWithValidArgumentsSucceeds(
            @ForAll("toolNames") String toolName,
            @ForAll("validArguments") Map<String, Object> arguments) {
        
        ToolRegistry registry = new ToolRegistry();
        
        // Create a tool that accepts any arguments
        Tool tool = new SimpleTool(toolName, "Test tool", createFlexibleSchema());
        registry.registerTool(tool);
        
        // Execute tool with valid arguments
        ToolResult result = registry.executeTool(toolName, arguments);
        
        // Assert: Execution returns a result
        assertNotNull(result, "Tool execution must return a result");
        assertNotNull(result.getContent(), "Result must have content");
        assertFalse(result.getContent().isEmpty(), "Result content must not be empty");
        
        // Assert: Result has proper structure
        assertNotNull(result.getIsError(), "Result must have isError flag");
    }
    
    /**
     * Property 14: Tool Execution Isolation
     * 
     * For any two different tools executed concurrently with their respective arguments,
     * one tool's execution must not affect the other tool's execution or results.
     * 
     * **Validates: Requirements 15.1, 15.3**
     */
    @Property(tries = 50)
    void concurrentToolExecutionsAreIsolated(
            @ForAll("toolNames") String toolName1,
            @ForAll("toolNames") String toolName2) {
        
        Assume.that(!toolName1.equals(toolName2));
        
        ToolRegistry registry = new ToolRegistry();
        
        // Create two tools with counters to track executions
        AtomicInteger counter1 = new AtomicInteger(0);
        AtomicInteger counter2 = new AtomicInteger(0);
        
        Tool tool1 = new CountingTool(toolName1, "Tool 1", counter1);
        Tool tool2 = new CountingTool(toolName2, "Tool 2", counter2);
        
        registry.registerTool(tool1);
        registry.registerTool(tool2);
        
        // Execute both tools concurrently multiple times
        int executionsPerTool = 10;
        ExecutorService executor = Executors.newFixedThreadPool(4);
        CountDownLatch latch = new CountDownLatch(executionsPerTool * 2);
        
        try {
            // Launch concurrent executions of tool1
            for (int i = 0; i < executionsPerTool; i++) {
                executor.submit(() -> {
                    try {
                        registry.executeTool(toolName1, Map.of());
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            // Launch concurrent executions of tool2
            for (int i = 0; i < executionsPerTool; i++) {
                executor.submit(() -> {
                    try {
                        registry.executeTool(toolName2, Map.of());
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            // Wait for all executions to complete
            assertTrue(latch.await(5, TimeUnit.SECONDS),
                    "All tool executions must complete within timeout");
            
            // Assert: Each tool was executed the correct number of times
            assertEquals(executionsPerTool, counter1.get(),
                    "Tool 1 must be executed exactly " + executionsPerTool + " times");
            assertEquals(executionsPerTool, counter2.get(),
                    "Tool 2 must be executed exactly " + executionsPerTool + " times");
            
        } catch (InterruptedException e) {
            fail("Concurrent execution test was interrupted: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }
    
    /**
     * Property: Tool replacement on duplicate registration
     * 
     * For any tool name, registering a new tool with that name must replace
     * any existing tool with the same name.
     * 
     * **Validates: Requirement 6.4**
     */
    @Property
    void duplicateToolRegistrationReplacesExistingTool(
            @ForAll("toolNames") String toolName) {
        
        ToolRegistry registry = new ToolRegistry();
        
        // Register first tool
        Tool tool1 = createSimpleTool(toolName, "First version");
        registry.registerTool(tool1);
        
        // Register second tool with same name
        Tool tool2 = createSimpleTool(toolName, "Second version");
        registry.registerTool(tool2);
        
        // Assert: Only one tool with that name exists
        List<ToolDefinition> tools = registry.listTools();
        long count = tools.stream().filter(t -> t.getName().equals(toolName)).count();
        assertEquals(1, count, "Only one tool with the name should exist");
        
        // Assert: The second tool replaced the first
        ToolDefinition definition = tools.stream()
                .filter(t -> t.getName().equals(toolName))
                .findFirst()
                .orElseThrow();
        assertEquals("Second version", definition.getDescription(),
                "The second tool should have replaced the first");
    }
    
    /**
     * Property: Tool unregistration removes tool
     * 
     * For any registered tool, unregistering it by name must remove it from
     * the registry so it no longer appears in tools/list and is not executable.
     * 
     * **Validates: Requirement 6.5**
     */
    @Property
    void unregisteredToolIsNotAvailable(
            @ForAll("toolNames") String toolName) {
        
        ToolRegistry registry = new ToolRegistry();
        
        // Register tool
        Tool tool = createSimpleTool(toolName, "Test tool");
        registry.registerTool(tool);
        
        // Verify it's registered
        assertTrue(registry.getTool(toolName).isPresent(),
                "Tool should be registered");
        
        // Unregister tool
        registry.unregisterTool(toolName);
        
        // Assert: Tool is no longer in registry
        assertFalse(registry.getTool(toolName).isPresent(),
                "Unregistered tool should not be retrievable");
        
        // Assert: Tool does not appear in list
        List<ToolDefinition> tools = registry.listTools();
        assertFalse(tools.stream().anyMatch(t -> t.getName().equals(toolName)),
                "Unregistered tool should not appear in tools list");
        
        // Assert: Tool execution returns error
        ToolResult result = registry.executeTool(toolName, Map.of());
        assertTrue(result.getIsError(), "Executing unregistered tool should return error");
    }
    
    // ========== Arbitraries (Data Generators) ==========
    
    @Provide
    Arbitrary<String> toolNames() {
        return Arbitraries.of(
                "system_info",
                "database_query",
                "file_reader",
                "calculator",
                "weather_lookup",
                "user_manager",
                "data_processor",
                "report_generator"
        );
    }
    
    @Provide
    Arbitrary<String> toolDescriptions() {
        return Arbitraries.of(
                "Returns system information",
                "Executes database queries",
                "Reads file contents",
                "Performs calculations",
                "Looks up weather data",
                "Manages user accounts",
                "Processes data",
                "Generates reports"
        );
    }
    
    @Provide
    Arbitrary<Map<String, Object>> validArguments() {
        return Arbitraries.oneOf(
                Arbitraries.just(Map.of()),
                Arbitraries.just(Map.of("key", "value")),
                Arbitraries.just(Map.of("query", "SELECT * FROM users")),
                Arbitraries.just(Map.of("count", 10)),
                Arbitraries.just(Map.of("enabled", true))
        );
    }
    
    @Provide
    Arbitrary<List<SimpleTool>> toolLists() {
        return Combinators.combine(
                toolNames(),
                toolDescriptions()
        ).as((name, desc) -> new SimpleTool(name, desc, createFlexibleSchema()))
         .list().ofMinSize(1).ofMaxSize(5)
         .map(list -> {
             // Ensure unique tool names
             Map<String, SimpleTool> uniqueTools = new ConcurrentHashMap<>();
             for (SimpleTool tool : list) {
                 uniqueTools.put(tool.getName(), tool);
             }
             return List.copyOf(uniqueTools.values());
         });
    }
    
    // ========== Helper Methods and Classes ==========
    
    private Tool createSimpleTool(String name, String description) {
        return new SimpleTool(name, description, createFlexibleSchema());
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
     * Tool that counts executions for isolation testing.
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
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            counter.incrementAndGet();
            return ToolResult.success("Executed " + name + " count: " + counter.get());
        }
    }
}
