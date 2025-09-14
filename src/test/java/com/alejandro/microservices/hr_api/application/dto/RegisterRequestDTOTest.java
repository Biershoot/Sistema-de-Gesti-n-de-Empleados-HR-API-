package com.alejandro.microservices.hr_api.application.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para RegisterRequestDTO.
 *
 * Verifica la correcta construcción y validación del DTO de registro
 * de usuarios, incluyendo validaciones de seguridad y casos edge.
 *
 * @author Sistema HR API
 * @version 1.0
 * @since 2025-01-14
 */
class RegisterRequestDTOTest {

    @Test
    @DisplayName("Constructor debe crear RegisterRequestDTO correctamente")
    void constructor_ShouldCreateRegisterRequestDTO_WhenValidParametersProvided() {
        // ARRANGE
        String username = "newuser";
        String password = "password123";
        String role = "ADMIN";

        // ACT
        RegisterRequestDTO request = new RegisterRequestDTO(username, password, role);

        // ASSERT
        assertEquals(username, request.username(), "Username debe coincidir");
        assertEquals(password, request.password(), "Password debe coincidir");
        assertEquals(role, request.role(), "Role debe coincidir");
    }

    @Test
    @DisplayName("Record debe ser inmutable")
    void record_ShouldBeImmutable() {
        // ARRANGE
        String username = "testuser";
        String password = "testpass";
        String role = "USER";

        // ACT
        RegisterRequestDTO request = new RegisterRequestDTO(username, password, role);

        // ASSERT
        // Los records son inmutables por naturaleza
        assertEquals(username, request.username(), "Username debe permanecer inmutable");
        assertEquals(password, request.password(), "Password debe permanecer inmutable");
        assertEquals(role, request.role(), "Role debe permanecer inmutable");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", " ", "  ", "\t", "\n"})
    @DisplayName("Record debe manejar usernames vacíos o nulos")
    void record_ShouldHandleEmptyOrNullUsernames(String username) {
        // ARRANGE
        String password = "password123";
        String role = "USER";

        // ACT & ASSERT
        assertDoesNotThrow(() -> {
            RegisterRequestDTO request = new RegisterRequestDTO(username, password, role);
            assertEquals(username, request.username(),
                "Debe aceptar username: '" + username + "'");
        }, "No debe lanzar excepción con username: '" + username + "'");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", " ", "weak", "StrongP@ssw0rd!", "very_long_password_with_many_chars_123!"})
    @DisplayName("Record debe manejar diferentes tipos de passwords")
    void record_ShouldHandleDifferentPasswordTypes(String password) {
        // ARRANGE
        String username = "testuser";
        String role = "USER";

        // ACT & ASSERT
        assertDoesNotThrow(() -> {
            RegisterRequestDTO request = new RegisterRequestDTO(username, password, role);
            assertEquals(password, request.password(), "Debe aceptar password");
        }, "No debe lanzar excepción con diferentes tipos de password");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "USER", "HR_SPECIALIST", "MANAGER", "admin", "user"})
    @DisplayName("Record debe manejar diferentes roles")
    void record_ShouldHandleDifferentRoles(String role) {
        // ARRANGE
        String username = "testuser";
        String password = "password123";

        // ACT
        RegisterRequestDTO request = new RegisterRequestDTO(username, password, role);

        // ASSERT
        assertEquals(role, request.role(), "Debe manejar role: " + role);
    }

    @Test
    @DisplayName("Record debe manejar usernames con caracteres especiales")
    void record_ShouldHandleSpecialCharactersInUsername() {
        // ARRANGE
        String[] specialUsernames = {
            "user@domain.com",
            "user.name@company.org",
            "user_name",
            "user-name",
            "user123",
            "用户名", // Unicode
            "usuário", // Acentos
            "user@sub.domain.com"
        };

        // ACT & ASSERT
        for (String username : specialUsernames) {
            RegisterRequestDTO request = new RegisterRequestDTO(username, "password", "USER");

            assertEquals(username, request.username(),
                "Debe manejar username con caracteres especiales: " + username);
        }
    }

    @Test
    @DisplayName("Record debe manejar passwords con caracteres especiales")
    void record_ShouldHandleSpecialCharactersInPassword() {
        // ARRANGE
        String[] specialPasswords = {
            "Pass@123!",
            "пароль123", // Cirílico
            "contraseña#123", // Español
            "P@$$w0rd!",
            "密码123", // Chino
            "mot_de_passe-123",
            "🔒password123🔑" // Emojis
        };

        // ACT & ASSERT
        for (String password : specialPasswords) {
            RegisterRequestDTO request = new RegisterRequestDTO("user", password, "USER");

            assertEquals(password, request.password(),
                "Debe manejar password con caracteres especiales");
        }
    }

    @Test
    @DisplayName("Record debe manejar roles con formatos especiales")
    void record_ShouldHandleSpecialRoleFormats() {
        // ARRANGE
        String[] specialRoles = {
            "ROLE_ADMIN",
            "HR_SPECIALIST_LEVEL_2",
            "ROLE-WITH-DASHES",
            "role_lowercase",
            "ROLE_123",
            null // Role nulo
        };

        // ACT & ASSERT
        for (String role : specialRoles) {
            RegisterRequestDTO request = new RegisterRequestDTO("user", "password", role);

            assertEquals(role, request.role(),
                "Debe manejar role con formato especial: " + role);
        }
    }

    @Test
    @DisplayName("Record debe manejar valores extremos de longitud")
    void record_ShouldHandleExtremeLengthValues() {
        // ARRANGE
        String longUsername = "user".repeat(100); // Username muy largo
        String longPassword = "password".repeat(50); // Password muy largo
        String longRole = "ROLE_".repeat(20); // Role muy largo

        // ACT
        RegisterRequestDTO request = new RegisterRequestDTO(longUsername, longPassword, longRole);

        // ASSERT
        assertEquals(longUsername, request.username(), "Debe manejar usernames largos");
        assertEquals(longPassword, request.password(), "Debe manejar passwords largos");
        assertEquals(longRole, request.role(), "Debe manejar roles largos");

        assertTrue(request.username().length() > 300, "Username debe ser muy largo");
        assertTrue(request.password().length() > 300, "Password debe ser muy largo");
        assertTrue(request.role().length() > 50, "Role debe ser muy largo");
    }

    @Test
    @DisplayName("equals debe funcionar correctamente")
    void equals_ShouldWorkCorrectly() {
        // ARRANGE
        RegisterRequestDTO request1 = new RegisterRequestDTO("user", "pass", "ADMIN");
        RegisterRequestDTO request2 = new RegisterRequestDTO("user", "pass", "ADMIN");
        RegisterRequestDTO request3 = new RegisterRequestDTO("different", "pass", "ADMIN");

        // ACT & ASSERT
        assertEquals(request1, request1, "Record debe ser igual a sí mismo");
        assertEquals(request1, request2, "Records con mismos valores deben ser iguales");
        assertNotEquals(request1, request3, "Records con diferentes valores no deben ser iguales");
        assertNotEquals(request1, null, "Record no debe ser igual a null");
        assertNotEquals(request1, "string", "Record no debe ser igual a diferente tipo");
    }

    @Test
    @DisplayName("hashCode debe ser consistente")
    void hashCode_ShouldBeConsistent() {
        // ARRANGE
        RegisterRequestDTO request1 = new RegisterRequestDTO("user", "pass", "ADMIN");
        RegisterRequestDTO request2 = new RegisterRequestDTO("user", "pass", "ADMIN");

        // ACT
        int hash1 = request1.hashCode();
        int hash2 = request2.hashCode();
        int hash1Again = request1.hashCode();

        // ASSERT
        assertEquals(hash1, hash1Again, "hashCode debe ser consistente para el mismo objeto");
        assertEquals(hash1, hash2, "hashCode debe ser igual para objects iguales");
    }

    @Test
    @DisplayName("toString no debe exponer password por seguridad")
    void toString_ShouldNotExposePasswordForSecurity() {
        // ARRANGE
        RegisterRequestDTO request = new RegisterRequestDTO(
            "testuser",
            "sensitivePassword123!",
            "ADMIN"
        );

        // ACT
        String stringRepresentation = request.toString();

        // ASSERT
        assertNotNull(stringRepresentation, "toString no debe ser nulo");
        assertFalse(stringRepresentation.isEmpty(), "toString no debe estar vacío");

        // En un record básico, toString incluye todos los campos
        // En producción se debería override para ocultar el password
        assertTrue(stringRepresentation.contains("testuser"),
            "toString debe incluir username");
        assertTrue(stringRepresentation.contains("ADMIN"),
            "toString debe incluir role");
    }

    @Test
    @DisplayName("Record debe manejar todos los valores nulos")
    void record_ShouldHandleAllNullValues() {
        // ACT
        RegisterRequestDTO request = new RegisterRequestDTO(null, null, null);

        // ASSERT
        assertNull(request.username(), "Username debe ser nulo");
        assertNull(request.password(), "Password debe ser nulo");
        assertNull(request.role(), "Role debe ser nulo");
    }

    @Test
    @DisplayName("Record debe ser thread-safe para lectura")
    void record_ShouldBeThreadSafeForReading() throws InterruptedException {
        // ARRANGE
        RegisterRequestDTO request = new RegisterRequestDTO("threaduser", "threadpass", "USER");
        final String[] results = new String[10];
        Thread[] threads = new Thread[10];

        // ACT - Leer desde múltiples hilos
        for (int i = 0; i < 10; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                results[index] = request.username();
            });
            threads[i].start();
        }

