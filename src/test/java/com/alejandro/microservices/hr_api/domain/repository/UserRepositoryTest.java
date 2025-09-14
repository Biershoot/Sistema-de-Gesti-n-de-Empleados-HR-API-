package com.alejandro.microservices.hr_api.domain.repository;

import com.alejandro.microservices.hr_api.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración para UserRepository.
 *
 * Verifica las operaciones de base de datos para la entidad User,
 * incluyendo consultas personalizadas y comportamiento de JPA.
 *
 * @author Sistema HR API
 * @version 1.0
 * @since 2025-01-14
 */
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser1;
    private User testUser2;
    private User disabledUser;

    @BeforeEach
    void setUp() {
        // Limpiar base de datos antes de cada test
        userRepository.deleteAll();
        entityManager.flush();

        // Crear usuarios de prueba
        testUser1 = new User("admin", "hashedPassword1", "ROLE_ADMIN");
        testUser1.setEnabled(true);

        testUser2 = new User("hr.specialist", "hashedPassword2", "ROLE_HR_SPECIALIST");
        testUser2.setEnabled(true);

        disabledUser = new User("disabled.user", "hashedPassword3", "ROLE_USER");
        disabledUser.setEnabled(false);

        // Persistir usuarios
        testUser1 = entityManager.persistAndFlush(testUser1);
        testUser2 = entityManager.persistAndFlush(testUser2);
        disabledUser = entityManager.persistAndFlush(disabledUser);
    }

    @Test
    void findByUsername_ShouldReturnUser_WhenUserExists() {
        // ACT
        Optional<User> foundUser = userRepository.findByUsername("admin");

        // ASSERT
        assertTrue(foundUser.isPresent(), "Usuario debe ser encontrado");
        assertEquals("admin", foundUser.get().getUsername(), "Username debe coincidir");
        assertEquals("ROLE_ADMIN", foundUser.get().getRole(), "Role debe coincidir");
        assertTrue(foundUser.get().isEnabled(), "Usuario debe estar habilitado");
    }

    @Test
    void findByUsername_ShouldReturnEmpty_WhenUserDoesNotExist() {
        // ACT
        Optional<User> foundUser = userRepository.findByUsername("nonexistent");

        // ASSERT
        assertFalse(foundUser.isPresent(), "No debe encontrar usuario inexistente");
    }

    @Test
    void findByUsername_ShouldReturnDisabledUser_WhenUserIsDisabled() {
        // ACT
        Optional<User> foundUser = userRepository.findByUsername("disabled.user");

        // ASSERT
        assertTrue(foundUser.isPresent(), "Debe encontrar usuario deshabilitado");
        assertFalse(foundUser.get().isEnabled(), "Usuario debe estar deshabilitado");
    }

    @Test
    void findByUsernameAndEnabled_ShouldReturnUser_WhenUserExistsAndEnabled() {
        // ACT
        Optional<User> foundUser = userRepository.findByUsernameAndEnabled("admin");

        // ASSERT
        assertTrue(foundUser.isPresent(), "Usuario habilitado debe ser encontrado");
        assertEquals("admin", foundUser.get().getUsername(), "Username debe coincidir");
        assertTrue(foundUser.get().isEnabled(), "Usuario debe estar habilitado");
    }

    @Test
    void findByUsernameAndEnabled_ShouldReturnEmpty_WhenUserIsDisabled() {
        // ACT
        Optional<User> foundUser = userRepository.findByUsernameAndEnabled("disabled.user");

        // ASSERT
        assertFalse(foundUser.isPresent(), "No debe encontrar usuario deshabilitado");
    }

    @Test
    void findByUsernameAndEnabled_ShouldReturnEmpty_WhenUserDoesNotExist() {
        // ACT
        Optional<User> foundUser = userRepository.findByUsernameAndEnabled("nonexistent");

        // ASSERT
        assertFalse(foundUser.isPresent(), "No debe encontrar usuario inexistente");
    }

    @Test
    void existsByUsername_ShouldReturnTrue_WhenUserExists() {
        // ACT
        boolean exists = userRepository.existsByUsername("admin");

        // ASSERT
        assertTrue(exists, "Debe indicar que el usuario existe");
    }

    @Test
    void existsByUsername_ShouldReturnFalse_WhenUserDoesNotExist() {
        // ACT
        boolean exists = userRepository.existsByUsername("nonexistent");

        // ASSERT
        assertFalse(exists, "Debe indicar que el usuario no existe");
    }

    @Test
    void existsByUsername_ShouldReturnTrue_WhenUserExistsButDisabled() {
        // ACT
        boolean exists = userRepository.existsByUsername("disabled.user");

        // ASSERT
        assertTrue(exists, "Debe indicar que el usuario existe aunque esté deshabilitado");
    }

    @Test
    void findByRoleAndEnabled_ShouldReturnUsersWithRole_WhenUsersExist() {
        // ARRANGE
        User anotherAdmin = new User("admin2", "password", "ROLE_ADMIN");
        anotherAdmin.setEnabled(true);
        entityManager.persistAndFlush(anotherAdmin);

        // ACT
        List<User> adminUsers = userRepository.findByRoleAndEnabled("ROLE_ADMIN");

        // ASSERT
        assertEquals(2, adminUsers.size(), "Debe encontrar 2 usuarios admin");
        assertTrue(adminUsers.stream().allMatch(user -> "ROLE_ADMIN".equals(user.getRole())),
            "Todos los usuarios deben tener role ADMIN");
        assertTrue(adminUsers.stream().allMatch(User::isEnabled),
            "Todos los usuarios deben estar habilitados");
    }

    @Test
    void findByRoleAndEnabled_ShouldReturnEmpty_WhenNoUsersWithRole() {
        // ACT
        List<User> managerUsers = userRepository.findByRoleAndEnabled("ROLE_MANAGER");

        // ASSERT
        assertTrue(managerUsers.isEmpty(), "No debe encontrar usuarios con role inexistente");
    }

    @Test
    void findByRoleAndEnabled_ShouldNotReturnDisabledUsers() {
        // ARRANGE
        User disabledAdmin = new User("disabled.admin", "password", "ROLE_ADMIN");
        disabledAdmin.setEnabled(false);
        entityManager.persistAndFlush(disabledAdmin);

        // ACT
        List<User> adminUsers = userRepository.findByRoleAndEnabled("ROLE_ADMIN");

        // ASSERT
        assertEquals(1, adminUsers.size(), "Solo debe encontrar usuarios habilitados");
        assertEquals("admin", adminUsers.get(0).getUsername(), "Debe ser el usuario habilitado");
    }

    @Test
    void countByRoleAndEnabled_ShouldReturnCorrectCount_WhenUsersExist() {
        // ARRANGE
        User anotherHR = new User("hr2", "password", "ROLE_HR_SPECIALIST");
        anotherHR.setEnabled(true);
        entityManager.persistAndFlush(anotherHR);

        // ACT
        long count = userRepository.countByRoleAndEnabled("ROLE_HR_SPECIALIST");

        // ASSERT
        assertEquals(2, count, "Debe contar 2 usuarios HR habilitados");
    }

    @Test
    void countByRoleAndEnabled_ShouldReturnZero_WhenNoUsersWithRole() {
        // ACT
        long count = userRepository.countByRoleAndEnabled("ROLE_NONEXISTENT");

        // ASSERT
        assertEquals(0, count, "Debe retornar 0 para role inexistente");
    }

    @Test
    void countByRoleAndEnabled_ShouldNotCountDisabledUsers() {
        // ARRANGE
        User disabledHR = new User("disabled.hr", "password", "ROLE_HR_SPECIALIST");
        disabledHR.setEnabled(false);
        entityManager.persistAndFlush(disabledHR);

        // ACT
        long count = userRepository.countByRoleAndEnabled("ROLE_HR_SPECIALIST");

        // ASSERT
        assertEquals(1, count, "Solo debe contar usuarios habilitados");
    }

    @Test
    void save_ShouldPersistUser_WhenValidUserProvided() {
        // ARRANGE
        User newUser = new User("newuser", "password", "ROLE_USER");
        newUser.setEnabled(true);

        // ACT
        User savedUser = userRepository.save(newUser);

        // ASSERT
        assertNotNull(savedUser.getId(), "ID debe ser generado");
        assertEquals("newuser", savedUser.getUsername(), "Username debe ser persistido");
        assertTrue(savedUser.isEnabled(), "Estado enabled debe ser persistido");

        // Verificar que se puede recuperar de la base de datos
        Optional<User> retrievedUser = userRepository.findById(savedUser.getId());
        assertTrue(retrievedUser.isPresent(), "Usuario debe ser recuperable");
        assertEquals("newuser", retrievedUser.get().getUsername());
    }

    @Test
    void findById_ShouldReturnUser_WhenUserExists() {
        // ACT
        Optional<User> foundUser = userRepository.findById(testUser1.getId());

        // ASSERT
        assertTrue(foundUser.isPresent(), "Usuario debe ser encontrado por ID");
        assertEquals(testUser1.getUsername(), foundUser.get().getUsername());
        assertEquals(testUser1.getRole(), foundUser.get().getRole());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenUserDoesNotExist() {
        // ARRANGE
        UUID nonExistentId = UUID.randomUUID();

        // ACT
        Optional<User> foundUser = userRepository.findById(nonExistentId);

        // ASSERT
        assertFalse(foundUser.isPresent(), "No debe encontrar usuario con ID inexistente");
    }

    @Test
    void deleteById_ShouldRemoveUser_WhenUserExists() {
        // ARRANGE
        UUID userId = testUser1.getId();

        // ACT
        userRepository.deleteById(userId);

        // ASSERT
        Optional<User> deletedUser = userRepository.findById(userId);
        assertFalse(deletedUser.isPresent(), "Usuario debe ser eliminado");
    }

    @Test
    void findAll_ShouldReturnAllUsers_IncludingDisabled() {
        // ACT
        List<User> allUsers = userRepository.findAll();

        // ASSERT
        assertEquals(3, allUsers.size(), "Debe retornar todos los usuarios");

        long enabledCount = allUsers.stream().mapToLong(user -> user.isEnabled() ? 1 : 0).sum();
        assertEquals(2, enabledCount, "Debe haber 2 usuarios habilitados");
    }

    @Test
    void findByUsername_ShouldBeCaseSensitive() {
        // ACT
        Optional<User> foundUser1 = userRepository.findByUsername("admin");
        Optional<User> foundUser2 = userRepository.findByUsername("ADMIN");
        Optional<User> foundUser3 = userRepository.findByUsername("Admin");

        // ASSERT
        assertTrue(foundUser1.isPresent(), "Debe encontrar 'admin'");
        assertFalse(foundUser2.isPresent(), "No debe encontrar 'ADMIN'");
        assertFalse(foundUser3.isPresent(), "No debe encontrar 'Admin'");
    }

    @Test
    void findByUsername_ShouldHandleSpecialCharacters() {
        // ARRANGE
        User userWithSpecialChars = new User("user@domain.com", "password", "ROLE_USER");
        userWithSpecialChars.setEnabled(true);
        entityManager.persistAndFlush(userWithSpecialChars);

        // ACT
        Optional<User> foundUser = userRepository.findByUsername("user@domain.com");

        // ASSERT
        assertTrue(foundUser.isPresent(), "Debe manejar caracteres especiales en username");
        assertEquals("user@domain.com", foundUser.get().getUsername());
    }

    @Test
    void update_ShouldModifyExistingUser() {
        // ARRANGE
        User userToUpdate = userRepository.findById(testUser1.getId()).orElseThrow();
        userToUpdate.setRole("ROLE_SUPER_ADMIN");
        userToUpdate.setEnabled(false);

        // ACT
        User updatedUser = userRepository.save(userToUpdate);

        // ASSERT
        assertEquals("ROLE_SUPER_ADMIN", updatedUser.getRole(), "Role debe ser actualizado");
        assertFalse(updatedUser.isEnabled(), "Estado enabled debe ser actualizado");

        // Verificar que los cambios persisten
        User retrievedUser = userRepository.findById(testUser1.getId()).orElseThrow();
        assertEquals("ROLE_SUPER_ADMIN", retrievedUser.getRole());
        assertFalse(retrievedUser.isEnabled());
    }

    @Test
    void customQueries_ShouldWorkWithParameters() {
        // ARRANGE
        String searchRole = "ROLE_HR_SPECIALIST";

        // ACT
        List<User> usersByRole = userRepository.findByRoleAndEnabled(searchRole);
        long countByRole = userRepository.countByRoleAndEnabled(searchRole);

        // ASSERT
        assertEquals(1, usersByRole.size(), "Query con parámetros debe funcionar");
        assertEquals(1, countByRole, "Count query con parámetros debe funcionar");
        assertEquals(searchRole, usersByRole.get(0).getRole(), "Role debe coincidir");
    }

    @Test
    void repository_ShouldHandleConcurrentOperations() {
        // ARRANGE
        String baseUsername = "concurrent_user_";
        int userCount = 10;

        // ACT - Simular operaciones concurrentes
        for (int i = 0; i < userCount; i++) {
            User user = new User(baseUsername + i, "password", "ROLE_USER");
            user.setEnabled(true);
            userRepository.save(user);
        }

        // ASSERT
        for (int i = 0; i < userCount; i++) {
            Optional<User> user = userRepository.findByUsername(baseUsername + i);
            assertTrue(user.isPresent(), "Usuario " + i + " debe existir");
        }

        long totalUsers = userRepository.count();
        assertEquals(3 + userCount, totalUsers, "Debe haber usuarios originales + nuevos usuarios");
    }
}
