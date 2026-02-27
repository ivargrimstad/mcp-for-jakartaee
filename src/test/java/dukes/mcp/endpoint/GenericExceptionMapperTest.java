package dukes.mcp.endpoint;

import dukes.mcp.model.JsonRpcResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GenericExceptionMapper.
 */
class GenericExceptionMapperTest {
    
    private GenericExceptionMapper mapper;
    
    @BeforeEach
    void setUp() {
        mapper = new GenericExceptionMapper();
    }
    
    @Test
    void testToResponse_RuntimeException() {
        // Given
        RuntimeException exception = new RuntimeException("Test runtime error");
        
        // When
        Response response = mapper.toResponse(exception);
        
        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatus(), "Should return HTTP 200");
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
        
        JsonRpcResponse jsonRpcResponse = (JsonRpcResponse) response.getEntity();
        assertNotNull(jsonRpcResponse);
        assertTrue(jsonRpcResponse.isError());
        assertNull(jsonRpcResponse.getId(), "ID should be null when request is not available");
        assertEquals(-32603, jsonRpcResponse.getError().getCode());
        assertEquals("Internal error", jsonRpcResponse.getError().getMessage());
        assertEquals("Test runtime error", jsonRpcResponse.getError().getData());
    }
    
    @Test
    void testToResponse_CheckedException() {
        // Given
        Exception exception = new Exception("Test checked exception");
        
        // When
        Response response = mapper.toResponse(exception);
        
        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatus(), "Should return HTTP 200");
        
        JsonRpcResponse jsonRpcResponse = (JsonRpcResponse) response.getEntity();
        assertNotNull(jsonRpcResponse);
        assertTrue(jsonRpcResponse.isError());
        assertEquals(-32603, jsonRpcResponse.getError().getCode());
        assertEquals("Internal error", jsonRpcResponse.getError().getMessage());
        assertEquals("Test checked exception", jsonRpcResponse.getError().getData());
    }
    
    @Test
    void testToResponse_NullPointerException() {
        // Given
        NullPointerException exception = new NullPointerException("Null pointer error");
        
        // When
        Response response = mapper.toResponse(exception);
        
        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatus(), "Should return HTTP 200");
        
        JsonRpcResponse jsonRpcResponse = (JsonRpcResponse) response.getEntity();
        assertNotNull(jsonRpcResponse);
        assertTrue(jsonRpcResponse.isError());
        assertEquals(-32603, jsonRpcResponse.getError().getCode());
        assertEquals("Null pointer error", jsonRpcResponse.getError().getData());
    }
    
    @Test
    void testToResponse_ExceptionWithNullMessage() {
        // Given
        RuntimeException exception = new RuntimeException((String) null);
        
        // When
        Response response = mapper.toResponse(exception);
        
        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatus(), "Should return HTTP 200");
        
        JsonRpcResponse jsonRpcResponse = (JsonRpcResponse) response.getEntity();
        assertNotNull(jsonRpcResponse);
        assertTrue(jsonRpcResponse.isError());
        assertEquals(-32603, jsonRpcResponse.getError().getCode());
        assertEquals("Internal error", jsonRpcResponse.getError().getMessage());
        assertNull(jsonRpcResponse.getError().getData());
    }
    
    @Test
    void testToResponse_AlwaysReturnsHttp200() {
        // Given - various exception types
        Throwable[] exceptions = {
            new RuntimeException("Runtime error"),
            new IllegalArgumentException("Invalid argument"),
            new IllegalStateException("Invalid state"),
            new NullPointerException("Null pointer"),
            new Exception("Checked exception")
        };
        
        // When & Then - all should return HTTP 200
        for (Throwable exception : exceptions) {
            Response response = mapper.toResponse(exception);
            assertEquals(200, response.getStatus(), 
                "All exception responses should have HTTP 200 status per JSON-RPC 2.0 spec");
            
            JsonRpcResponse jsonRpcResponse = (JsonRpcResponse) response.getEntity();
            assertTrue(jsonRpcResponse.isError(), "Response should contain error");
            assertEquals(-32603, jsonRpcResponse.getError().getCode(), 
                "Should use internal error code -32603");
        }
    }
    
    @Test
    void testToResponse_JsonRpcResponseStructure() {
        // Given
        RuntimeException exception = new RuntimeException("Test error");
        
        // When
        Response response = mapper.toResponse(exception);
        JsonRpcResponse jsonRpcResponse = (JsonRpcResponse) response.getEntity();
        
        // Then - verify JSON-RPC response structure
        assertNotNull(jsonRpcResponse);
        assertEquals("2.0", jsonRpcResponse.getJsonrpc());
        assertNull(jsonRpcResponse.getResult(), "Error response should not have result");
        assertNotNull(jsonRpcResponse.getError(), "Error response should have error object");
        
        // Verify error object structure
        assertNotNull(jsonRpcResponse.getError().getCode());
        assertNotNull(jsonRpcResponse.getError().getMessage());
    }
}
