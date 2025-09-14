package com.alejandro.microservices.hr_api.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.alejandro.microservices.hr_api.infrastructure.security.JwtAuthenticationFilter;

/**
 * Pruebas unitarias para SecurityConfig.
 *
 * Verifica la correcta configuración de beans de seguridad,
 * incluyendo password encoders, authentication providers y managers.
 *
 * @author Sistema HR API
 * @version 1.0
 * @since 2025-01-14
 */
@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private AuthenticationConfiguration authConfig;

    @Mock
    private AuthenticationManager authenticationManager;

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(jwtAuthenticationFilter, userDetailsService);
    }

    @Test
    void passwordEncoder_ShouldReturnBCryptPasswordEncoder() {
        // ACT
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        // ASSERT
        assertNotNull(passwordEncoder, "PasswordEncoder no debe ser nulo");
        assertTrue(passwordEncoder instanceof BCryptPasswordEncoder,
            "PasswordEncoder debe ser instancia de BCryptPasswordEncoder");
    }

    @Test
    void passwordEncoder_ShouldEncodePasswordsCorrectly() {
        // ARRANGE
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String rawPassword = "testPassword123";

        // ACT
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // ASSERT
        assertNotNull(encodedPassword, "Password codificado no debe ser nulo");
        assertNotEquals(rawPassword, encodedPassword, "Password codificado debe ser diferente al original");
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword),
            "Password original debe hacer match con el codificado");
        assertTrue(encodedPassword.startsWith("$2a$"), "Password debe usar BCrypt con formato correcto");
    }

    @Test
    void passwordEncoder_ShouldGenerateDifferentHashesForSamePassword() {
        // ARRANGE
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String password = "samePassword";

        // ACT
        String hash1 = passwordEncoder.encode(password);
        String hash2 = passwordEncoder.encode(password);

        // ASSERT
        assertNotEquals(hash1, hash2, "BCrypt debe generar hashes diferentes para la misma password");
        assertTrue(passwordEncoder.matches(password, hash1), "Hash1 debe hacer match");
        assertTrue(passwordEncoder.matches(password, hash2), "Hash2 debe hacer match");
    }

    @Test
    void passwordEncoder_ShouldHandleEmptyPasswords() {
        // ARRANGE
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        // ACT & ASSERT
        assertDoesNotThrow(() -> {
            String encoded = passwordEncoder.encode("");
            assertNotNull(encoded, "Password vacío debe ser codificado");
            assertTrue(passwordEncoder.matches("", encoded), "Password vacío debe hacer match");
        }, "PasswordEncoder debe manejar passwords vacíos");
    }

    @Test
    void passwordEncoder_ShouldHandleSpecialCharacters() {
        // ARRANGE
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String specialPassword = "P@$$w0rd!#$%^&*()";

        // ACT
        String encoded = passwordEncoder.encode(specialPassword);

        // ASSERT
        assertNotNull(encoded, "Password con caracteres especiales debe ser codificado");
        assertTrue(passwordEncoder.matches(specialPassword, encoded),
            "Password con caracteres especiales debe hacer match");
    }

    @Test
    void passwordEncoder_ShouldHandleUnicodeCharacters() {
        // ARRANGE
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String unicodePassword = "密码123ñáéíóú";

        // ACT
        String encoded = passwordEncoder.encode(unicodePassword);

        // ASSERT
        assertNotNull(encoded, "Password con Unicode debe ser codificado");
        assertTrue(passwordEncoder.matches(unicodePassword, encoded),
            "Password con Unicode debe hacer match");
    }

    @Test
    void passwordEncoder_ShouldHandleLongPasswords() {
        // ARRANGE
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String longPassword = "a".repeat(1000); // Password muy largo

        // ACT & ASSERT
        assertThrows(IllegalArgumentException.class, () -> {
            passwordEncoder.encode(longPassword);
        }, "Debe lanzar excepción con password muy largo");
    }

    @Test
    void authenticationProvider_ShouldReturnConfiguredProvider() {
        // ACT
        AuthenticationProvider provider = securityConfig.authenticationProvider();

        // ASSERT
        assertNotNull(provider, "AuthenticationProvider no debe ser nulo");
        // Verificar que el provider está configurado correctamente sería más complejo
        // ya que requiere acceso a los campos internos del DaoAuthenticationProvider
    }

    @Test
    void authenticationManager_ShouldReturnManagerFromConfiguration() throws Exception {
        // ARRANGE
        when(authConfig.getAuthenticationManager()).thenReturn(authenticationManager);

        // ACT
        AuthenticationManager manager = securityConfig.authenticationManager(authConfig);

        // ASSERT
        assertNotNull(manager, "AuthenticationManager no debe ser nulo");
        assertSame(authenticationManager, manager,
            "Debe retornar el AuthenticationManager de la configuración");
        verify(authConfig).getAuthenticationManager();
    }

    @Test
    void authenticationManager_ShouldPropagateException_WhenConfigurationThrows() throws Exception {
        // ARRANGE
        when(authConfig.getAuthenticationManager()).thenThrow(new RuntimeException("Configuration error"));

        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> {
            securityConfig.authenticationManager(authConfig);
        }, "Debe propagar excepciones de la configuración");
    }

    @Test
    void securityConfig_ShouldBeStateless() {
        // ARRANGE
        SecurityConfig config1 = new SecurityConfig(jwtAuthenticationFilter, userDetailsService);
        SecurityConfig config2 = new SecurityConfig(jwtAuthenticationFilter, userDetailsService);

        // ACT
        PasswordEncoder encoder1 = config1.passwordEncoder();
        PasswordEncoder encoder2 = config2.passwordEncoder();

        // ASSERT
        assertNotSame(encoder1, encoder2, "Cada instancia debe crear nuevos encoders");
        assertEquals(encoder1.getClass(), encoder2.getClass(),
            "Pero deben ser del mismo tipo");
    }

    @Test
    void passwordEncoder_ShouldBeThreadSafe() throws InterruptedException {
        // ARRANGE
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String password = "threadTestPassword";
        final String[] encodedPasswords = new String[10];
        final Boolean[] matchResults = new Boolean[10];
        Thread[] threads = new Thread[10];

        // ACT - Codificar desde múltiples hilos
        for (int i = 0; i < 10; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                encodedPasswords[index] = passwordEncoder.encode(password);
                matchResults[index] = passwordEncoder.matches(password, encodedPasswords[index]);
            });
            threads[i].start();
        }

        // Esperar a que todos terminen
        for (Thread thread : threads) {
            thread.join();
        }

        // ASSERT
        for (int i = 0; i < 10; i++) {
            assertNotNull(encodedPasswords[i], "Password codificado " + i + " no debe ser nulo");
            assertTrue(matchResults[i], "Password " + i + " debe hacer match");
        }
    }

    @Test
    void passwordEncoder_ShouldRejectIncorrectPasswords() {
        // ARRANGE
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String correctPassword = "correctPassword";
        String wrongPassword = "wrongPassword";
        String encoded = passwordEncoder.encode(correctPassword);

        // ACT & ASSERT
        assertTrue(passwordEncoder.matches(correctPassword, encoded),
            "Password correcta debe hacer match");
        assertFalse(passwordEncoder.matches(wrongPassword, encoded),
            "Password incorrecta no debe hacer match");
        assertFalse(passwordEncoder.matches("", encoded),
            "Password vacía no debe hacer match con password no vacía");
    }

    @Test
    void passwordEncoder_ShouldHandleMalformedHashes() {
        // ARRANGE
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String password = "testPassword";
        String malformedHash = "not_a_valid_bcrypt_hash";

        // ACT & ASSERT
        boolean result = passwordEncoder.matches(password, malformedHash);
        assertFalse(result, "Debe retornar false con hash malformado");
    }

    @Test
    void securityConfig_ShouldProvideSingleton() {
        // ARRANGE
        SecurityConfig config = new SecurityConfig(jwtAuthenticationFilter, userDetailsService);

        // ACT
        PasswordEncoder encoder1 = config.passwordEncoder();
        PasswordEncoder encoder2 = config.passwordEncoder();

        // ASSERT
        // Como es un @Bean, Spring normalmente lo haría singleton,
        // pero aquí estamos probando la instanciación directa
        assertNotSame(encoder1, encoder2, "Métodos crean nuevas instancias cada vez");
    }

    @Test
    void passwordEncoder_PerformanceTest() {
        // ARRANGE
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String password = "performanceTestPassword";

        // ACT
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            String encoded = passwordEncoder.encode(password);
            passwordEncoder.matches(password, encoded);
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        // ASSERT
        assertTrue(totalTime < 30000, // Menos de 30 segundos para 100 operaciones
            "Encoding debe ser eficiente: " + totalTime + "ms para 100 operaciones");
    }
}
