package dukes.mcp.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JsonRpcRequest.
 * 
 * Tests validation rules for jsonrpc field, method field, and id handling.
 * Validates Requirements 2.1, 2.2, 2.3, 2.4.
 */
class JsonRpcRequestTest {
    
    @Test
    @DisplayName("Valid request with all fields should pass validation")
    void testValidRequestWithAllFields() {
        JsonRpcRequest request = new JsonRpcRequest("test_method", null, "123");
        
        assertDoesNotThrow(request::validate);
        assertEquals("2.0", request.getJsonrpc());
        assertEquals("test_method", request.getMethod());
        assertEquals("123", request.getId());
    }
    
    @Test
    @DisplayName("Valid request without id (notification) should pass validation")
    void testValidRequestWithoutId() {
        JsonRpcRequest request = new JsonRpcRequest("test_method", null);
        
        assertDoesNotThrow(request::validate);
        assertEquals("2.0", request.getJsonrpc());
        assertEquals("test_method", request.getMethod());
        assertNull(request.getId());
        assertTrue(request.isNotification());
    }
    
    @Test
    @DisplayName("Request with null jsonrpc field should fail validation")
    void testNullJsonrpcField() {
        JsonRpcRequest request = new JsonRpcRequest();
        request.setJsonrpc(null);
        request.setMethod("test_method");
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            request::validate
        );
        assertEquals("jsonrpc field must be \"2.0\"", exception.getMessage());
    }
    
    @Test
    @DisplayName("Request with invalid jsonrpc version should fail validation")
    void testInvalidJsonrpcVersion() {
        JsonRpcRequest request = new JsonRpcRequest();
        request.setJsonrpc("1.0");
        request.setMethod("test_method");
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            request::validate
        );
        assertEquals("jsonrpc field must be \"2.0\"", exception.getMessage());
    }
    
    @Test
    @DisplayName("Request with empty jsonrpc field should fail validation")
    void testEmptyJsonrpcField() {
        JsonRpcRequest request = new JsonRpcRequest();
        request.setJsonrpc("");
        request.setMethod("test_method");
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            request::validate
        );
        assertEquals("jsonrpc field must be \"2.0\"", exception.getMessage());
    }
    
    @Test
    @DisplayName("Request with null method should fail validation")
    void testNullMethod() {
        JsonRpcRequest request = new JsonRpcRequest();
        request.setJsonrpc("2.0");
        request.setMethod(null);
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            request::validate
        );
        assertEquals("method field must be present and non-empty", exception.getMessage());
    }
    
    @Test
    @DisplayName("Request with empty method should fail validation")
    void testEmptyMethod() {
        JsonRpcRequest request = new JsonRpcRequest();
        request.setJsonrpc("2.0");
        request.setMethod("");
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            request::validate
        );
        assertEquals("method field must be present and non-empty", exception.getMessage());
    }
    
    @Test
    @DisplayName("Request with whitespace-only method should fail validation")
    void testWhitespaceOnlyMethod() {
        JsonRpcRequest request = new JsonRpcRequest();
        request.setJsonrpc("2.0");
        request.setMethod("   ");
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            request::validate
        );
        assertEquals("method field must be present and non-empty", exception.getMessage());
    }
    
    @Test
    @DisplayName("Request with string id should be valid")
    void testStringId() {
        JsonRpcRequest request = new JsonRpcRequest("test_method", null, "request-123");
        
        assertDoesNotThrow(request::validate);
        assertEquals("request-123", request.getId());
        assertFalse(request.isNotification());
    }
    
    @Test
    @DisplayName("Request with numeric id should be valid")
    void testNumericId() {
        JsonRpcRequest request = new JsonRpcRequest("test_method", null, 42);
        
        assertDoesNotThrow(request::validate);
        assertEquals(42, request.getId());
        assertFalse(request.isNotification());
    }
    
    @Test
    @DisplayName("Request with null id should be notification")
    void testNullIdIsNotification() {
        JsonRpcRequest request = new JsonRpcRequest("test_method", null, null);
        
        assertDoesNotThrow(request::validate);
        assertNull(request.getId());
        assertTrue(request.isNotification());
    }
    
    @Test
    @DisplayName("Request with params should preserve params")
    void testRequestWithParams() {
        Object params = new Object();
        JsonRpcRequest request = new JsonRpcRequest("test_method", params, "123");
        
        assertDoesNotThrow(request::validate);
        assertSame(params, request.getParams());
    }
    
    @Test
    @DisplayName("Request equality should work correctly")
    void testRequestEquality() {
        JsonRpcRequest request1 = new JsonRpcRequest("test_method", null, "123");
        JsonRpcRequest request2 = new JsonRpcRequest("test_method", null, "123");
        JsonRpcRequest request3 = new JsonRpcRequest("other_method", null, "123");
        
        assertEquals(request1, request2);
        assertNotEquals(request1, request3);
        assertEquals(request1.hashCode(), request2.hashCode());
    }
    
    @Test
    @DisplayName("Request toString should include all fields")
    void testRequestToString() {
        JsonRpcRequest request = new JsonRpcRequest("test_method", null, "123");
        String str = request.toString();
        
        assertTrue(str.contains("jsonrpc='2.0'"));
        assertTrue(str.contains("method='test_method'"));
        assertTrue(str.contains("id=123"));
    }
}
