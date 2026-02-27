package dukes.mcp.endpoint;

import dukes.mcp.model.JsonRpcResponse;
import jakarta.json.bind.JsonbException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JsonParseExceptionMapper.
 */
class JsonParseExceptionMapperTest {
    
    private JsonParseExceptionMapper mapper;
    
    @BeforeEach
    void setUp() {
        mapper = new JsonParseExceptionMapper();
    }
    
    @Test
    void testToResponse_ReturnsHttp200() {
        // Given
        JsonbException exception = new JsonbException("Invalid JSON syntax");
        
        // When
        Response response = mapper.toResponse(exception);
        
        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatus(), 
            "Should return HTTP 200 per JSON-RPC 2.0 specification");
    }
    
    @Test
    void testToResponse_ReturnsJsonRpcError() {
        // Given
        JsonbException exception = new JsonbException("Unexpected character");
        
        // When
        Response response = mapper.toResponse(exception);
        
        // Then
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity() instanceof JsonRpcResponse);
        
        JsonRpcResponse jsonRpcResponse = (JsonRpcResponse) response.getEntity();
        assertTrue(jsonRpcResponse.isError(), "Response should be an error");
        assertNull(jsonRpcResponse.getId(), "ID should be null since request couldn't be parsed");
    }
    
    @Test
    void testToResponse_UsesParseErrorCode() {
        // Given
        JsonbException exception = new JsonbException("Malformed JSON");
        
        // When
        Response response = mapper.toResponse(exception);
        JsonRpcResponse jsonRpcResponse = (JsonRpcResponse) response.getEntity();
        
        // Then
        assertEquals(-32700, jsonRpcResponse.getError().getCode(),
            "Should use error code -32700 for parse errors");
        assertEquals("Parse error", jsonRpcResponse.getError().getMessage());
    }
    
    @Test
    void testToResponse_IncludesErrorDetails() {
        // Given
        String errorMessage = "Unexpected end of JSON input";
        JsonbException exception = new JsonbException(errorMessage);
        
        // When
        Response response = mapper.toResponse(exception);
        JsonRpcResponse jsonRpcResponse = (JsonRpcResponse) response.getEntity();
        
        // Then
        assertNotNull(jsonRpcResponse.getError().getData());
        String errorData = (String) jsonRpcResponse.getError().getData();
        assertTrue(errorData.contains(errorMessage),
            "Error data should include the original exception message");
    }
    
    @Test
    void testToResponse_ReturnsJsonContentType() {
        // Given
        JsonbException exception = new JsonbException("Invalid JSON");
        
        // When
        Response response = mapper.toResponse(exception);
        
        // Then
        assertEquals("application/json", response.getMediaType().toString(),
            "Response should have application/json content type");
    }
}
