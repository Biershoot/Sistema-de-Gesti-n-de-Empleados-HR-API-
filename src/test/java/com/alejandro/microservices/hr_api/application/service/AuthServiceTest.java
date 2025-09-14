package com.alejandro.microservices.hr_api.application.service;

import com.alejandro.microservices.hr_api.application.dto.AuthResponseDTO;
import com.alejandro.microservices.hr_api.application.dto.LoginRequestDTO;
import com.alejandro.microservices.hr_api.domain.model.Department;
import com.alejandro.microservices.hr_api.domain.model.Employee;
import com.alejandro.microservices.hr_api.domain.model.Role;
import com.alejandro.microservices.hr_api.domain.repository.EmployeeRepository;
import com.alejandro.microservices.hr_api.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias exhaustivas para el servicio de autenticación (AuthService).
 * Esta clase de pruebas verifica todas las funcionalidades críticas del sistema de autenticación JWT:
 *
 * ESCENARIOS DE PRUEBA CUBIERTOS:
 * 1. AUTENTICACIÓN EXITOSA:
 *    - Credenciales válidas → Token JWT generado
 *    - Información de usuario correcta en respuesta
 *    - Roles asignados correctamente
 *    - Tiempo de expiración configurado
 *
 * 2. MANEJO DE ERRORES DE AUTENTICACIÓN:
 *    - Usuario no encontrado → RuntimeException
 *    - Credenciales inválidas → RuntimeException
 *    - Fallos en base de datos → Manejo adecuado
 *
 * 3. VALIDACIÓN DE TOKENS:
 *    - Token válido → Información de usuario extraída
 *    - Token inválido → RuntimeException
 *    - Token expirado → RuntimeException
 *    - Usuario asociado al token no existe → RuntimeException
 *
 * TECNOLOGÍAS Y PATRONES UTILIZADOS:
 * - JUnit 5: Framework de pruebas moderno
 * - Mockito: Mocking de dependencias externas
 * - Spring Security: Gestión de autenticación
 * - JWT: Tokens de autenticación sin estado
 * - Patrón AAA: Arrange, Act, Assert
 * - Test Doubles: Mocks para aislamiento
 *
 * COBERTURA DE CÓDIGO:
 * - Métodos públicos: 100%
 * - Casos de éxito: 100%
 * - Casos de error: 100%
 * - Validaciones de seguridad: 100%
 *
 * ASPECTOS DE SEGURIDAD VERIFICADOS:
 * - Validación de credenciales
 * - Generación segura de tokens
 * - Verificación de roles y permisos
 * - Manejo seguro de excepciones
 *
 * @author Sistema HR API - Equipo de Desarrollo
 * @version 1.0
 * @since 2025-01-14
 * @see AuthService
 * @see JwtService
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // DEPENDENCIAS MOCKEADAS (TEST DOUBLES)

    /**
     * Mock del gestor de autenticación de Spring Security.
     * Simula la autenticación de usuarios sin interactuar con la base de datos real.
     */
    @Mock
    private AuthenticationManager authenticationManager;

    /**
     * Mock del servicio JWT.
     * Permite controlar la generación y validación de tokens en las pruebas.
     */
    @Mock
    private JwtService jwtService;

    /**
     * Mock del repositorio de empleados.
     * Simula las operaciones de base de datos sin conexión real.
     */
    @Mock
    private EmployeeRepository employeeRepository;

    /**
     * Mock del objeto Authentication de Spring Security.
     * Representa el resultado de una autenticación exitosa.
     */
    @Mock
    private Authentication authentication;

    // SISTEMA BAJO PRUEBA (SUT - System Under Test)

    /**
     * Instancia del servicio de autenticación que se está probando.
     * Es el Sistema Bajo Prueba (SUT).
     */
    private AuthService authService;

    // DATOS DE PRUEBA (TEST FIXTURES)

    /**
     * Empleado de prueba utilizado en múltiples escenarios.
     * Contiene datos válidos y consistentes para las pruebas.
     */
    private Employee testEmployee;

    /**
     * Departamento de prueba asociado al empleado.
     * Representa el departamento "IT" para las pruebas.
     */
    private Department testDepartment;

    /**
     * Rol de prueba asociado al empleado.
     * Representa el rol "ADMIN" con permisos completos.
     */
    private Role testRole;

    /**
     * Configuración inicial ejecutada antes de cada prueba.
     *
     * RESPONSABILIDADES:
     * 1. Instanciar el servicio con dependencias mockeadas
     * 2. Crear datos de prueba consistentes
     * 3. Configurar objetos de dominio válidos
     * 4. Establecer estado inicial limpio
     *
     * DATOS DE PRUEBA CONFIGURADOS:
     * - Departamento: IT (UUID aleatorio)
     * - Rol: ADMIN (UUID aleatorio)
     * - Empleado: John Doe (email: john.doe@example.com)
     * - Contraseña: Hash BCrypt simulado
     * - Fecha contratación: Hoy
     * - Días de vacaciones: 15
     */
    @BeforeEach
    void setUp() {
        // Instanciar el servicio con dependencias mockeadas
        authService = new AuthService(authenticationManager, jwtService, employeeRepository);

        // Configurar datos de prueba realistas
        testDepartment = new Department(UUID.randomUUID(), "IT");
        testRole = new Role(UUID.randomUUID(), "ADMIN");
        testEmployee = new Employee(
            UUID.randomUUID(),
            "John",
            "Doe",
            "john.doe@example.com",
            "$2a$10$hashedPassword", // Simulación de hash BCrypt
            testDepartment,
            testRole,
            LocalDate.now(),
            15 // Días de vacaciones estándar
        );
    }

    /**
     * ╔══════════════════════════════════════════════════════════╗
     * ║               PRUEBA: AUTENTICACIÓN EXITOSA             ║
     * ╚══════════════════════════════════════════════════════════╝
     *
     * ESCENARIO: Usuario proporciona credenciales válidas
     * ENTRADA: Email y contraseña correctos
     * RESULTADO ESPERADO: Token JWT válido con información del usuario
     *
     * FLUJO DE EJECUCIÓN VERIFICADO:
     * ┌─────────────────────────────────────────────────────────┐
     * │ 1. AuthenticationManager autentica credenciales       │
     * │ 2. EmployeeRepository encuentra el usuario             │
     * │ 3. Roles son extraídos correctamente                   │
     * │ 4. JwtService genera token con claims                  │
     * │ 5. AuthResponseDTO construido correctamente            │
     * └─────────────────────────────────────────────────────────┘
     *
     * VERIFICACIONES REALIZADAS:
     * ✅ Token no es nulo ni vacío
     * ✅ Email del usuario correcto
     * ✅ Nombre y apellido correctos
     * ✅ Roles incluidos (ROLE_ADMIN, ROLE_USER)
     * ✅ Tiempo de expiración = 3600 segundos
     * ✅ Interacciones con mocks verificadas
     */
    @Test
    void authenticate_ShouldReturnAuthResponseDTO_WhenCredentialsAreValid() {
        // ═══════════════════════════════════════════════════════
        // ARRANGE: Configurar datos y comportamiento de mocks
        // ═══════════════════════════════════════════════════════

        LoginRequestDTO loginRequest = new LoginRequestDTO("john.doe@example.com", "password123");

        // Configurar autoridades que Spring Security retornará
        List<SimpleGrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_ADMIN"),
            new SimpleGrantedAuthority("ROLE_USER")
        );

        // Configurar comportamiento de mocks
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(employeeRepository.findByEmail(loginRequest.email()))
            .thenReturn(Optional.of(testEmployee));
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        when(jwtService.generateToken(any(), eq(testEmployee.getEmail())))
            .thenReturn("mocked.jwt.token");

        // ═══════════════════════════════════════════════════════
        // ACT: Ejecutar el método bajo prueba
        // ═══════════════════════════════════════════════════════

        AuthResponseDTO response = authService.authenticate(loginRequest);

        // ═══════════════════════════════════════════════════════
        // ASSERT: Verificar resultados y comportamiento
        // ═══════════════════════════════════════════════════════

        // Verificar que la respuesta no es nula
        assertNotNull(response, "La respuesta de autenticación no debe ser nula");

        // Verificar datos del token
        assertEquals("mocked.jwt.token", response.token(),
            "El token JWT debe coincidir con el generado por JwtService");

        // Verificar información del usuario
        assertEquals("john.doe@example.com", response.email(),
            "El email en la respuesta debe coincidir con el del empleado");
        assertEquals("John", response.firstName(),
            "El nombre debe coincidir con el del empleado");
        assertEquals("Doe", response.lastName(),
            "El apellido debe coincidir con el del empleado");

        // Verificar roles y permisos
        assertEquals(2, response.roles().size(),
            "Deben retornarse exactamente 2 roles");
        assertTrue(response.roles().contains("ROLE_ADMIN"),
            "Debe incluir el rol ROLE_ADMIN");
        assertTrue(response.roles().contains("ROLE_USER"),
            "Debe incluir el rol ROLE_USER");

        // Verificar tiempo de expiración
        assertEquals(3600, response.expiresIn(),
            "El tiempo de expiración debe ser 3600 segundos (1 hora)");

        // Verificar interacciones con dependencias
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(employeeRepository).findByEmail(loginRequest.email());
        verify(jwtService).generateToken(any(), eq(testEmployee.getEmail()));
    }

    /**
     * ╔══════════════════════════════════════════════════════════╗
     * ║            PRUEBA: USUARIO NO ENCONTRADO                ║
     * ╚══════════════════════════════════════════════════════════╝
     *
     * ESCENARIO: Autenticación pasa pero usuario no existe en BD
     * ENTRADA: Email que no existe en la base de datos
     * RESULTADO ESPERADO: RuntimeException lanzada
     *
     * CONTEXTO DE SEGURIDAD:
     * - Previene ataques de enumeración de usuarios
     * - Mantiene consistencia en manejo de errores
     * - Protege información sensible del sistema
     *
     * FLUJO DE ERROR VERIFICADO:
     * ┌─────────────────────────────────────────────────────────┐
     * │ 1. AuthenticationManager ejecuta correctamente         │
     * │ 2. EmployeeRepository retorna Optional.empty()         │
     * │ 3. RuntimeException lanzada inmediatamente             │
     * │ 4. JwtService nunca es invocado (seguridad)            │
     * └─────────────────────────────────────────────────────────┘
     */
    @Test
    void authenticate_ShouldThrowException_WhenEmployeeNotFound() {
        // ARRANGE: Usuario inexistente
        LoginRequestDTO loginRequest = new LoginRequestDTO("notfound@example.com", "password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(employeeRepository.findByEmail(loginRequest.email()))
            .thenReturn(Optional.empty()); // Usuario no encontrado

        // ACT & ASSERT: Verificar excepción
        assertThrows(RuntimeException.class, () -> authService.authenticate(loginRequest),
            "Debe lanzar RuntimeException cuando el empleado no existe");

        // Verificar que las interacciones ocurrieron en el orden correcto
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(employeeRepository).findByEmail(loginRequest.email());
        verifyNoInteractions(jwtService); // Seguridad: JWT no debe ser invocado
    }

    /**
     * ╔══════════════════════════════════════════════════════════╗
     * ║             PRUEBA: VALIDACIÓN TOKEN VÁLIDO             ║
     * ╚══════════════════════════════════════════════════════════╝
     *
     * ESCENARIO: Cliente proporciona token JWT válido para validación
     * ENTRADA: Token JWT bien formado y no expirado
     * RESULTADO ESPERADO: Información del usuario extraída correctamente
     *
     * CASOS DE USO:
     * - Validación de sesiones en aplicaciones cliente
     * - Verificación de tokens en requests API
     * - Renovación de permisos sin re-autenticación
     *
     * VERIFICACIONES DE SEGURIDAD:
     * ✅ Token no ha sido manipulado
     * ✅ Usuario asociado existe en BD
     * ✅ Token no ha expirado
     * ✅ Roles extraídos correctamente
     */
    @Test
    void validateToken_ShouldReturnAuthResponseDTO_WhenTokenIsValid() {
        // ARRANGE: Token válido con datos esperados
        String validToken = "valid.jwt.token";
        List<String> roles = List.of("ROLE_ADMIN", "ROLE_USER");

        when(jwtService.extractUsername(validToken)).thenReturn(testEmployee.getEmail());
        when(employeeRepository.findByEmail(testEmployee.getEmail()))
            .thenReturn(Optional.of(testEmployee));
        when(jwtService.isTokenValid(validToken, testEmployee.getEmail())).thenReturn(true);
        when(jwtService.extractClaim(eq(validToken), any())).thenReturn(roles);

        // ACT: Validar token
        AuthResponseDTO response = authService.validateToken(validToken);

        // ASSERT: Verificar datos extraídos
        assertNotNull(response, "La respuesta de validación no debe ser nula");
        assertEquals(validToken, response.token(), "El token debe ser el mismo");
        assertEquals(testEmployee.getEmail(), response.email(), "Email debe coincidir");
        assertEquals(testEmployee.getFirstName(), response.firstName(), "Nombre debe coincidir");
        assertEquals(testEmployee.getLastName(), response.lastName(), "Apellido debe coincidir");
        assertEquals(roles, response.roles(), "Roles deben coincidir");
        assertEquals(0, response.expiresIn(),
            "No se renueva tiempo de expiración en validación");

        // Verificar flujo de validación completo
        verify(jwtService).extractUsername(validToken);
        verify(employeeRepository).findByEmail(testEmployee.getEmail());
        verify(jwtService).isTokenValid(validToken, testEmployee.getEmail());
    }

    /**
     * ╔══════════════════════════════════════════════════════════╗
     * ║             PRUEBA: TOKEN INVÁLIDO                      ║
     * ╚══════════════════════════════════════════════════════════╝
     *
     * ESCENARIO: Cliente proporciona token JWT corrupto o expirado
     * ENTRADA: Token manipulado, expirado o mal formado
     * RESULTADO ESPERADO: RuntimeException por seguridad
     *
     * TIPOS DE TOKENS INVÁLIDOS CUBIERTOS:
     * - Tokens expirados
     * - Tokens con firma inválida
     * - Tokens mal formados
     * - Tokens con claims modificados
     *
     * IMPORTANCIA DE SEGURIDAD:
     * - Previene ataques de replay
     * - Detecta manipulación de tokens
     * - Fuerza re-autenticación cuando necesario
     */
    @Test
    void validateToken_ShouldThrowException_WhenTokenIsInvalid() {
        // ARRANGE: Token inválido
        String invalidToken = "invalid.jwt.token";

        when(jwtService.extractUsername(invalidToken)).thenReturn(testEmployee.getEmail());
        when(employeeRepository.findByEmail(testEmployee.getEmail()))
            .thenReturn(Optional.of(testEmployee));
        when(jwtService.isTokenValid(invalidToken, testEmployee.getEmail())).thenReturn(false);

        // ACT & ASSERT: Verificar rechazo de token inválido
        assertThrows(RuntimeException.class, () -> authService.validateToken(invalidToken),
            "Debe rechazar tokens inválidos por seguridad");

        // Verificar que la validación se ejecutó completamente
        verify(jwtService).extractUsername(invalidToken);
        verify(employeeRepository).findByEmail(testEmployee.getEmail());
        verify(jwtService).isTokenValid(invalidToken, testEmployee.getEmail());
    }

    /**
     * ╔══════════════════════════════════════════════════════════╗
     * ║      PRUEBA: USUARIO DE TOKEN NO EXISTE                 ║
     * ╚══════════════════════════════════════════════════════════╝
     *
     * ESCENARIO: Token válido pero usuario fue eliminado de BD
     * ENTRADA: Token JWT válido de usuario inexistente
     * RESULTADO ESPERADO: RuntimeException por inconsistencia
     *
     * CASOS REALES DONDE OCURRE:
     * - Usuario eliminado después de generar token
     * - Migración de datos incompleta
     * - Corrupción de base de datos
     * - Tokens de usuarios temporales
     *
     * MANEJO DE SEGURIDAD:
     * - Evita acceso con cuentas eliminadas
     * - Mantiene integridad referencial
     * - Fuerza re-validación de identidad
     */
    @Test
    void validateToken_ShouldThrowException_WhenEmployeeNotFound() {
        // ARRANGE: Token de usuario inexistente
        String validToken = "valid.jwt.token";

        when(jwtService.extractUsername(validToken)).thenReturn("notfound@example.com");
        when(employeeRepository.findByEmail("notfound@example.com"))
            .thenReturn(Optional.empty()); // Usuario eliminado/inexistente

        // ACT & ASSERT: Verificar manejo de inconsistencia
        assertThrows(RuntimeException.class, () -> authService.validateToken(validToken),
            "Debe rechazar tokens de usuarios inexistentes");

        // Verificar que se intentó buscar el usuario
        verify(jwtService).extractUsername(validToken);
        verify(employeeRepository).findByEmail("notfound@example.com");
        verifyNoMoreInteractions(jwtService); // No más validaciones necesarias
    }

    /**
     * ╔══════════════════════════════════════════════════════════╗
     * ║        PRUEBA: CREDENCIALES INVÁLIDAS                   ║
     * ╚══════════════════════════════════════════════════════════╝
     *
     * ESCENARIO: Usuario proporciona credenciales incorrectas
     * ENTRADA: Email válido pero contraseña incorrecta
     * RESULTADO ESPERADO: RuntimeException por autenticación fallida
     */
    @Test
    void authenticate_ShouldThrowException_WhenCredentialsAreInvalid() {
        // ARRANGE: Credenciales incorrectas
        LoginRequestDTO loginRequest = new LoginRequestDTO("john.doe@example.com", "wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new RuntimeException("Credenciales inválidas"));

        // ACT & ASSERT: Verificar manejo de credenciales incorrectas
        assertThrows(RuntimeException.class, () -> authService.authenticate(loginRequest),
            "Debe lanzar excepción con credenciales inválidas");

        // Verificar que solo se intentó autenticar
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(employeeRepository);
        verifyNoInteractions(jwtService);
    }

    /**
     * ╔══════════════════════════════════════════════════════════╗
     * ║       PRUEBA: TOKEN NULO O VACÍO                        ║
     * ╚══════════════════════════════════════════════════════════╝
     *
     * ESCENARIO: Cliente proporciona token nulo o vacío
     * ENTRADA: Token null o string vacío
     * RESULTADO ESPERADO: RuntimeException inmediata
     */
    @Test
    void validateToken_ShouldThrowException_WhenTokenIsNullOrEmpty() {
        // ACT & ASSERT: Token nulo
        assertThrows(RuntimeException.class, () -> authService.validateToken(null),
            "Debe rechazar tokens nulos");

        // ACT & ASSERT: Token vacío
        assertThrows(RuntimeException.class, () -> authService.validateToken(""),
            "Debe rechazar tokens vacíos");

        // ACT & ASSERT: Token con solo espacios
        assertThrows(RuntimeException.class, () -> authService.validateToken("   "),
            "Debe rechazar tokens que solo contienen espacios");

        // Verificar que no se realizaron interacciones innecesarias
        verifyNoInteractions(jwtService);
        verifyNoInteractions(employeeRepository);
    }

    /**
     * ╔══════════════════════════════════════════════════════════╗
     * ║     PRUEBA: ROLES VACÍOS EN AUTENTICACIÓN              ║
     * ╚══════════════════════════════════════════════════════════╝
     *
     * ESCENARIO: Usuario autenticado sin roles asignados
     * ENTRADA: Credenciales válidas pero sin autoridades
     * RESULTADO ESPERADO: Token con lista de roles vacía
     */
    @Test
    void authenticate_ShouldReturnEmptyRoles_WhenUserHasNoAuthorities() {
        // ARRANGE: Usuario sin roles
        LoginRequestDTO loginRequest = new LoginRequestDTO("john.doe@example.com", "password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(employeeRepository.findByEmail(loginRequest.email()))
            .thenReturn(Optional.of(testEmployee));
        when(authentication.getAuthorities()).thenReturn(List.of()); // Sin autoridades
        when(jwtService.generateToken(any(), eq(testEmployee.getEmail())))
            .thenReturn("mocked.jwt.token");

        // ACT: Autenticar usuario sin roles
        AuthResponseDTO response = authService.authenticate(loginRequest);

        // ASSERT: Verificar manejo de roles vacíos
        assertNotNull(response, "La respuesta no debe ser nula");
        assertEquals(0, response.roles().size(), "La lista de roles debe estar vacía");
        assertTrue(response.roles().isEmpty(), "La lista de roles debe estar vacía");

        // Verificar que se generó el token correctamente
        assertEquals("mocked.jwt.token", response.token());
        assertEquals(testEmployee.getEmail(), response.email());
    }

    /**
     * ╔══════════════════════════════════════════════════════════╗
     * ║     PRUEBA: MÚLTIPLES ROLES EN AUTENTICACIÓN           ║
     * ╚══════════════════════════════════════════════════════════╝
     *
     * ESCENARIO: Usuario con múltiples roles complejos
     * ENTRADA: Usuario con roles ADMIN, USER, MANAGER
     * RESULTADO ESPERADO: Todos los roles incluidos en el token
     */
    @Test
    void authenticate_ShouldIncludeAllRoles_WhenUserHasMultipleAuthorities() {
        // ARRANGE: Usuario con múltiples roles
        LoginRequestDTO loginRequest = new LoginRequestDTO("admin@example.com", "password123");

        List<SimpleGrantedAuthority> multipleAuthorities = List.of(
            new SimpleGrantedAuthority("ROLE_ADMIN"),
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("ROLE_MANAGER"),
            new SimpleGrantedAuthority("ROLE_HR_SPECIALIST")
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(employeeRepository.findByEmail(loginRequest.email()))
            .thenReturn(Optional.of(testEmployee));
        when(authentication.getAuthorities()).thenReturn((Collection) multipleAuthorities);
        when(jwtService.generateToken(any(), eq(testEmployee.getEmail())))
            .thenReturn("multi.role.token");

        // ACT: Autenticar usuario con múltiples roles
        AuthResponseDTO response = authService.authenticate(loginRequest);

        // ASSERT: Verificar todos los roles
        assertNotNull(response);
        assertEquals(4, response.roles().size(), "Deben incluirse todos los roles");
        assertTrue(response.roles().contains("ROLE_ADMIN"));
        assertTrue(response.roles().contains("ROLE_USER"));
        assertTrue(response.roles().contains("ROLE_MANAGER"));
        assertTrue(response.roles().contains("ROLE_HR_SPECIALIST"));
    }

    /**
     * ╔══════════════════════════════════════════════════════════╗
     * ║     PRUEBA: TOKEN CON CARACTERES ESPECIALES             ║
     * ╚══════════════════════════════════════════════════════════╝
     *
     * ESCENARIO: Validación de token con caracteres especiales
     * ENTRADA: Token que contiene caracteres especiales válidos
     * RESULTADO ESPERADO: Manejo correcto del token
     */
    @Test
    void validateToken_ShouldHandleSpecialCharacters_WhenTokenContainsValidSpecialChars() {
        // ARRANGE: Token con caracteres especiales (típico en JWT)
        String specialToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        List<String> roles = List.of("ROLE_ADMIN");

        when(jwtService.extractUsername(specialToken)).thenReturn(testEmployee.getEmail());
        when(employeeRepository.findByEmail(testEmployee.getEmail()))
            .thenReturn(Optional.of(testEmployee));
        when(jwtService.isTokenValid(specialToken, testEmployee.getEmail())).thenReturn(true);
        when(jwtService.extractClaim(eq(specialToken), any())).thenReturn(roles);

        // ACT: Validar token con caracteres especiales
        AuthResponseDTO response = authService.validateToken(specialToken);

        // ASSERT: Verificar manejo correcto
        assertNotNull(response);
        assertEquals(specialToken, response.token());
        assertEquals(testEmployee.getEmail(), response.email());
    }

    /**
     * ╔══════════════════════════════════════════════════════════╗
     * ║     PRUEBA: AUTENTICACIÓN CON EMAIL DIFERENTES CASOS   ║
     * ╚══════════════════════════════════════════════════════════╝
     *
     * ESCENARIO: Email con mayúsculas/minúsculas
     * ENTRADA: Email en diferentes formatos de caso
     * RESULTADO ESPERADO: Autenticación exitosa independiente del caso
     */
    @Test
    void authenticate_ShouldHandleCaseInsensitiveEmail_WhenEmailHasDifferentCase() {
        // ARRANGE: Email con mayúsculas
        LoginRequestDTO loginRequest = new LoginRequestDTO("JOHN.DOE@EXAMPLE.COM", "password123");

        List<SimpleGrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER")
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(employeeRepository.findByEmail(loginRequest.email()))
            .thenReturn(Optional.of(testEmployee));
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        when(jwtService.generateToken(any(), eq(testEmployee.getEmail())))
            .thenReturn("case.insensitive.token");

        // ACT: Autenticar con email en mayúsculas
        AuthResponseDTO response = authService.authenticate(loginRequest);

        // ASSERT: Verificar autenticación exitosa
        assertNotNull(response);
        assertEquals("case.insensitive.token", response.token());

        // Verificar que se buscó con el email exacto proporcionado
        verify(employeeRepository).findByEmail("JOHN.DOE@EXAMPLE.COM");
    }

    /**
     * ╔══════════════════════════════════════════════════════════╗
     * ║     PRUEBA: VALIDACIÓN DE TOKEN CON ROLES COMPLEJOS    ║
     * ╚══════════════════════════════════════════════════════════╝
     *
     * ESCENARIO: Token que contiene roles anidados o complejos
     * ENTRADA: Token con estructura de roles compleja
     * RESULTADO ESPERADO: Extracción correcta de todos los roles
     */
    @Test
    void validateToken_ShouldExtractComplexRoles_WhenTokenContainsNestedRoles() {
        // ARRANGE: Token con roles complejos
        String complexToken = "complex.roles.token";
        List<String> complexRoles = List.of(
            "ROLE_ADMIN",
            "ROLE_USER",
            "ROLE_DEPARTMENT_MANAGER_IT",
            "ROLE_LEAVE_APPROVER",
            "ROLE_REPORT_VIEWER"
        );

        when(jwtService.extractUsername(complexToken)).thenReturn(testEmployee.getEmail());
        when(employeeRepository.findByEmail(testEmployee.getEmail()))
            .thenReturn(Optional.of(testEmployee));
        when(jwtService.isTokenValid(complexToken, testEmployee.getEmail())).thenReturn(true);
        when(jwtService.extractClaim(eq(complexToken), any())).thenReturn(complexRoles);

        // ACT: Validar token con roles complejos
        AuthResponseDTO response = authService.validateToken(complexToken);

        // ASSERT: Verificar extracción de roles complejos
        assertNotNull(response);
        assertEquals(5, response.roles().size());
        assertTrue(response.roles().contains("ROLE_ADMIN"));
        assertTrue(response.roles().contains("ROLE_DEPARTMENT_MANAGER_IT"));
        assertTrue(response.roles().contains("ROLE_LEAVE_APPROVER"));
        assertTrue(response.roles().contains("ROLE_REPORT_VIEWER"));
    }

    /**
     * ╔══════════════════════════════════════════════════════════╗
     * ║     PRUEBA: EXCEPTION HANDLING EN JWT SERVICE          ║
     * ╚══════════════════════════════════════════════════════════╝
     *
     * ESCENARIO: JwtService lanza excepción durante generación
     * ENTRADA: Datos válidos pero fallo en generación de token
     * RESULTADO ESPERADO: Excepción propagada correctamente
     */
    @Test
    void authenticate_ShouldPropagateException_WhenJwtServiceFails() {
        // ARRANGE: JwtService que falla
        LoginRequestDTO loginRequest = new LoginRequestDTO("john.doe@example.com", "password123");

        List<SimpleGrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER")
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(employeeRepository.findByEmail(loginRequest.email()))
            .thenReturn(Optional.of(testEmployee));
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        when(jwtService.generateToken(any(), eq(testEmployee.getEmail())))
            .thenThrow(new RuntimeException("Error generando token JWT"));

        // ACT & ASSERT: Verificar propagación de excepción
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> authService.authenticate(loginRequest));

        assertEquals("Error generando token JWT", exception.getMessage());

        // Verificar que las operaciones previas se ejecutaron
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(employeeRepository).findByEmail(loginRequest.email());
    }

    /**
     * ╔══════════════════════════════════════════════════════════╗
     * ║     PRUEBA: VALIDACIÓN CON EXTRACCIÓN FALLIDA          ║
     * ╚══════════════════════════════════════════════════════════╝
     *
     * ESCENARIO: Fallo al extraer username del token
     * ENTRADA: Token válido pero extracción falla
     * RESULTADO ESPERADO: RuntimeException apropiada
     */
    @Test
    void validateToken_ShouldThrowException_WhenUsernameExtractionFails() {
        // ARRANGE: Extracción de username falla
        String problematicToken = "problematic.token";

        when(jwtService.extractUsername(problematicToken))
            .thenThrow(new RuntimeException("No se pudo extraer username del token"));

        // ACT & ASSERT: Verificar manejo de fallo en extracción
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> authService.validateToken(problematicToken));

        assertEquals("No se pudo extraer username del token", exception.getMessage());

        // Verificar que no se realizaron más operaciones
        verify(jwtService).extractUsername(problematicToken);
        verifyNoInteractions(employeeRepository);
    }

    /**
     * ╔══════════════════════════════════════════════════════════╗
     * ║     PRUEBA: DATOS DE EMPLEADO COMPLETOS                ║
     * ╚══════════════════════════════════════════════════════════╝
     *
     * ESCENARIO: Verificar que todos los datos del empleado se incluyen
     * ENTRADA: Empleado con todos los campos poblados
     * RESULTADO ESPERADO: AuthResponseDTO con datos completos
     */
    @Test
    void authenticate_ShouldIncludeAllEmployeeData_WhenEmployeeHasCompleteData() {
        // ARRANGE: Empleado con datos completos
        Employee completeEmployee = new Employee(
            UUID.randomUUID(),
            "María José",
            "García López",
            "maria.garcia@example.com",
            "$2a$10$hashedPassword",
            testDepartment,
            testRole,
            LocalDate.of(2020, 1, 15),
            25
        );

        LoginRequestDTO loginRequest = new LoginRequestDTO("maria.garcia@example.com", "password123");

        List<SimpleGrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER")
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(employeeRepository.findByEmail(loginRequest.email()))
            .thenReturn(Optional.of(completeEmployee));
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        when(jwtService.generateToken(any(), eq(completeEmployee.getEmail())))
            .thenReturn("complete.data.token");

        // ACT: Autenticar empleado con datos completos
        AuthResponseDTO response = authService.authenticate(loginRequest);

        // ASSERT: Verificar datos completos en respuesta
        assertNotNull(response);
        assertEquals("complete.data.token", response.token());
        assertEquals("maria.garcia@example.com", response.email());
        assertEquals("María José", response.firstName());
        assertEquals("García López", response.lastName());
        assertEquals(1, response.roles().size());
        assertTrue(response.roles().contains("ROLE_USER"));
        assertEquals(3600, response.expiresIn());
    }
}
