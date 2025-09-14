package com.alejandro.microservices.hr_api.application.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para LoginRequestDTO.
 *
 * Verifica la correcta construcción y validación del DTO de request
 * de autenticación, incluyendo casos edge y validaciones de seguridad.
 *
 * @author Sistema HR API
 * @version 1.0
 * @since 2025-01-14
 */
class LoginRequestDTOTest {

    @Test
    @DisplayName("Constructor debe crear LoginRequestDTO correctamente")
    void constructor_ShouldCreateLoginRequestDTO_WhenValidParametersProvided() {
        // ARRANGE
        String username = "testuser";
        String password = "password123";

        // ACT
        LoginRequestDTO request = new LoginRequestDTO(username, password);

        // ASSERT
        assertEquals(username, request.username());
        assertEquals(password, request.password());
    }

    @Test
    @DisplayName("Constructor debe manejar username nulo")
    void constructor_ShouldHandleNullUsername() {
        // ARRANGE
        String username = null;
        String password = "password123";

        // ACT
        LoginRequestDTO request = new LoginRequestDTO(username, password);

        // ASSERT
        assertNull(request.username());
        assertEquals(password, request.password());
    }

    @Test
    @DisplayName("Constructor debe manejar password nulo")
    void constructor_ShouldHandleNullPassword() {
        // ARRANGE
        String username = "testuser";
        String password = null;

        // ACT
        LoginRequestDTO request = new LoginRequestDTO(username, password);

        // ASSERT
        assertEquals(username, request.username());
        assertNull(request.password());
    }

    @Test
    @DisplayName("Constructor debe manejar username vacío")
    void constructor_ShouldHandleEmptyUsername() {
        // ARRANGE
        String username = "";
        String password = "password123";

        // ACT
        LoginRequestDTO request = new LoginRequestDTO(username, password);

        // ASSERT
        assertEquals("", request.username());
        assertEquals(password, request.password());
    }

    @Test
    @DisplayName("Constructor debe manejar password vacío")
    void constructor_ShouldHandleEmptyPassword() {
        // ARRANGE
        String username = "testuser";
        String password = "";

        // ACT
        LoginRequestDTO request = new LoginRequestDTO(username, password);

        // ASSERT
        assertEquals(username, request.username());
        assertEquals("", request.password());
    }

    @Test
    @DisplayName("Record debe ser immutable")
    void record_ShouldBeImmutable() {
        // ARRANGE
        String username = "testuser";
        String password = "password123";

        // ACT
        LoginRequestDTO request1 = new LoginRequestDTO(username, password);
        LoginRequestDTO request2 = new LoginRequestDTO(username, password);

        // ASSERT
        assertEquals(request1.username(), request2.username());
        assertEquals(request1.password(), request2.password());
        assertEquals(request1, request2);
    }

    @Test
    @DisplayName("toString debe incluir información relevante")
    void toString_ShouldIncludeRelevantInformation() {
        // ARRANGE
        String username = "testuser";
        String password = "password123";

        // ACT
        LoginRequestDTO request = new LoginRequestDTO(username, password);
        String stringRepresentation = request.toString();

        // ASSERT
        assertNotNull(stringRepresentation);
        assertTrue(stringRepresentation.contains("LoginRequestDTO"));
        assertTrue(stringRepresentation.contains(username));
        // La contraseña debería aparecer en toString, pero en producción se debería ofuscar
    }

    @Test
    @DisplayName("hashCode debe ser consistente")
    void hashCode_ShouldBeConsistent() {
        // ARRANGE
        String username = "testuser";
        String password = "password123";

        // ACT
        LoginRequestDTO request = new LoginRequestDTO(username, password);
        int hashCode1 = request.hashCode();
        int hashCode2 = request.hashCode();

        // ASSERT
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    @DisplayName("equals debe funcionar correctamente")
    void equals_ShouldWorkCorrectly() {
        // ARRANGE
        LoginRequestDTO request1 = new LoginRequestDTO("user1", "pass1");
        LoginRequestDTO request2 = new LoginRequestDTO("user1", "pass1");
        LoginRequestDTO request3 = new LoginRequestDTO("user2", "pass2");

        // ASSERT
        assertEquals(request1, request2);
        assertNotEquals(request1, request3);
        assertNotEquals(request2, request3);
    }
}
