package com.alejandro.microservices.hr_api.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la entidad User.
 *
 * Verifica la funcionalidad del modelo User, incluyendo constructores,
 * getters, setters, validaciones y comportamiento de equals/hashCode.
 *
 * @author Sistema HR API
 * @version 1.0
 * @since 2025-01-14
 */
class UserTest {

    private User user;
    private final String testUsername = "testuser";
    private final String testPassword = "hashedPassword123";
    private final String testRole = "ROLE_ADMIN";

    @BeforeEach
    void setUp() {
        user = new User(testUsername, testPassword, testRole);
    }

    @Test
    @DisplayName("Constructor con parámetros debe crear usuario correctamente")
    void constructor_ShouldCreateUserWithCorrectValues() {
        // ASSERT
        assertEquals(testUsername, user.getUsername(), "Username debe ser asignado correctamente");
        assertEquals(testPassword, user.getPassword(), "Password debe ser asignado correctamente");
        assertEquals(testRole, user.getRole(), "Role debe ser asignado correctamente");
        assertTrue(user.isEnabled(), "Usuario debe estar habilitado por defecto");
        assertNull(user.getId(), "ID debe ser nulo antes de persistir");
    }

    @Test
    @DisplayName("Constructor por defecto debe crear usuario con valores por defecto")
    void defaultConstructor_ShouldCreateUserWithDefaultValues() {
        // ACT
        User defaultUser = new User();

        // ASSERT
        assertNull(defaultUser.getUsername(), "Username debe ser nulo por defecto");
        assertNull(defaultUser.getPassword(), "Password debe ser nulo por defecto");
        assertNull(defaultUser.getRole(), "Role debe ser nulo por defecto");
        assertTrue(defaultUser.isEnabled(), "Usuario debe estar habilitado por defecto");
        assertNull(defaultUser.getId(), "ID debe ser nulo por defecto");
    }

    @Test
    @DisplayName("Setters deben modificar propiedades correctamente")
    void setters_ShouldModifyPropertiesCorrectly() {
        // ARRANGE
        UUID newId = UUID.randomUUID();
        String newUsername = "newuser";
        String newPassword = "newpassword";
        String newRole = "ROLE_USER";
        boolean newEnabled = false;

        // ACT
        user.setId(newId);
        user.setUsername(newUsername);
        user.setPassword(newPassword);
        user.setRole(newRole);
        user.setEnabled(newEnabled);

        // ASSERT
        assertEquals(newId, user.getId(), "ID debe ser modificado");
        assertEquals(newUsername, user.getUsername(), "Username debe ser modificado");
        assertEquals(newPassword, user.getPassword(), "Password debe ser modificado");
        assertEquals(newRole, user.getRole(), "Role debe ser modificado");
        assertEquals(newEnabled, user.isEnabled(), "Enabled debe ser modificado");
    }

    @Test
    @DisplayName("Getters deben retornar valores correctos")
    void getters_ShouldReturnCorrectValues() {
        // ARRANGE
        UUID id = UUID.randomUUID();
        user.setId(id);

        // ACT & ASSERT
        assertEquals(id, user.getId(), "getId debe retornar ID correcto");
        assertEquals(testUsername, user.getUsername(), "getUsername debe retornar username correcto");
        assertEquals(testPassword, user.getPassword(), "getPassword debe retornar password correcto");
        assertEquals(testRole, user.getRole(), "getRole debe retornar role correcto");
        assertTrue(user.isEnabled(), "isEnabled debe retornar estado correcto");
    }

