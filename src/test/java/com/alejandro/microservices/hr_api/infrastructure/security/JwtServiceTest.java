package com.alejandro.microservices.hr_api.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para JwtService.
 *
 * Verifica la generación, validación y extracción de claims de tokens JWT.
 * Incluye pruebas para diferentes escenarios de seguridad y casos edge.
 *
 * @author Sistema HR API
 * @version 1.0
 * @since 2025-01-14
 */
@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;
    private final String testSecretKey = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long testExpiration = 3600000; // 1 hora

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", testSecretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", testExpiration);
    }

    @Test
    void generateToken_ShouldCreateValidToken_WhenUsernameProvided() {
        // ARRANGE
        String username = "testuser";

        // ACT
        String token = jwtService.generateToken(username);

        // ASSERT
        assertNotNull(token, "Token no debe ser nulo");
        assertFalse(token.isEmpty(), "Token no debe estar vacío");
        assertTrue(token.contains("."), "Token debe tener formato JWT válido");

        // Verificar que el token se puede parsear
        String extractedUsername = jwtService.extractUsername(token);
        assertEquals(username, extractedUsername, "Username extraído debe coincidir");
    }

    @Test
    void generateToken_ShouldCreateValidTokenWithClaims_WhenExtraClaimsProvided() {
        // ARRANGE
        String username = "testuser";
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "ADMIN");
        extraClaims.put("department", "IT");

        // ACT
        String token = jwtService.generateToken(extraClaims, username);

        // ASSERT
        assertNotNull(token, "Token no debe ser nulo");

        // Verificar claims
        Claims claims = jwtService.extractAllClaims(token);
        assertEquals(username, claims.getSubject(), "Subject debe ser el username");
        assertEquals("ADMIN", claims.get("role"), "Claim 'role' debe estar presente");
        assertEquals("IT", claims.get("department"), "Claim 'department' debe estar presente");
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername_WhenValidTokenProvided() {
        // ARRANGE
        String username = "user@example.com";
        String token = jwtService.generateToken(username);

        // ACT
        String extractedUsername = jwtService.extractUsername(token);

        // ASSERT
        assertEquals(username, extractedUsername, "Username extraído debe coincidir exactamente");
    }

    @Test
    void extractExpiration_ShouldReturnFutureDate_WhenTokenGenerated() {
        // ARRANGE
        String username = "testuser";
        String token = jwtService.generateToken(username);

        // ACT
        Date expiration = jwtService.extractExpiration(token);

        // ASSERT
        assertNotNull(expiration, "Fecha de expiración no debe ser nula");
        assertTrue(expiration.after(new Date()), "Token debe tener fecha de expiración futura");

        // Verificar que la expiración es aproximadamente la esperada (con margen de 10 segundos)
        long expectedExpiration = System.currentTimeMillis() + testExpiration;
        long actualExpiration = expiration.getTime();
        assertTrue(Math.abs(expectedExpiration - actualExpiration) < 10000,
            "Expiración debe estar dentro del rango esperado");
    }

    @Test
    void isTokenValid_ShouldReturnTrue_WhenTokenIsValidAndUserMatches() {
        // ARRANGE
        String username = "validuser";
        String token = jwtService.generateToken(username);
        UserDetails userDetails = User.builder()
            .username(username)
            .password("password")
            .authorities(Collections.emptyList())
            .build();

        // ACT
        boolean isValid = jwtService.isTokenValid(token, userDetails.getUsername());

        // ASSERT
        assertTrue(isValid, "Token debe ser válido para el usuario correcto");
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenUsernameDoesNotMatch() {
        // ARRANGE
        String tokenUsername = "tokenuser";
        String userDetailsUsername = "differentuser";
        String token = jwtService.generateToken(tokenUsername);
        UserDetails userDetails = User.builder()
            .username(userDetailsUsername)
            .password("password")
            .authorities(Collections.emptyList())
            .build();

        // ACT
        boolean isValid = jwtService.isTokenValid(token, userDetails.getUsername());

        // ASSERT
        assertFalse(isValid, "Token no debe ser válido para usuario diferente");
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenTokenIsExpired() {
        // ARRANGE
        String username = "testuser";

        // Crear servicio con expiración muy corta
        JwtService shortExpirationService = new JwtService();
        ReflectionTestUtils.setField(shortExpirationService, "secretKey", testSecretKey);
        ReflectionTestUtils.setField(shortExpirationService, "jwtExpiration", 1L); // 1ms

        String token = shortExpirationService.generateToken(username);

        // Esperar a que expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        UserDetails userDetails = User.builder()
            .username(username)
            .password("password")
            .authorities(Collections.emptyList())
            .build();

        // ACT
        boolean isValid = shortExpirationService.isTokenValid(token, userDetails.getUsername());

        // ASSERT
        assertFalse(isValid, "Token expirado no debe ser válido");
    }

    @Test
    void isTokenExpired_ShouldReturnFalse_WhenTokenIsValid() {
        // ARRANGE
        String username = "testuser";
        String token = jwtService.generateToken(username);

        // ACT
        boolean isExpired = jwtService.isTokenExpired(token);

        // ASSERT
        assertFalse(isExpired, "Token recién generado no debe estar expirado");
    }

    @Test
    void extractAllClaims_ShouldReturnAllClaims_WhenTokenHasMultipleClaims() {
        // ARRANGE
        String username = "testuser";
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "ADMIN");
        extraClaims.put("permissions", "READ,WRITE,DELETE");
        extraClaims.put("department", "HR");

        String token = jwtService.generateToken(extraClaims, username);

        // ACT
        Claims claims = jwtService.extractAllClaims(token);

        // ASSERT
        assertNotNull(claims, "Claims no deben ser nulos");
        assertEquals(username, claims.getSubject(), "Subject debe coincidir");
        assertEquals("ADMIN", claims.get("role"), "Role claim debe estar presente");
        assertEquals("READ,WRITE,DELETE", claims.get("permissions"), "Permissions claim debe estar presente");
        assertEquals("HR", claims.get("department"), "Department claim debe estar presente");
        assertNotNull(claims.getIssuedAt(), "Fecha de emisión debe estar presente");
        assertNotNull(claims.getExpiration(), "Fecha de expiración debe estar presente");
    }

    @Test
    void generateToken_ShouldHandleSpecialCharactersInUsername() {
        // ARRANGE
        String specialUsername = "user@domain.com";

        // ACT
        String token = jwtService.generateToken(specialUsername);
        String extractedUsername = jwtService.extractUsername(token);

        // ASSERT
        assertEquals(specialUsername, extractedUsername,
            "Debe manejar caracteres especiales en username correctamente");
    }

    @Test
    void generateToken_ShouldHandleUnicodeInUsername() {
        // ARRANGE
        String unicodeUsername = "用户名";

        // ACT
        String token = jwtService.generateToken(unicodeUsername);
        String extractedUsername = jwtService.extractUsername(token);

        // ASSERT
        assertEquals(unicodeUsername, extractedUsername,
            "Debe manejar caracteres Unicode en username correctamente");
    }

    @Test
    void generateToken_ShouldCreateDifferentTokens_WhenCalledMultipleTimes() {
        // ARRANGE
        String username = "testuser";

        // ACT
        String token1 = jwtService.generateToken(username);
        
        // Pequeño delay para asegurar diferentes timestamps
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        String token2 = jwtService.generateToken(username);

        // ASSERT
        assertNotEquals(token1, token2, "Tokens generados en diferentes momentos deben ser distintos");

        // Pero ambos deben ser válidos para el mismo usuario
        assertEquals(username, jwtService.extractUsername(token1));
        assertEquals(username, jwtService.extractUsername(token2));
    }

    @Test
    void generateToken_ShouldHandleNullExtraClaims() {
        // ARRANGE
        String username = "testuser";
        Map<String, Object> nullClaims = null;

        // ACT & ASSERT
        assertDoesNotThrow(() -> {
            String token = jwtService.generateToken(nullClaims, username);
            assertNotNull(token, "Token debe generarse incluso con claims nulos");
            assertEquals(username, jwtService.extractUsername(token));
        }, "Debe manejar claims nulos sin lanzar excepción");
    }

    @Test
    void generateToken_ShouldHandleEmptyExtraClaims() {
        // ARRANGE
        String username = "testuser";
        Map<String, Object> emptyClaims = new HashMap<>();

        // ACT
        String token = jwtService.generateToken(emptyClaims, username);

        // ASSERT
        assertNotNull(token, "Token debe generarse con claims vacíos");
        assertEquals(username, jwtService.extractUsername(token));
    }

    @Test
    void extractClaim_ShouldReturnCorrectValue_WhenClaimExists() {
        // ARRANGE
        String username = "testuser";
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("customClaim", "customValue");

        String token = jwtService.generateToken(extraClaims, username);

        // ACT
        String customValue = jwtService.extractClaim(token, claims -> (String) claims.get("customClaim"));

        // ASSERT
        assertEquals("customValue", customValue, "Debe extraer claim personalizado correctamente");
    }

    @Test
    void generateToken_ShouldHandleLongUsername() {
        // ARRANGE
        String longUsername = "a".repeat(255); // Username muy largo

        // ACT
        String token = jwtService.generateToken(longUsername);
        String extractedUsername = jwtService.extractUsername(token);

        // ASSERT
        assertEquals(longUsername, extractedUsername,
            "Debe manejar usernames largos correctamente");
    }

    @Test
    void isTokenValid_ShouldHandleNullUserDetails() {
        // ARRANGE
        String username = "testuser";
        String token = jwtService.generateToken(username);

        // ACT & ASSERT
        assertThrows(NullPointerException.class, () -> {
            jwtService.isTokenValid(token, null);
        }, "Debe lanzar excepción con UserDetails nulo");
    }

    @Test
    void extractUsername_ShouldHandleCorruptedToken() {
        // ARRANGE
        String corruptedToken = "eyJhbGciOiJIUzI1NiJ9.corrupted.token";

        // ACT & ASSERT
        assertThrows(Exception.class, () -> {
            jwtService.extractUsername(corruptedToken);
        }, "Debe lanzar excepción con token corrupto");
    }

    @Test
    void generateToken_ShouldIncludeStandardClaims() {
        // ARRANGE
        String username = "testuser";

        // ACT
        String token = jwtService.generateToken(username);
        Claims claims = jwtService.extractAllClaims(token);

        // ASSERT
        assertNotNull(claims.getSubject(), "Subject claim debe estar presente");
        assertNotNull(claims.getIssuedAt(), "IssuedAt claim debe estar presente");
        assertNotNull(claims.getExpiration(), "Expiration claim debe estar presente");
        assertEquals(username, claims.getSubject(), "Subject debe ser el username");
        assertTrue(claims.getIssuedAt().before(claims.getExpiration()),
            "IssuedAt debe ser anterior a Expiration");
    }

    @Test
    void tokenSigning_ShouldBeConsistent() {
        // ARRANGE
        String username = "testuser";
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "USER");

        // ACT - Generar dos tokens con los mismos datos pero en momentos diferentes
        String token1 = jwtService.generateToken(claims, username);

        try {
            Thread.sleep(10); // Pausa para asegurar diferente timestamp
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String token2 = jwtService.generateToken(claims, username);

        // ASSERT
        // Los tokens deben ser diferentes debido al timestamp diferente
        assertNotEquals(token1, token2, "Tokens con timestamps diferentes deben ser distintos");

        // Pero ambos deben ser válidos y contener la misma información
        assertEquals(username, jwtService.extractUsername(token1));
        assertEquals(username, jwtService.extractUsername(token2));
        assertEquals("USER", jwtService.extractAllClaims(token1).get("role"));
        assertEquals("USER", jwtService.extractAllClaims(token2).get("role"));
    }

    @Test
    void performanceTest_ShouldGenerateTokensEfficiently() {
        // ARRANGE
        String username = "performanceuser";
        int tokenCount = 1000;

        // ACT
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < tokenCount; i++) {
            String token = jwtService.generateToken(username + i);
            assertNotNull(token, "Token " + i + " debe ser válido");
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        // ASSERT
        assertTrue(totalTime < 5000, // Menos de 5 segundos para 1000 tokens
            "Generación de tokens debe ser eficiente: " + totalTime + "ms para " + tokenCount + " tokens");
    }
}
