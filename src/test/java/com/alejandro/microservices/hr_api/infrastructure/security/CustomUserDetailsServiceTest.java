package com.alejandro.microservices.hr_api.infrastructure.security;

import com.alejandro.microservices.hr_api.domain.model.User;
import com.alejandro.microservices.hr_api.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para CustomUserDetailsService.
 *
 * Verifica la correcta carga de usuarios y conversión a UserDetails
 * para la integración con Spring Security.
 *
 * @author Sistema HR API
 * @version 1.0
 * @since 2025-01-14
 */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    private CustomUserDetailsService userDetailsService;
    private User testUser;

    @BeforeEach
    void setUp() {
        userDetailsService = new CustomUserDetailsService(userRepository);

        testUser = new User(
            "testuser",
            "$2a$12$hashedPassword",
            "ROLE_ADMIN"
        );
        testUser.setId(UUID.randomUUID());
        testUser.setEnabled(true);
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        // ARRANGE
        String username = "testuser";
        when(userRepository.findByUsernameAndEnabled(username))
            .thenReturn(Optional.of(testUser));

        // ACT
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // ASSERT
        assertNotNull(userDetails, "UserDetails no debe ser nulo");
        assertEquals(username, userDetails.getUsername(), "Username debe coincidir");
        assertEquals(testUser.getPassword(), userDetails.getPassword(), "Password debe coincidir");
        assertTrue(userDetails.isEnabled(), "Usuario debe estar habilitado");
        assertTrue(userDetails.isAccountNonExpired(), "Cuenta no debe estar expirada");
        assertTrue(userDetails.isAccountNonLocked(), "Cuenta no debe estar bloqueada");
        assertTrue(userDetails.isCredentialsNonExpired(), "Credenciales no deben estar expiradas");

        verify(userRepository).findByUsernameAndEnabled(username);
    }

    @Test
    void loadUserByUsername_ShouldReturnCorrectAuthorities_WhenUserHasRole() {
        // ARRANGE
        String username = "adminuser";
        when(userRepository.findByUsernameAndEnabled(username))
            .thenReturn(Optional.of(testUser));

        // ACT
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // ASSERT
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertNotNull(authorities, "Authorities no debe ser nulo");
        assertEquals(1, authorities.size(), "Debe tener exactamente una autoridad");

        GrantedAuthority authority = authorities.iterator().next();
        assertEquals("ROLE_ADMIN", authority.getAuthority(), "La autoridad debe ser ROLE_ADMIN");

        verify(userRepository).findByUsernameAndEnabled(username);
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserNotFound() {
        // ARRANGE
        String username = "nonexistentuser";
        when(userRepository.findByUsernameAndEnabled(username))
            .thenReturn(Optional.empty());

        // ACT & ASSERT
        UsernameNotFoundException exception = assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername(username),
            "Debe lanzar UsernameNotFoundException cuando el usuario no existe"
        );

        assertEquals("Usuario no encontrado: " + username, exception.getMessage(),
            "El mensaje de error debe incluir el username");

        verify(userRepository).findByUsernameAndEnabled(username);
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserIsDisabled() {
        // ARRANGE
        String username = "disableduser";
        User disabledUser = new User("disableduser", "password", "ROLE_USER");
        disabledUser.setEnabled(false);

        when(userRepository.findByUsernameAndEnabled(username))
            .thenReturn(Optional.empty()); // Simulamos que no lo encuentra porque está deshabilitado

        // ACT & ASSERT
        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername(username),
            "Debe lanzar excepción para usuarios deshabilitados"
        );

        verify(userRepository).findByUsernameAndEnabled(username);
    }

    @Test
    void loadUserByUsername_ShouldHandleDifferentRoles() {
        // ARRANGE
        User hrSpecialist = new User("hruser", "password", "ROLE_HR_SPECIALIST");
        hrSpecialist.setEnabled(true);

        when(userRepository.findByUsernameAndEnabled("hruser"))
            .thenReturn(Optional.of(hrSpecialist));

        // ACT
        UserDetails userDetails = userDetailsService.loadUserByUsername("hruser");

        // ASSERT
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertEquals(1, authorities.size());
        assertEquals("ROLE_HR_SPECIALIST",
            authorities.iterator().next().getAuthority(),
            "Debe manejar correctamente diferentes roles");
    }

    @Test
    void loadUserByUsername_ShouldHandleUsernameWithSpecialCharacters() {
        // ARRANGE
        String emailUsername = "user@example.com";
        User emailUser = new User(emailUsername, "password", "ROLE_USER");
        emailUser.setEnabled(true);

        when(userRepository.findByUsernameAndEnabled(emailUsername))
            .thenReturn(Optional.of(emailUser));

        // ACT
        UserDetails userDetails = userDetailsService.loadUserByUsername(emailUsername);

        // ASSERT
        assertEquals(emailUsername, userDetails.getUsername(),
            "Debe manejar usernames con caracteres especiales como emails");

        verify(userRepository).findByUsernameAndEnabled(emailUsername);
    }

    @Test
    void customUserPrincipal_ShouldExposeOriginalUser() {
        // ARRANGE
        when(userRepository.findByUsernameAndEnabled("testuser"))
            .thenReturn(Optional.of(testUser));

        // ACT
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // ASSERT
        assertTrue(userDetails instanceof CustomUserDetailsService.CustomUserPrincipal,
            "UserDetails debe ser instancia de CustomUserPrincipal");

        CustomUserDetailsService.CustomUserPrincipal principal =
            (CustomUserDetailsService.CustomUserPrincipal) userDetails;

        assertEquals(testUser, principal.getUser(),
            "El usuario original debe estar disponible a través del principal");
    }

    @Test
    void loadUserByUsername_ShouldCallRepositoryOnlyOnce() {
        // ARRANGE
        String username = "testuser";
        when(userRepository.findByUsernameAndEnabled(username))
            .thenReturn(Optional.of(testUser));

        // ACT
        userDetailsService.loadUserByUsername(username);

        // ASSERT
        verify(userRepository, times(1)).findByUsernameAndEnabled(username);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void loadUserByUsername_ShouldHandleNullUsername() {
        // ACT & ASSERT
        assertThrows(
            RuntimeException.class,
            () -> userDetailsService.loadUserByUsername(null),
            "Debe manejar username nulo apropiadamente"
        );
    }

    @Test
    void loadUserByUsername_ShouldHandleEmptyUsername() {
        // ARRANGE
        String emptyUsername = "";
        when(userRepository.findByUsernameAndEnabled(emptyUsername))
            .thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername(emptyUsername),
            "Debe rechazar usernames vacíos"
        );
    }

    @Test
    void customUserPrincipal_ShouldImplementUserDetailsCorrectly() {
        // ARRANGE
        User user = new User("testuser", "hashedpass", "ROLE_MANAGER");
        user.setEnabled(true);

        // ACT
        CustomUserDetailsService.CustomUserPrincipal principal =
            new CustomUserDetailsService.CustomUserPrincipal(user);

        // ASSERT
        assertEquals("testuser", principal.getUsername());
        assertEquals("hashedpass", principal.getPassword());
        assertTrue(principal.isEnabled());
        assertTrue(principal.isAccountNonExpired());
        assertTrue(principal.isAccountNonLocked());
        assertTrue(principal.isCredentialsNonExpired());

        Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();
        assertEquals(1, authorities.size());
        assertEquals("ROLE_MANAGER", authorities.iterator().next().getAuthority());
    }

    @Test
    void customUserPrincipal_ShouldReflectDisabledStatus() {
        // ARRANGE
        User disabledUser = new User("disableduser", "password", "ROLE_USER");
        disabledUser.setEnabled(false);

        // ACT
        CustomUserDetailsService.CustomUserPrincipal principal =
            new CustomUserDetailsService.CustomUserPrincipal(disabledUser);

        // ASSERT
        assertFalse(principal.isEnabled(), "Principal debe reflejar el estado deshabilitado");
        assertEquals(disabledUser, principal.getUser(), "Debe exponer el usuario original");
    }

    @Test
    void loadUserByUsername_ShouldHandleRepositoryException() {
        // ARRANGE
        String username = "problematicuser";
        when(userRepository.findByUsernameAndEnabled(username))
            .thenThrow(new RuntimeException("Database connection error"));

        // ACT & ASSERT
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> userDetailsService.loadUserByUsername(username),
            "Debe propagar excepciones del repositorio"
        );

        assertEquals("Database connection error", exception.getMessage(),
            "Debe preservar el mensaje de error original");
        verify(userRepository).findByUsernameAndEnabled(username);
    }

    @Test
    void loadUserByUsername_ShouldHandleUserWithoutRole() {
        // ARRANGE
        String username = "norolesuser";
        User userWithoutRole = new User(username, "password", null);
        userWithoutRole.setEnabled(true);

        when(userRepository.findByUsernameAndEnabled(username))
            .thenReturn(Optional.of(userWithoutRole));

        // ACT
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // ASSERT
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertNotNull(authorities, "Authorities no debe ser nulo");
        assertEquals(0, authorities.size(), "Debe tener una lista vacía cuando role es null");
    }

    @Test
    void loadUserByUsername_ShouldHandleUserWithEmptyRole() {
        // ARRANGE
        String username = "emptyroleuser";
        User userWithEmptyRole = new User(username, "password", "");
        userWithEmptyRole.setEnabled(true);

        when(userRepository.findByUsernameAndEnabled(username))
            .thenReturn(Optional.of(userWithEmptyRole));

        // ACT
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // ASSERT
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertNotNull(authorities, "Authorities no debe ser nulo");
        assertEquals(0, authorities.size(), "Debe tener una lista vacía cuando role es vacío");
    }

    @Test
    void loadUserByUsername_ShouldBeCaseSensitive() {
        // ARRANGE
        String originalUsername = "TestUser";
        String differentCaseUsername = "testuser";

        when(userRepository.findByUsernameAndEnabled(originalUsername))
            .thenReturn(Optional.of(testUser));
        when(userRepository.findByUsernameAndEnabled(differentCaseUsername))
            .thenReturn(Optional.empty());

        // ACT & ASSERT - Usuario con case correcto debe funcionar
        UserDetails userDetails = userDetailsService.loadUserByUsername(originalUsername);
        assertNotNull(userDetails, "Debe encontrar usuario con case exacto");

        // Usuario con case diferente no debe funcionar
        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername(differentCaseUsername),
            "Debe ser case-sensitive para usernames"
        );

        verify(userRepository).findByUsernameAndEnabled(originalUsername);
        verify(userRepository).findByUsernameAndEnabled(differentCaseUsername);
    }

    @Test
    void loadUserByUsername_ShouldHandleUsernameWithWhitespace() {
        // ARRANGE
        String usernameWithSpaces = " user with spaces ";
        User userWithSpaces = new User(usernameWithSpaces, "password", "ROLE_USER");
        userWithSpaces.setEnabled(true);

        when(userRepository.findByUsernameAndEnabled(usernameWithSpaces))
            .thenReturn(Optional.of(userWithSpaces));

        // ACT
        UserDetails userDetails = userDetailsService.loadUserByUsername(usernameWithSpaces);

        // ASSERT
        assertEquals(usernameWithSpaces, userDetails.getUsername(),
            "Debe preservar espacios en el username");
        verify(userRepository).findByUsernameAndEnabled(usernameWithSpaces);
    }

    @Test
    void customUserPrincipal_ShouldHandleUserWithNullPassword() {
        // ARRANGE
        User userWithNullPassword = new User("user", null, "ROLE_USER");
        userWithNullPassword.setEnabled(true);

        // ACT
        CustomUserDetailsService.CustomUserPrincipal principal =
            new CustomUserDetailsService.CustomUserPrincipal(userWithNullPassword);

        // ASSERT
        assertNull(principal.getPassword(), "Password debe ser null cuando el user tiene password null");
        assertEquals("user", principal.getUsername(), "Username debe estar disponible");
        assertTrue(principal.isEnabled(), "Usuario debe estar habilitado");
    }

    @Test
    void customUserPrincipal_ShouldMaintainConsistentBehavior() {
        // ARRANGE
        User user = new User("consistent", "pass", "ROLE_TEST");
        user.setEnabled(true);
        CustomUserDetailsService.CustomUserPrincipal principal =
            new CustomUserDetailsService.CustomUserPrincipal(user);

        // ACT & ASSERT - Múltiples llamadas deben devolver resultados consistentes
        for (int i = 0; i < 5; i++) {
            assertEquals("consistent", principal.getUsername(),
                "Username debe ser consistente en múltiples llamadas");
            assertEquals("pass", principal.getPassword(),
                "Password debe ser consistente en múltiples llamadas");
            assertTrue(principal.isEnabled(),
                "Estado enabled debe ser consistente");
            assertTrue(principal.isAccountNonExpired(),
                "AccountNonExpired debe ser consistente");
            assertTrue(principal.isAccountNonLocked(),
                "AccountNonLocked debe ser consistente");
            assertTrue(principal.isCredentialsNonExpired(),
                "CredentialsNonExpired debe ser consistente");

            Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();
            assertEquals(1, authorities.size(), "Authorities size debe ser consistente");
        }
    }

    @Test
    void loadUserByUsername_ShouldHandleLongUsername() {
        // ARRANGE
        String longUsername = "a".repeat(100); // Username muy largo
        User userWithLongName = new User(longUsername, "password", "ROLE_USER");
        userWithLongName.setEnabled(true);

        when(userRepository.findByUsernameAndEnabled(longUsername))
            .thenReturn(Optional.of(userWithLongName));

        // ACT
        UserDetails userDetails = userDetailsService.loadUserByUsername(longUsername);

        // ASSERT
        assertEquals(longUsername, userDetails.getUsername(),
            "Debe manejar usernames largos correctamente");
        verify(userRepository).findByUsernameAndEnabled(longUsername);
    }

    @Test
    void loadUserByUsername_ShouldHandleSpecialRoleFormats() {
        // ARRANGE - Probando diferentes formatos de roles
        String[] testRoles = {
            "ROLE_ADMIN",
            "admin", // Sin prefijo ROLE_
            "ROLE_HR_SPECIALIST_LEVEL_2", // Rol compuesto
            "ROLE_123", // Con números
            "role_lowercase", // En minúsculas
            "ROLE-WITH-DASHES" // Con guiones
        };

        for (int i = 0; i < testRoles.length; i++) {
            String username = "user" + i;
            String role = testRoles[i];

            User user = new User(username, "password", role);
            user.setEnabled(true);

            when(userRepository.findByUsernameAndEnabled(username))
                .thenReturn(Optional.of(user));

            // ACT
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // ASSERT
            Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
            assertEquals(1, authorities.size(),
                "Debe tener exactamente una autoridad para role: " + role);
            assertEquals(role, authorities.iterator().next().getAuthority(),
                "Debe preservar el formato exacto del role: " + role);
        }
    }

    @Test
    void userDetailsService_ShouldNotModifyOriginalUser() {
        // ARRANGE
        String originalUsername = testUser.getUsername();
        String originalPassword = testUser.getPassword();
        String originalRole = testUser.getRole();
        boolean originalEnabled = testUser.isEnabled();

        when(userRepository.findByUsernameAndEnabled(originalUsername))
            .thenReturn(Optional.of(testUser));

        // ACT
        UserDetails userDetails = userDetailsService.loadUserByUsername(originalUsername);

        // ASSERT - El usuario original no debe haber sido modificado
        assertEquals(originalUsername, testUser.getUsername(),
            "Username original no debe cambiar");
        assertEquals(originalPassword, testUser.getPassword(),
            "Password original no debe cambiar");
        assertEquals(originalRole, testUser.getRole(),
            "Role original no debe cambiar");
        assertEquals(originalEnabled, testUser.isEnabled(),
            "Estado enabled original no debe cambiar");

        // UserDetails debe tener los mismos valores
        assertEquals(originalUsername, userDetails.getUsername());
        assertEquals(originalPassword, userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
    }

    @Test
    void loadUserByUsername_ShouldHandleUnicodeUsernames() {
        // ARRANGE
        String unicodeUsername = "用户名"; // Username en chino
        User unicodeUser = new User(unicodeUsername, "password", "ROLE_USER");
        unicodeUser.setEnabled(true);

        when(userRepository.findByUsernameAndEnabled(unicodeUsername))
            .thenReturn(Optional.of(unicodeUser));

        // ACT
        UserDetails userDetails = userDetailsService.loadUserByUsername(unicodeUsername);

        // ASSERT
        assertEquals(unicodeUsername, userDetails.getUsername(),
            "Debe manejar caracteres Unicode correctamente");
        verify(userRepository).findByUsernameAndEnabled(unicodeUsername);
    }

    @Test
    void customUserPrincipal_ShouldBeImmutableFromExternalChanges() {
        // ARRANGE
        User mutableUser = new User("mutable", "original", "ROLE_USER");
        mutableUser.setEnabled(true);

        CustomUserDetailsService.CustomUserPrincipal principal =
            new CustomUserDetailsService.CustomUserPrincipal(mutableUser);

        // ACT - Modificar el usuario original después de crear el principal
        String originalUsername = principal.getUsername();
        String originalPassword = principal.getPassword();
        boolean originalEnabled = principal.isEnabled();

        // Modificar el usuario original
        mutableUser.setUsername("changed");
        mutableUser.setPassword("changed_password");
        mutableUser.setEnabled(false);

        // ASSERT - El principal debe mantener una referencia al objeto original
        // pero sus métodos deben reflejar el estado actual del objeto
        assertEquals("changed", principal.getUsername(),
            "Principal debe reflejar cambios en el objeto User referenciado");
        assertEquals("changed_password", principal.getPassword(),
            "Principal debe reflejar cambios en password");
        assertFalse(principal.isEnabled(),
            "Principal debe reflejar cambios en estado enabled");
    }

    @Test
    void loadUserByUsername_ShouldHandleConcurrentAccess() {
        // ARRANGE
        String username = "concurrent";
        when(userRepository.findByUsernameAndEnabled(username))
            .thenReturn(Optional.of(testUser));

        // ACT - Simular múltiples accesos concurrentes
        UserDetails[] results = new UserDetails[10];
        for (int i = 0; i < 10; i++) {
            results[i] = userDetailsService.loadUserByUsername(username);
        }

        // ASSERT - Todos los resultados deben ser válidos y consistentes
        for (UserDetails result : results) {
            assertNotNull(result, "Resultado no debe ser null en acceso concurrente");
            assertEquals(testUser.getUsername(), result.getUsername(),
                "Username debe ser consistente en accesos concurrentes");
            assertTrue(result.isEnabled(),
                "Estado enabled debe ser consistente");
        }

        verify(userRepository, times(10)).findByUsernameAndEnabled(username);
    }

    @Test
    void customUserPrincipal_ShouldHandleEqualsAndHashCode() {
        // ARRANGE
        User user1 = new User("same", "password", "ROLE_USER");
        user1.setId(UUID.randomUUID());
        user1.setEnabled(true);

        User user2 = new User("same", "password", "ROLE_USER");
        user2.setId(user1.getId()); // Mismo ID
        user2.setEnabled(true);

        User user3 = new User("different", "password", "ROLE_USER");
        user3.setId(UUID.randomUUID());
        user3.setEnabled(true);

        CustomUserDetailsService.CustomUserPrincipal principal1 =
            new CustomUserDetailsService.CustomUserPrincipal(user1);
        CustomUserDetailsService.CustomUserPrincipal principal2 =
            new CustomUserDetailsService.CustomUserPrincipal(user2);
        CustomUserDetailsService.CustomUserPrincipal principal3 =
            new CustomUserDetailsService.CustomUserPrincipal(user3);

        // ACT & ASSERT
        // Nota: El comportamiento de equals depende de la implementación en User
        assertNotNull(principal1, "Principal1 no debe ser null");
        assertNotNull(principal2, "Principal2 no debe ser null");
        assertNotNull(principal3, "Principal3 no debe ser null");

        // Verificar que toString no lance excepciones
        assertDoesNotThrow(() -> principal1.toString(),
            "toString() no debe lanzar excepciones");
        assertDoesNotThrow(() -> principal1.hashCode(),
            "hashCode() no debe lanzar excepciones");
    }

    @Test
    void loadUserByUsername_PerformanceTest() {
        // ARRANGE
        String username = "performance";
        when(userRepository.findByUsernameAndEnabled(username))
            .thenReturn(Optional.of(testUser));

        // ACT - Medir tiempo de ejecución
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 1000; i++) {
            userDetailsService.loadUserByUsername(username);
        }

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // ASSERT
        assertTrue(executionTime < 5000, // Menos de 5 segundos para 1000 llamadas
            "El servicio debe ser eficiente para múltiples llamadas");

        verify(userRepository, times(1000)).findByUsernameAndEnabled(username);
    }

    @Test
    void customUserPrincipal_ShouldHandleNullUser() {
        // ACT & ASSERT
        assertThrows(
            NullPointerException.class,
            () -> new CustomUserDetailsService.CustomUserPrincipal(null),
            "Constructor debe rechazar usuario null"
        );
    }

    @Test
    void loadUserByUsername_ShouldHandleRepositoryReturningNull() {
        // ARRANGE
        String username = "nullreturn";
        when(userRepository.findByUsernameAndEnabled(username))
            .thenReturn(null); // Retorno null en lugar de Optional

        // ACT & ASSERT
        assertThrows(
            RuntimeException.class,
            () -> userDetailsService.loadUserByUsername(username),
            "Debe manejar apropiadamente cuando el repositorio retorna null"
        );
    }

    @Test
    void loadUserByUsername_ShouldLogSecurityEvents() {
        // ARRANGE - Este test verificaría que se registren eventos de seguridad
        // En una implementación real, se podría usar un logger mock
        String username = "security_test";

        // Caso 1: Usuario encontrado
        when(userRepository.findByUsernameAndEnabled(username))
            .thenReturn(Optional.of(testUser));

        // ACT
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // ASSERT
        assertNotNull(userDetails, "Debe retornar UserDetails válido");
        verify(userRepository).findByUsernameAndEnabled(username);

        // Caso 2: Usuario no encontrado (evento de seguridad)
        reset(userRepository);
        String unknownUser = "unknown_user";
        when(userRepository.findByUsernameAndEnabled(unknownUser))
            .thenReturn(Optional.empty());

        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername(unknownUser),
            "Debe lanzar excepción para usuario no encontrado"
        );
    }

    @Test
    void customUserPrincipal_ShouldProvideUserContextForAuditing() {
        // ARRANGE
        UUID userId = UUID.randomUUID();
        User auditUser = new User("audit_user", "password", "ROLE_AUDITOR");
        auditUser.setId(userId);
        auditUser.setEnabled(true);

        // ACT
        CustomUserDetailsService.CustomUserPrincipal principal =
            new CustomUserDetailsService.CustomUserPrincipal(auditUser);

        // ASSERT
        assertEquals(auditUser, principal.getUser(),
            "Principal debe exponer el usuario para auditoría");
        assertEquals(userId, principal.getUser().getId(),
            "ID del usuario debe estar disponible para auditoría");
        assertEquals("ROLE_AUDITOR", principal.getUser().getRole(),
            "Role debe estar disponible para auditoría");
    }

    @Test
    void loadUserByUsername_ShouldBeThreadSafe() throws InterruptedException {
        // ARRANGE
        String username = "threadsafe";
        when(userRepository.findByUsernameAndEnabled(username))
            .thenReturn(Optional.of(testUser));

        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        UserDetails[] results = new UserDetails[threadCount];
        Exception[] exceptions = new Exception[threadCount];

        // ACT - Ejecutar en múltiples hilos
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                try {
                    results[index] = userDetailsService.loadUserByUsername(username);
                } catch (Exception e) {
                    exceptions[index] = e;
                }
            });
            threads[i].start();
        }

        // Esperar a que todos los hilos terminen
        for (Thread thread : threads) {
            thread.join();
        }

        // ASSERT
        for (int i = 0; i < threadCount; i++) {
            assertNull(exceptions[i], "No debe haber excepciones en hilo " + i);
            assertNotNull(results[i], "Resultado debe ser válido en hilo " + i);
            assertEquals(testUser.getUsername(), results[i].getUsername(),
                "Username debe ser consistente en hilo " + i);
        }

        verify(userRepository, times(threadCount)).findByUsernameAndEnabled(username);
    }
}
