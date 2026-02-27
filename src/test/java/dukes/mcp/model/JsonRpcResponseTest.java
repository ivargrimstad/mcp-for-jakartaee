package dukes.mcp.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JsonRpcResponse.
 * 
 * Tests validation rules for jsonrpc field, mutual exclusivity of result and error fields,
 * and id matching between request and response.
 * Validates Requirements 2.1, 2.2, 2.3, 2.4.
 */
class JsonRpcResponseTest {
    
    @Test
    @DisplayName("Valid success response should pass validation")
    void testValidSuccessResponse() {
        JsonRpcResponse response = JsonRpcResponse.success("123", "result_value");
        
        assertDoesNotThrow(response::validate);
        assertEquals("2.0", response.getJsonrpc());
        assertEquals("result_value", response.getResult());
        assertNull(response.getError());
        assertEquals("123", response.getId());
        assertTrue(response.isSuccess());
        assertFalse(response.isError());
    }
    
    @Test
    @DisplayName("Valid error response should pass validation")
    void testValidErrorResponse() {
        JsonRpcError error = new JsonRpcError(-32600, "Invalid Request");
        JsonRpcResponse response = JsonRpcResponse.error("123", error);
        
        assertDoesNotThrow(response::validate);
        assertEquals("2.0", response.getJsonrpc());
        assertNull(response.getResult());
        assertEquals(error, response.getError());
        assertEquals("123", response.getId());
        assertTrue(response.isError());
        assertFalse(response.isSuccess());
    }
    
    @Test
    @DisplayName("Error response with code and message should be valid")
    void testErrorResponseWithCodeAndMessage() {
        JsonRpcResponse response = JsonRpcResponse.error("123", -32601, "Method not found");
        
        assertDoesNotThrow(response::validate);
        assertEquals("2.0", response.getJsonrpc());
        assertNull(response.getResult());
        assertNotNull(response.getError());
        assertEquals(-32601, response.getError().getCode());
        assertEquals("Method not found", response.getError().getMessage());
        assertEquals("123", response.getId());
    }
    
    @Test
    @DisplayName("Error response with code, message, and data should be valid")
    void testErrorResponseWithCodeMessageAndData() {
        Object errorData = "additional error details";
        JsonRpcResponse response = JsonRpcResponse.error("123", -32603, "Internal error", errorData);
        
        assertDoesNotThrow(response::validate);
        assertEquals("2.0", response.getJsonrpc());
        assertNull(response.getResult());
        assertNotNull(response.getError());
        assertEquals(-32603, response.getError().getCode());
        assertEquals("Internal error", response.getError().getMessage());
        assertEquals(errorData, response.getError().getData());
        assertEquals("123", response.getId());
    }
    
