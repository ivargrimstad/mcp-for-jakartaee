package dukes.mcp;

import dukes.mcp.model.JsonRpcError;
import dukes.mcp.model.JsonRpcRequest;
import dukes.mcp.model.JsonRpcResponse;
import net.jqwik.api.*;
import net.jqwik.api.constraints.NotBlank;
import net.jqwik.api.constraints.NotEmpty;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for JSON-RPC 2.0 protocol compliance.
 * 
 * These tests validate:
 * - Property 1: JSON-RPC Protocol Compliance
 * - Property 2: JSON-RPC Request ID Round-Trip
 * 
 * Validates Requirements: 1.2, 1.4, 1.5, 2.1, 2.2, 2.3, 2.4
 */
class JsonRpcProtocolProperties {
    
    /**
     * Property 1: JSON-RPC Protocol Compliance
     * 
     * For any valid JSON-RPC request, the server must return a valid JSON-RPC response
     * with matching request ID, proper structure (either result or error field but not both),
     * and correct protocol version.
     * 
     * Validates: Requirements 1.2, 1.4, 1.5, 2.1, 2.2, 2.3, 2.4
     */
    @Property
    void validJsonRpcRequestProducesValidResponse(
            @ForAll("validJsonRpcRequests") JsonRpcRequest request) {
        
        // Simulate processing: create a response for the request
        JsonRpcResponse response = createResponseForRequest(request);
        
        // Assert: Response must be valid JSON-RPC 2.0
        assertNotNull(response, "Response must not be null");
        assertEquals("2.0", response.getJsonrpc(), 
                "Response jsonrpc field must be '2.0'");
        
        // Assert: Response must have either result or error, but not both
        boolean hasResult = response.getResult() != null;
        boolean hasError = response.getError() != null;
        assertTrue(hasResult || hasError, 
                "Response must have either result or error");
        assertFalse(hasResult && hasError, 
                "Response must not have both result and error");
        
        // Assert: Response ID must match request ID (if request has ID)
        if (request.getId() != null) {
            assertEquals(request.getId(), response.getId(), 
                    "Response ID must match request ID");
        }
        
        // Assert: Response must pass validation
        assertDoesNotThrow(() -> response.validate(), 
                "Response must be valid according to JSON-RPC 2.0 spec");
    }
    
    /**
     * Property 2: JSON-RPC Request ID Round-Trip
     * 
     * For any JSON-RPC request with an "id" field, the response must contain
     * the exact same "id" value.
     * 
     * Validates: Requirement 2.3
     */
    @Property
    void requestIdIsPreservedInResponse(
            @ForAll @NotBlank String method,
            @ForAll("requestIds") Object requestId) {
        
        // Create request with specific ID
        JsonRpcRequest request = new JsonRpcRequest(method, null, requestId);
        
        // Simulate processing: create response
        JsonRpcResponse successResponse = JsonRpcResponse.success(request.getId(), "result");
        JsonRpcResponse errorResponse = JsonRpcResponse.error(request.getId(), 
                new JsonRpcError(-32603, "error"));
        
        // Assert: Both success and error responses preserve the ID
        assertEquals(requestId, successResponse.getId(), 
                "Success response must preserve request ID");
        assertEquals(requestId, errorResponse.getId(), 
                "Error response must preserve request ID");
        
        // Assert: ID type and value are exactly the same
        assertSame(requestId.getClass(), successResponse.getId().getClass(),
                "Response ID must have same type as request ID");
        assertSame(requestId.getClass(), errorResponse.getId().getClass(),
                "Response ID must have same type as request ID");
    }
    
    /**
     * Property: Invalid JSON-RPC version is rejected
     * 
     * Validates: Requirement 2.1
     */
    @Property
    void invalidJsonRpcVersionIsRejected(
            @ForAll("invalidJsonRpcVersions") String invalidVersion,
            @ForAll @NotBlank String method) {
        
        JsonRpcRequest request = new JsonRpcRequest(method, null);
        request.setJsonrpc(invalidVersion);
        
        // Assert: Validation must fail for invalid version
        assertThrows(IllegalArgumentException.class, 
                () -> request.validate(),
                "Request with invalid jsonrpc version must fail validation");
    }
    
