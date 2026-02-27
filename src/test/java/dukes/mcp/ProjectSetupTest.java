package dukes.mcp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic test to verify project setup and testing framework.
 */
class ProjectSetupTest {

    @Test
    @DisplayName("JUnit 5 is working")
    void testJUnit5() {
        assertTrue(true, "JUnit 5 should be working");
    }

    @Test
    @DisplayName("Jakarta EE API is available")
    void testJakartaEEAvailable() {
        assertDoesNotThrow(() -> {
            Class.forName("jakarta.ws.rs.core.Application");
            Class.forName("jakarta.enterprise.context.ApplicationScoped");
            Class.forName("jakarta.json.bind.Jsonb");
        }, "Jakarta EE 11 APIs should be available");
    }

    @Test
    @DisplayName("JSON Schema validator is available")
    void testJsonSchemaValidatorAvailable() {
        assertDoesNotThrow(() -> {
            Class.forName("com.networknt.schema.JsonSchemaFactory");
        }, "JSON Schema validator should be available");
    }

    @Test
    @DisplayName("Mockito is available")
    void testMockitoAvailable() {
        assertDoesNotThrow(() -> {
            Class.forName("org.mockito.Mockito");
        }, "Mockito should be available");
    }

    @Test
    @DisplayName("Arquillian is available")
    void testArquillianAvailable() {
        assertDoesNotThrow(() -> {
            Class.forName("org.jboss.arquillian.junit5.ArquillianExtension");
        }, "Arquillian should be available");
    }

    @Test
    @DisplayName("jqwik is available")
    void testJqwikAvailable() {
        assertDoesNotThrow(() -> {
            Class.forName("net.jqwik.api.Property");
        }, "jqwik should be available");
    }

    @Test
    @DisplayName("REST Assured is available")
    void testRestAssuredAvailable() {
        assertDoesNotThrow(() -> {
            Class.forName("io.restassured.RestAssured");
        }, "REST Assured should be available");
    }
}