    @Test
    @DisplayName("Response with null jsonrpc field should fail validation")
    void testNullJsonrpcField() {
        JsonRpcResponse response = new JsonRpcResponse("123");
        response.setJsonrpc(null);
        response.setResult("result");
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            response::validate
        );
        assertEquals("jsonrpc field must be \"2.0\"", exception.getMessage());
    }
    
    @Test
    @DisplayName("Response with invalid jsonrpc version should fail validation")
    void testInvalidJsonrpcVersion() {
        JsonRpcResponse response = new JsonRpcResponse("123");
        response.setJsonrpc("1.0");
        response.setResult("result");
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            response::validate
        );
        assertEquals("jsonrpc field must be \"2.0\"", exception.getMessage());
    }
    
    @Test
    @DisplayName("Mutual exclusivity is enforced by setters")
    void testMutualExclusivityEnforcement() {
        // The JsonRpcResponse class enforces mutual exclusivity through its setters:
        // - setResult() clears error
        // - setError() clears result
        // This test verifies that the setters properly enforce this constraint
        
        // Start with an error response
        JsonRpcResponse response = JsonRpcResponse.error("123", -32600, "Invalid Request");
        assertNotNull(response.getError());
        assertNull(response.getResult());
        
        // Setting result should clear error
        response.setResult("new_result");
        assertNotNull(response.getResult());
        assertNull(response.getError());
        
        // Setting error should clear result
        response.setError(new JsonRpcError(-32601, "Method not found"));
        assertNull(response.getResult());
        assertNotNull(response.getError());
        
        // The validation would catch any manual manipulation that bypasses setters
        assertDoesNotThrow(response::validate);
    }
    
    @Test
    @DisplayName("Response with neither result nor error should fail validation")
    void testNeitherResultNorError() {
        JsonRpcResponse response = new JsonRpcResponse("123");
        response.setJsonrpc("2.0");
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            response::validate
        );
        assertEquals("response must contain either result or error", exception.getMessage());
    }
    
    @Test
    @DisplayName("Setting result should clear error (mutual exclusivity)")
    void testSetResultClearsError() {
        JsonRpcResponse response = JsonRpcResponse.error("123", -32600, "Invalid Request");
        assertNotNull(response.getError());
        
        response.setResult("new_result");
        
        assertEquals("new_result", response.getResult());
        assertNull(response.getError());
        assertTrue(response.isSuccess());
        assertFalse(response.isError());
    }
    
    @Test
    @DisplayName("Setting error should clear result (mutual exclusivity)")
    void testSetErrorClearsResult() {
        JsonRpcResponse response = JsonRpcResponse.success("123", "result_value");
        assertNotNull(response.getResult());
        
        JsonRpcError error = new JsonRpcError(-32601, "Method not found");
        response.setError(error);
        
        assertEquals(error, response.getError());
        assertNull(response.getResult());
        assertTrue(response.isError());
        assertFalse(response.isSuccess());
    }
    
    @Test
    @DisplayName("Response id should match request id (string)")
    void testIdMatchingWithString() {
        String requestId = "request-123";
        JsonRpcResponse response = JsonRpcResponse.success(requestId, "result");
        
        assertEquals(requestId, response.getId());
    }
    
    @Test
    @DisplayName("Response id should match request id (number)")
    void testIdMatchingWithNumber() {
        Integer requestId = 42;
        JsonRpcResponse response = JsonRpcResponse.success(requestId, "result");
        
        assertEquals(requestId, response.getId());
    }
    
    @Test
    @DisplayName("Response id should match request id (null)")
    void testIdMatchingWithNull() {
        JsonRpcResponse response = JsonRpcResponse.success(null, "result");
        
        assertNull(response.getId());
    }
    
    @Test
    @DisplayName("Response with null result value should fail validation")
    void testNullResultValue() {
        JsonRpcResponse response = JsonRpcResponse.success("123", null);
        
        // Note: null result is treated as absence of result, so validation should fail
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            response::validate
        );
        assertEquals("response must contain either result or error", exception.getMessage());
    }
    
    @Test
    @DisplayName("Response equality should work correctly")
    void testResponseEquality() {
        JsonRpcResponse response1 = JsonRpcResponse.success("123", "result");
        JsonRpcResponse response2 = JsonRpcResponse.success("123", "result");
        JsonRpcResponse response3 = JsonRpcResponse.success("456", "result");
        
        assertEquals(response1, response2);
        assertNotEquals(response1, response3);
        assertEquals(response1.hashCode(), response2.hashCode());
    }
    
    @Test
    @DisplayName("Response toString should include all fields")
    void testResponseToString() {
        JsonRpcResponse response = JsonRpcResponse.success("123", "result_value");
        String str = response.toString();
        
        assertTrue(str.contains("jsonrpc='2.0'"));
        assertTrue(str.contains("result=result_value"));
        assertTrue(str.contains("id=123"));
    }
    
    @Test
    @DisplayName("Error response toString should include error")
    void testErrorResponseToString() {
        JsonRpcResponse response = JsonRpcResponse.error("123", -32600, "Invalid Request");
        String str = response.toString();
        
        assertTrue(str.contains("jsonrpc='2.0'"));
        assertTrue(str.contains("error="));
        assertTrue(str.contains("id=123"));
    }
}