    /**
     * Property: Empty or null method is rejected
     * 
     * Validates: Requirement 2.2
     */
    @Property
    void emptyOrNullMethodIsRejected(
            @ForAll("emptyOrNullStrings") String invalidMethod) {
        
        JsonRpcRequest request = new JsonRpcRequest();
        request.setJsonrpc("2.0");
        request.setMethod(invalidMethod);
        
        // Assert: Validation must fail for invalid method
        assertThrows(IllegalArgumentException.class, 
                () -> request.validate(),
                "Request with empty or null method must fail validation");
    }
    
    /**
     * Property: Response with both result and error is invalid
     * 
     * Validates: Requirement 2.4
     */
    @Property
    void responseCannotHaveBothResultAndError(@ForAll("requestIds") Object id) {
        
        JsonRpcResponse response = new JsonRpcResponse(id);
        
        // Manually set both result and error (bypassing setters that enforce exclusivity)
        response.setResult("some result");
        // Force error to be set despite result being present
        JsonRpcError error = new JsonRpcError(-32603, "error");
        try {
            java.lang.reflect.Field errorField = JsonRpcResponse.class.getDeclaredField("error");
            errorField.setAccessible(true);
            errorField.set(response, error);
        } catch (Exception e) {
            fail("Failed to set error field via reflection: " + e.getMessage());
        }
        
        // Assert: Validation must fail when both result and error are present
        assertThrows(IllegalArgumentException.class, 
                () -> response.validate(),
                "Response with both result and error must fail validation");
    }
    
    /**
     * Property: Response must have either result or error
     * 
     * Validates: Requirement 2.4
     */
    @Property
    void responseMustHaveResultOrError(@ForAll("requestIds") Object id) {
        
        JsonRpcResponse response = new JsonRpcResponse(id);
        // Don't set result or error
        
        // Assert: Validation must fail when neither result nor error is present
        assertThrows(IllegalArgumentException.class, 
                () -> response.validate(),
                "Response without result or error must fail validation");
    }
    
    // ========== Arbitraries (Data Generators) ==========
    
    /**
     * Generates valid JSON-RPC 2.0 requests.
     */
    @Provide
    Arbitrary<JsonRpcRequest> validJsonRpcRequests() {
        return Combinators.combine(
                methods(),
                params(),
                requestIds().injectNull(0.2) // 20% chance of null (notification)
        ).as((method, params, id) -> {
            JsonRpcRequest request = new JsonRpcRequest(method, params, id);
            return request;
        });
    }
    
    /**
     * Generates valid method names.
     */
    @Provide
    Arbitrary<String> methods() {
        return Arbitraries.of(
                "initialize",
                "tools/list",
                "tools/call",
                "resources/list",
                "resources/read",
                "prompts/list",
                "prompts/get",
                "custom/method",
                "test/method"
        );
    }
    
    /**
     * Generates various parameter types.
     */
    @Provide
    Arbitrary<Object> params() {
        return Arbitraries.oneOf(
                Arbitraries.just(null),
                Arbitraries.strings(),
                Arbitraries.integers(),
                Arbitraries.maps(Arbitraries.strings(), Arbitraries.strings()),
                Arbitraries.of(Map.of("key", "value")),
                Arbitraries.of(List.of("item1", "item2"))
        );
    }
    
    /**
     * Generates valid request IDs (string, number, or null).
     */
    @Provide
    Arbitrary<Object> requestIds() {
        return Arbitraries.oneOf(
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50),
                Arbitraries.integers(),
                Arbitraries.longs(),
                Arbitraries.doubles()
        );
    }
    
    /**
     * Generates invalid JSON-RPC versions.
     */
    @Provide
    Arbitrary<String> invalidJsonRpcVersions() {
        return Arbitraries.of(
                null,
                "",
                "1.0",
                "2.1",
                "3.0",
                "2",
                "2.0.0",
                "JSON-RPC 2.0"
        );
    }
    
    /**
     * Generates empty or null strings.
     */
    @Provide
    Arbitrary<String> emptyOrNullStrings() {
        return Arbitraries.of(
                null,
                "",
                "   ",
                "\t",
                "\n"
        );
    }
    
    // ========== Helper Methods ==========
    
    /**
     * Simulates creating a response for a request.
     * In a real implementation, this would be done by the protocol handler.
     */
    private JsonRpcResponse createResponseForRequest(JsonRpcRequest request) {
        // Validate request first
        try {
            request.validate();
        } catch (IllegalArgumentException e) {
            // Invalid request gets error response
            return JsonRpcResponse.error(request.getId(), -32600, "Invalid Request");
        }
        
        // For valid requests, return success response
        return JsonRpcResponse.success(request.getId(), Map.of("status", "ok"));
    }
}