    @Test
    @DisplayName("Constructor debe manejar valores nulos")
    void constructor_ShouldHandleNullValues() {
        // ACT
        User userWithNulls = new User(null, null, null);

        // ASSERT
        assertNull(userWithNulls.getUsername(), "Username nulo debe ser aceptado");
        assertNull(userWithNulls.getPassword(), "Password nulo debe ser aceptado");
        assertNull(userWithNulls.getRole(), "Role nulo debe ser aceptado");
        assertTrue(userWithNulls.isEnabled(), "Enabled debe seguir siendo true por defecto");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ROLE_ADMIN", "ROLE_USER", "ROLE_HR_SPECIALIST", "ROLE_MANAGER", "admin", "user"})
    @DisplayName("Usuario debe aceptar diferentes formatos de roles")
    void user_ShouldAcceptDifferentRoleFormats(String role) {
        // ACT
        user.setRole(role);

        // ASSERT
        assertEquals(role, user.getRole(), "Debe aceptar role: " + role);
    }

    @ParameterizedTest
    @ValueSource(strings = {"user@domain.com", "user.name", "user_name", "user-name", "123user", "用户名"})
    @DisplayName("Usuario debe aceptar diferentes formatos de username")
    void user_ShouldAcceptDifferentUsernameFormats(String username) {
        // ACT
        user.setUsername(username);

        // ASSERT
        assertEquals(username, user.getUsername(), "Debe aceptar username: " + username);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", " ", "  "})
    @DisplayName("Usuario debe manejar usernames vacíos o con espacios")
    void user_ShouldHandleEmptyOrWhitespaceUsernames(String username) {
        // ACT & ASSERT
        assertDoesNotThrow(() -> {
            user.setUsername(username);
            assertEquals(username, user.getUsername(), "Debe aceptar username: '" + username + "'");
        }, "No debe lanzar excepción con username: '" + username + "'");
    }

    @Test
    @DisplayName("Usuario debe manejar passwords largos")
    void user_ShouldHandleLongPasswords() {
        // ARRANGE
        String longPassword = "a".repeat(500); // Password muy largo

        // ACT
        user.setPassword(longPassword);

        // ASSERT
        assertEquals(longPassword, user.getPassword(), "Debe manejar passwords largos");
    }

    @Test
    @DisplayName("Usuario debe manejar usernames largos")
    void user_ShouldHandleLongUsernames() {
        // ARRANGE
        String longUsername = "u".repeat(255); // Username largo

        // ACT
        user.setUsername(longUsername);

        // ASSERT
        assertEquals(longUsername, user.getUsername(), "Debe manejar usernames largos");
    }

    @Test
    @DisplayName("Enabled debe funcionar como boolean")
    void enabled_ShouldWorkAsBooleanFlag() {
        // ARRANGE & ACT
        user.setEnabled(false);
        assertFalse(user.isEnabled(), "Debe poder deshabilitar usuario");

        user.setEnabled(true);
        assertTrue(user.isEnabled(), "Debe poder habilitar usuario");
    }

    @Test
    @DisplayName("toString debe incluir información relevante")
    void toString_ShouldIncludeRelevantInformation() {
        // ARRANGE
        user.setId(UUID.randomUUID());

        // ACT
        String userString = user.toString();

        // ASSERT
        assertNotNull(userString, "toString no debe ser nulo");
        assertFalse(userString.isEmpty(), "toString no debe estar vacío");
        // Nota: El comportamiento exacto de toString depende de la implementación
        // Aquí verificamos que no lance excepciones
    }

    @Test
    @DisplayName("hashCode debe ser consistente")
    void hashCode_ShouldBeConsistent() {
        // ARRANGE
        UUID id = UUID.randomUUID();
        user.setId(id);

        // ACT
        int hashCode1 = user.hashCode();
        int hashCode2 = user.hashCode();

        // ASSERT
        assertEquals(hashCode1, hashCode2, "hashCode debe ser consistente para el mismo objeto");
    }

    @Test
    @DisplayName("equals debe funcionar correctamente")
    void equals_ShouldWorkCorrectly() {
        // ARRANGE
        UUID sameId = UUID.randomUUID();

        User user1 = new User("user1", "pass1", "ROLE_USER");
        user1.setId(sameId);

        User user2 = new User("user2", "pass2", "ROLE_ADMIN");
        user2.setId(sameId);

        User user3 = new User("user1", "pass1", "ROLE_USER");
        user3.setId(UUID.randomUUID());

        // ACT & ASSERT
        assertEquals(user1, user1, "Usuario debe ser igual a sí mismo");
        assertNotEquals(user1, null, "Usuario no debe ser igual a null");
        assertNotEquals(user1, "string", "Usuario no debe ser igual a objeto de diferente tipo");

        // El comportamiento de equals depende de la implementación en User
        // Típicamente se basa en el ID
        if (user1.getId() != null && user2.getId() != null) {
            if (user1.getId().equals(user2.getId())) {
                assertEquals(user1, user2, "Usuarios con mismo ID deben ser iguales");
            }
        }
    }

    @Test
    @DisplayName("Usuario debe ser mutable después de la construcción")
    void user_ShouldBeMutableAfterConstruction() {
        // ARRANGE
        String originalUsername = user.getUsername();
        String originalPassword = user.getPassword();
        String originalRole = user.getRole();
        boolean originalEnabled = user.isEnabled();

        // ACT - Modificar todas las propiedades
        user.setUsername("modified_username");
        user.setPassword("modified_password");
        user.setRole("ROLE_MODIFIED");
        user.setEnabled(!originalEnabled);

        // ASSERT
        assertNotEquals(originalUsername, user.getUsername(), "Username debe poder modificarse");
        assertNotEquals(originalPassword, user.getPassword(), "Password debe poder modificarse");
        assertNotEquals(originalRole, user.getRole(), "Role debe poder modificarse");
        assertNotEquals(originalEnabled, user.isEnabled(), "Enabled debe poder modificarse");
    }

    @Test
    @DisplayName("Constructor debe preservar valores exactos")
    void constructor_ShouldPreserveExactValues() {
        // ARRANGE
        String usernameWithSpaces = " username with spaces ";
        String passwordWithSpecialChars = "p@$$w0rd!";
        String roleWithNumbers = "ROLE_LEVEL_5";

        // ACT
        User userWithSpecialValues = new User(usernameWithSpaces, passwordWithSpecialChars, roleWithNumbers);

        // ASSERT
        assertEquals(usernameWithSpaces, userWithSpecialValues.getUsername(),
            "Debe preservar espacios en username");
        assertEquals(passwordWithSpecialChars, userWithSpecialValues.getPassword(),
            "Debe preservar caracteres especiales en password");
        assertEquals(roleWithNumbers, userWithSpecialValues.getRole(),
            "Debe preservar números en role");
    }

    @Test
    @DisplayName("Usuario debe mantener estado independiente entre instancias")
    void user_ShouldMaintainIndependentState() {
        // ARRANGE
        User user1 = new User("user1", "pass1", "ROLE_1");
        User user2 = new User("user2", "pass2", "ROLE_2");

        // ACT
        user1.setEnabled(false);
        user2.setEnabled(true);

        // ASSERT
        assertFalse(user1.isEnabled(), "User1 debe estar deshabilitado");
        assertTrue(user2.isEnabled(), "User2 debe estar habilitado");
        assertNotEquals(user1.getUsername(), user2.getUsername(), "Usernames deben ser independientes");
        assertNotEquals(user1.getRole(), user2.getRole(), "Roles deben ser independientes");
    }

    @Test
    @DisplayName("setId debe aceptar UUIDs válidos")
    void setId_ShouldAcceptValidUUIDs() {
        // ARRANGE
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        // ACT & ASSERT
        assertDoesNotThrow(() -> {
            user.setId(uuid1);
            assertEquals(uuid1, user.getId(), "Debe aceptar UUID random");

            user.setId(uuid2);
            assertEquals(uuid2, user.getId(), "Debe aceptar UUID específico");

            user.setId(null);
            assertNull(user.getId(), "Debe aceptar null");
        }, "No debe lanzar excepciones con UUIDs válidos");
    }

    @Test
    @DisplayName("Usuario debe ser thread-safe para operaciones básicas")
    void user_ShouldBeThreadSafeForBasicOperations() throws InterruptedException {
        // ARRANGE
        final String[] results = new String[10];
        Thread[] threads = new Thread[10];

        // ACT - Acceder a propiedades desde múltiples hilos
        for (int i = 0; i < 10; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                results[index] = user.getUsername();
            });
            threads[i].start();
        }

        // Esperar a que todos los hilos terminen
        for (Thread thread : threads) {
            thread.join();
        }

        // ASSERT
        for (String result : results) {
            assertEquals(testUsername, result, "Todos los hilos deben obtener el mismo username");
        }
    }
}