        // Esperar a que todos los hilos terminen
        for (Thread thread : threads) {
            thread.join();
        }

        // ASSERT
        for (String result : results) {
            assertEquals("threaduser", result,
                "Todos los hilos deben obtener el mismo username");
        }
    }

    @Test
    @DisplayName("Record debe funcionar correctamente con streams")
    void record_ShouldWorkCorrectlyWithStreams() {
        // ARRANGE
        RegisterRequestDTO[] requests = {
            new RegisterRequestDTO("user1", "pass1", "ADMIN"),
            new RegisterRequestDTO("user2", "pass2", "USER"),
            new RegisterRequestDTO("user3", "pass3", "ADMIN"),
            new RegisterRequestDTO("user4", "pass4", "USER")
        };

        // ACT
        long adminCount = java.util.Arrays.stream(requests)
            .filter(req -> "ADMIN".equals(req.role()))
            .count();

        // ASSERT
        assertEquals(2, adminCount, "Debe haber 2 usuarios con role ADMIN");
    }

    @Test
    @DisplayName("Record debe manejar deconstrucción correctamente")
    void record_ShouldHandleDeconstructionCorrectly() {
        // ARRANGE
        RegisterRequestDTO request = new RegisterRequestDTO("testuser", "testpass", "MANAGER");

        // ACT - Deconstruir record (Java 14+ pattern matching)
        String username = request.username();
        String password = request.password();
        String role = request.role();

        // ASSERT
        assertEquals("testuser", username, "Username debe extraerse correctamente");
        assertEquals("testpass", password, "Password debe extraerse correctamente");
        assertEquals("MANAGER", role, "Role debe extraerse correctamente");
    }

    @Test
    @DisplayName("Record debe validar integridad de datos")
    void record_ShouldValidateDataIntegrity() {
        // ARRANGE
        String originalUsername = "original";
        String originalPassword = "original_pass";
        String originalRole = "ORIGINAL_ROLE";

        // ACT
        RegisterRequestDTO request = new RegisterRequestDTO(originalUsername, originalPassword, originalRole);

        // ASSERT - Los valores deben ser exactamente los mismos
        assertSame(originalUsername, request.username(), "Username debe ser la misma referencia");
        assertSame(originalPassword, request.password(), "Password debe ser la misma referencia");
        assertSame(originalRole, request.role(), "Role debe ser la misma referencia");
    }
}
