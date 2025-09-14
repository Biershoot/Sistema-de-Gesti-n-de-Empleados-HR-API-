package com.alejandro.microservices.hr_api.application.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para AuthResponseDTO.
 *
 * Verifica la correcta construcción y funcionamiento del DTO de respuesta
 * de autenticación, incluyendo validaciones y casos edge.
 *
 * @author Sistema HR API
 * @version 1.0
 * @since 2025-01-14
 */
class AuthResponseDTOTest {

    @Test
    @DisplayName("Constructor debe crear AuthResponseDTO correctamente con todos los parámetros")
    void constructor_ShouldCreateAuthResponseDTO_WhenAllParametersProvided() {
        // ARRANGE
        String token = "eyJhbGciOiJIUzI1NiJ9.test.token";
        String username = "testuser";
        List<String> roles = List.of("ROLE_ADMIN", "ROLE_USER");
        long expiresIn = 3600;

        // ACT
        AuthResponseDTO response = new AuthResponseDTO(token, username, roles, expiresIn);

        // ASSERT
        assertEquals(token, response.token(), "Token debe coincidir");
        assertEquals(username, response.username(), "Username debe coincidir");
        assertEquals(roles, response.roles(), "Roles deben coincidir");
        assertEquals(expiresIn, response.expiresIn(), "ExpiresIn debe coincidir");
    }

    @Test
    @DisplayName("Constructor debe manejar token nulo")
    void constructor_ShouldHandleNullToken() {
        // ARRANGE
        String token = null;
        String username = "testuser";
        List<String> roles = List.of("ROLE_USER");
        long expiresIn = 3600;

        // ACT
        AuthResponseDTO response = new AuthResponseDTO(token, username, roles, expiresIn);

        // ASSERT
        assertNull(response.token(), "Token debe ser nulo");
        assertEquals(username, response.username(), "Username debe estar presente");
    }

    @Test
    @DisplayName("Constructor debe manejar lista de roles vacía")
    void constructor_ShouldHandleEmptyRoles() {
        // ARRANGE
        String token = "valid.token";
        String username = "testuser";
        List<String> roles = List.of();
        long expiresIn = 3600;

        // ACT
        AuthResponseDTO response = new AuthResponseDTO(token, username, roles, expiresIn);

        // ASSERT
        assertNotNull(response.roles(), "Roles no debe ser nulo");
        assertTrue(response.roles().isEmpty(), "Roles debe estar vacío");
    }

    @Test
    @DisplayName("Constructor de conveniencia debe funcionar con un solo rol")
    void convenientConstructor_ShouldWork_WithSingleRole() {
        // ARRANGE
        String token = "valid.token";
        String username = "testuser";
        String role = "ROLE_USER";
        long expiresIn = 3600;

        // ACT
        AuthResponseDTO response = new AuthResponseDTO(token, username, role, expiresIn);

        // ASSERT
        assertEquals(token, response.token());
        assertEquals(username, response.username());
        assertEquals(List.of(role), response.roles());
        assertEquals(expiresIn, response.expiresIn());
    }

    @Test
    @DisplayName("Constructor debe manejar expiresIn cero")
    void constructor_ShouldHandleZeroExpiresIn() {
        // ARRANGE
        String token = "valid.token";
        String username = "testuser";
        List<String> roles = List.of("ROLE_USER");
        long expiresIn = 0;

        // ACT
        AuthResponseDTO response = new AuthResponseDTO(token, username, roles, expiresIn);

        // ASSERT
        assertEquals(0, response.expiresIn());
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, -3600, Long.MIN_VALUE})
    @DisplayName("Constructor debe manejar expiresIn negativos")
    void constructor_ShouldHandleNegativeExpiresIn(long expiresIn) {
        // ARRANGE
        String token = "valid.token";
        String username = "testuser";
        List<String> roles = List.of("ROLE_USER");

        // ACT
        AuthResponseDTO response = new AuthResponseDTO(token, username, roles, expiresIn);

        // ASSERT
        assertEquals(expiresIn, response.expiresIn());
    }

    @Test
    @DisplayName("toString debe incluir información relevante")
    void toString_ShouldIncludeRelevantInformation() {
        // ARRANGE
        String token = "test.token";
        String username = "testuser";
        List<String> roles = List.of("ROLE_USER");
        long expiresIn = 3600;

        // ACT
        AuthResponseDTO response = new AuthResponseDTO(token, username, roles, expiresIn);
        String stringRepresentation = response.toString();

        // ASSERT
        assertNotNull(stringRepresentation);
        assertTrue(stringRepresentation.contains("AuthResponseDTO"));
    }
}
