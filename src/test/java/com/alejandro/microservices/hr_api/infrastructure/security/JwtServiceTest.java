package com.alejandro.microservices.hr_api.infrastructure.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para JwtService.
 *
 * Valida la generación, validación y extracción de información de tokens JWT.
 */
class JwtServiceTest {

    private JwtService jwtService;
    private final String testUsername = "test@example.com";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ADMIN");

        // When
        String token = jwtService.generateToken(claims, testUsername);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT tiene 3 partes separadas por puntos
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        String token = jwtService.generateToken(claims, testUsername);

        // When
        String extractedUsername = jwtService.extractUsername(token);

        // Then
        assertEquals(testUsername, extractedUsername);
    }

    @Test
    void isTokenValid_ShouldReturnTrueForValidToken() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        String token = jwtService.generateToken(claims, testUsername);

        // When
        boolean isValid = jwtService.isTokenValid(token, testUsername);

        // Then
        assertTrue(isValid);
    }

    @Test
    void isTokenValid_ShouldReturnFalseForDifferentUsername() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        String token = jwtService.generateToken(claims, testUsername);

        // When
        boolean isValid = jwtService.isTokenValid(token, "different@example.com");

        // Then
        assertFalse(isValid);
    }

    @Test
    void extractClaim_ShouldReturnCorrectClaim() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("customClaim", "customValue");
        String token = jwtService.generateToken(claims, testUsername);

        // When
        String extractedClaim = jwtService.extractClaim(token,
            claims1 -> claims1.get("customClaim", String.class));

        // Then
        assertEquals("customValue", extractedClaim);
    }

    @Test
    void isTokenValid_ShouldReturnFalseForInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When & Then
        assertFalse(jwtService.isTokenValid(invalidToken, testUsername));
    }
}
