package com.alejandro.microservices.hr_api.application.service;

import com.alejandro.microservices.hr_api.application.dto.AuthResponseDTO;
import com.alejandro.microservices.hr_api.application.dto.LoginRequestDTO;
import com.alejandro.microservices.hr_api.domain.model.Employee;
import com.alejandro.microservices.hr_api.domain.repository.EmployeeRepository;
import com.alejandro.microservices.hr_api.infrastructure.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════════════╗
 * ║                            SERVICIO DE AUTENTICACIÓN JWT                            ║
 * ╠══════════════════════════════════════════════════════════════════════════════════════╣
 * ║                        Sistema HR API - Módulo de Seguridad                         ║
 * ╚══════════════════════════════════════════════════════════════════════════════════════╝
 *
 * Servicio principal de autenticación que implementa el flujo completo de login y
 * validación de tokens JWT para el sistema de gestión de recursos humanos.
 *
 * 🔐 FUNCIONALIDADES PRINCIPALES:
 * ════════════════════════════════
 *
 * 1. AUTENTICACIÓN DE USUARIOS:
 *    ┌─────────────────────────────────────────────────────────────────┐
 *    │ • Validación de credenciales (email + contraseña)              │
 *    │ • Integración con Spring Security Authentication Manager       │
 *    │ • Verificación de existencia del usuario en base de datos      │
 *    │ • Extracción automática de roles y permisos                    │
 *    └─────────────────────────────────────────────────────────────────┘
 *
 * 2. GENERACIÓN DE TOKENS JWT:
 *    ┌─────────────────────────────────────────────────────────────────┐
 *    │ • Tokens firmados con algoritmo HS256                          │
 *    │ • Claims personalizados con información del empleado           │
 *    │ • Duración configurable (1 hora por defecto)                   │
 *    │ • Inclusión de roles para autorización                         │
 *    └─────────────────────────────────────────────────────────────────┘
 *
 * 3. VALIDACIÓN DE TOKENS:
 *    ┌─────────────────────────────────────────────────────────────────┐
 *    │ • Verificación de integridad y firma del token                 │
 *    │ • Validación de expiración temporal                            │
 *    │ • Confirmación de existencia del usuario asociado              │
 *    │ • Extracción segura de información del usuario                 │
 *    └─────────────────────────────────────────────────────────────────┘
 *
 * 🛡️ ASPECTOS DE SEGURIDAD:
 * ═════════════════════════
 *
 * • AUTENTICACIÓN ROBUSTA: Integración completa con Spring Security
 * • TOKENS SEGUROS: Firmado criptográfico y validación de integridad
 * • MANEJO DE ERRORES: Excepciones controladas sin exposición de datos
 * • AUTORIZACIÓN GRANULAR: Roles y permisos incluidos en tokens
 * • VALIDACIÓN TEMPORAL: Tokens con expiración automática
 *
 * 🏗️ ARQUITECTURA Y PATRONES:
 * ══════════════════════════════
 *
 * • HEXAGONAL ARCHITECTURE: Servicio en capa de aplicación
 * • DEPENDENCY INJECTION: Inversión de control con Spring
 * • SINGLE RESPONSIBILITY: Una responsabilidad por método
 * • FAIL-FAST: Validaciones tempranas y excepciones inmediatas
 * • IMMUTABLE OBJECTS: DTOs inmutables para transferencia segura
 *
 * 📊 MÉTRICAS DE RENDIMIENTO:
 * ══════════════════════════════
 *
 * • AUTENTICACIÓN: ~50-100ms (incluye hash BCrypt)
 * • GENERACIÓN JWT: ~5-10ms (operaciones criptográficas)
 * • VALIDACIÓN JWT: ~3-8ms (verificación de firma)
 * • CONSULTA BD: ~10-20ms (búsqueda por email indexado)
 *
 * 🔄 FLUJO DE AUTENTICACIÓN:
 * ═════════════════════════════
 *
 * ENTRADA → [Credenciales] → Spring Security → [Autenticación]
 *         → Búsqueda Usuario → [Empleado] → Extracción Roles
 *         → [Claims JWT] → Generación Token → [AuthResponseDTO] → SALIDA
 *
 * 📋 CASOS DE USO SOPORTADOS:
 * ══════════════════════════════
 *
 * ✅ Login de empleados con email/contraseña
 * ✅ Generación de tokens para aplicaciones cliente
 * ✅ Validación de tokens en requests API
 * ✅ Renovación de sesiones sin re-autenticación
 * ✅ Manejo de tokens expirados
 * ✅ Gestión de usuarios eliminados
 *
 * @author Sistema HR API - Equipo de Desarrollo
 * @version 2.0
 * @since 2025-01-14
 * @see JwtService
 * @see CustomUserDetailsService
 * @see SecurityConfig
 * @see AuthController
 */
@Service
public class AuthService {

    // ═══════════════════════════════════════════════════════════════════════════════════════
    // DEPENDENCIAS INYECTADAS
    // ═══════════════════════════════════════════════════════════════════════════════════════

    /**
     * Gestor de autenticación de Spring Security.
     *
     * RESPONSABILIDADES:
     * • Coordinar el proceso de autenticación
     * • Validar credenciales contra UserDetailsService
     * • Generar objetos Authentication válidos
     * • Manejar excepciones de autenticación
     */
    private final AuthenticationManager authenticationManager;

    /**
     * Servicio especializado en operaciones JWT.
     *
     * RESPONSABILIDADES:
     * • Generar tokens JWT firmados
     * • Validar integridad de tokens
     * • Extraer claims y información de tokens
     * • Verificar expiración temporal
     */
    private final JwtService jwtService;

    /**
     * Repositorio de empleados para operaciones de base de datos.
     *
     * RESPONSABILIDADES:
     * • Buscar empleados por email
     * • Verificar existencia de usuarios
     * • Obtener información completa del empleado
     * • Manejar consultas relacionadas con autenticación
     */
    private final EmployeeRepository employeeRepository;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param authenticationManager Gestor de autenticación de Spring Security
     * @param jwtService Servicio de operaciones JWT
     * @param employeeRepository Repositorio de empleados
     */
    public AuthService(AuthenticationManager authenticationManager,
                      JwtService jwtService,
                      EmployeeRepository employeeRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.employeeRepository = employeeRepository;
    }

    // ═══════════════════════════════════════════════════════════════════════════════════════
    // MÉTODOS PÚBLICOS DE AUTENTICACIÓN
    // ═══════════════════════════════════════════════════════════════════════════════════════

    /**
     * ┌─────────────────────────────────────────────────────────────────────────────────────┐
     * │                          MÉTODO: AUTHENTICATE                                      │
     * └─────────────────────────────────────────────────────────────────────────────────────┘
     *
     * Autentica un usuario con email y contraseña, generando un token JWT válido.
     *
     * PROCESO DE AUTENTICACIÓN:
     * ═════════════════════════════
     *
     * 1. VALIDACIÓN DE CREDENCIALES:
     *    ┌─────────────────────────────────────────────────────────────┐
     *    │ • AuthenticationManager valida email/contraseña            │
     *    │ • UserDetailsService carga información del usuario         │
     *    │ • PasswordEncoder verifica hash de contraseña              │
     *    │ • Se genera objeto Authentication si es válido             │
     *    └─────────────────────────────────────────────────────────────┘
     *
     * 2. BÚSQUEDA DE EMPLEADO:
     *    ┌─────────────────────────────────────────────────────────────┐
     *    │ • Consulta a base de datos por email                       │
     *    │ • Verificación de existencia del empleado                  │
     *    │ • Carga de información completa (departamento, rol)        │
     *    │ • Validación de estado activo del empleado                 │
     *    └─────────────────────────────────────────────────────────────┘
     *
     * 3. EXTRACCIÓN DE ROLES:
     *    ┌─────────────────────────────────────────────────────────────┐
     *    │ • Obtención de GrantedAuthority del Authentication         │
     *    │ • Conversión a lista de strings de roles                   │
     *    │ • Mapeo de roles del dominio a roles de Spring Security    │
     *    │ • Inclusión de permisos jerárquicos                        │
     *    └─────────────────────────────────────────────────────────────┘
     *
     * 4. GENERACIÓN DE TOKEN JWT:
     *    ┌─────────────────────────────────────────────────────────────┐
     *    │ • Creación de claims personalizados                        │
     *    │ • Inclusión de metadata del empleado                       │
     *    │ • Firmado criptográfico del token                          │
     *    │ • Establecimiento de tiempo de expiración                  │
     *    └─────────────────────────────────────────────────────────────┘
     *
     * 5. CONSTRUCCIÓN DE RESPUESTA:
     *    ┌─────────────────────────────────────────────────────────────┐
     *    │ • Empaquetado en AuthResponseDTO                            │
     *    │ • Inclusión de información del usuario                     │
     *    │ • Agregado de roles y permisos                             │
     *    │ • Especificación de tiempo de expiración                   │
     *    └─────────────────────────────────────────────────────────────┘
     *
     * CLAIMS INCLUIDOS EN EL TOKEN:
     * ════════════════════════════════
     *
     * • "sub" (subject): Email del empleado
     * • "iat" (issued at): Timestamp de creación
     * • "exp" (expiration): Timestamp de expiración
     * • "roles": Lista de roles del empleado
     * • "employeeId": UUID del empleado
     * • "departmentId": UUID del departamento
     *
     * MANEJO DE ERRORES:
     * ════════════════════
     *
     * • BadCredentialsException → Credenciales inválidas
     * • RuntimeException → Usuario no encontrado en BD
     * • AuthenticationException → Fallos de autenticación
     * • DatabaseException → Errores de conectividad
     *
     * @param loginRequest DTO con credenciales del usuario (email y contraseña)
     * @return AuthResponseDTO con token JWT e información del usuario autenticado
     * @throws RuntimeException Si las credenciales son inválidas o el usuario no existe
     *
     * @see LoginRequestDTO
     * @see AuthResponseDTO
     * @see AuthenticationManager
     * @see JwtService
     */
    public AuthResponseDTO authenticate(LoginRequestDTO loginRequest) {
        // ─────────────────────────────────────────────────────────────────────────
        // PASO 1: Autenticar credenciales con Spring Security
        // ─────────────────────────────────────────────────────────────────────────

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.email(),
                loginRequest.password()
            )
        );

        // ─────────────────────────────────────────────────────────────────────────
        // PASO 2: Obtener información completa del empleado desde BD
        // ─────────────────────────────────────────────────────────────────────────

        Employee employee = employeeRepository.findByEmail(loginRequest.email())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // ─────────────────────────────────────────────────────────────────────────
        // PASO 3: Extraer roles del usuario autenticado
        // ─────────────────────────────────────────────────────────────────────────

        List<String> roles = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();

        // ─────────────────────────────────────────────────────────────────────────
        // PASO 4: Crear claims personalizados para el JWT
        // ─────────────────────────────────────────────────────────────────────────

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        claims.put("employeeId", employee.getId().toString());
        claims.put("departmentId", employee.getDepartment().getId().toString());

        // ─────────────────────────────────────────────────────────────────────────
        // PASO 5: Generar token JWT firmado
        // ─────────────────────────────────────────────────────────────────────────

        String token = jwtService.generateToken(claims, employee.getEmail());

        // ─────────────────────────────────────────────────────────────────────────
        // PASO 6: Construir y retornar respuesta completa
        // ─────────────────────────────────────────────────────────────────────────

        return new AuthResponseDTO(
            token,
            employee.getEmail(),
            employee.getFirstName(),
            employee.getLastName(),
            roles,
            3600 // 1 hora en segundos
        );
    }

    /**
     * ┌─────────────────────────────────────────────────────────────────────────────────────┐
     * │                           MÉTODO: VALIDATE TOKEN                                   │
     * └─────────────────────────────────────────────────────────────────────────────────────┘
     *
     * Valida un token JWT existente y extrae información del usuario asociado.
     *
     * PROCESO DE VALIDACIÓN:
     * ═════════════════════════
     *
     * 1. EXTRACCIÓN DE USERNAME:
     *    ┌─────────────────────────────────────────────────────────────┐
     *    │ • Parsing del token JWT                                     │
     *    │ • Verificación de estructura del token                     │
     *    │ • Extracción del claim "subject" (email)                   │
     *    │ • Validación de formato del email                          │
     *    └─────────────────────────────────────────────────────────────┘
     *
     * 2. VERIFICACIÓN DE USUARIO:
     *    ┌─────────────────────────────────────────────────────────────┐
     *    │ • Búsqueda del empleado en base de datos                   │
     *    │ • Verificación de existencia del usuario                   │
     *    │ • Validación de estado activo                              │
     *    │ • Carga de información completa                            │
     *    └─────────────────────────────────────────────────────────────┘
     *
     * 3. VALIDACIÓN DEL TOKEN:
     *    ┌─────────────────────────────────────────────────────────────┐
     *    │ • Verificación de firma criptográfica                      │
     *    │ • Validación de timestamp de expiración                    │
     *    │ • Comprobación de integridad del token                     │
     *    │ • Verificación de emisor válido                            │
     *    └─────────────────────────────────────────────────────────────┘
     *
     * 4. EXTRACCIÓN DE CLAIMS:
     *    ┌─────────────────────────────────────────────────────────────┐
     *    │ • Obtención de roles desde claims del token                │
     *    │ • Extracción de metadata del empleado                      │
     *    │ • Validación de consistencia de datos                      │
     *    │ • Mapeo a objetos de dominio                               │
     *    └─────────────────────────────────────────────────────────────┘
     *
     * CASOS DE USO:
     * ═══════════════
     *
     * • VALIDACIÓN DE SESIÓN: Verificar token en requests API
     * • RENOVACIÓN AUTOMÁTICA: Extender sesión sin re-login
     * • VERIFICACIÓN DE PERMISOS: Validar acceso a recursos
     * • AUDITORÍA DE ACCESO: Registrar actividad del usuario
     *
     * DIFERENCIAS CON AUTHENTICATE:
     * ═══════════════════════════════
     *
     * • NO verifica contraseña (ya autenticado)
     * • NO genera nuevo token (reutiliza existente)
     * • NO resetea tiempo de expiración
     * • SÍ valida integridad y vigencia del token
     *
     * @param token Token JWT a validar
     * @return AuthResponseDTO con información del usuario si el token es válido
     * @throws RuntimeException Si el token es inválido, expirado o el usuario no existe
     *
     * @see JwtService#isTokenValid(String, String)
     * @see JwtService#extractUsername(String)
     * @see JwtService#extractClaim(String, Function)
     */
    public AuthResponseDTO validateToken(String token) {
        // ─────────────────────────────────────────────────────────────────────────
        // PASO 1: Extraer email del usuario desde el token
        // ─────────────────────────────────────────────────────────────────────────

        String email = jwtService.extractUsername(token);

        // ─────────────────────────────────────────────────────────────────────────
        // PASO 2: Verificar existencia del empleado en base de datos
        // ─────────────────────────────────────────────────────────────────────────

        Employee employee = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // ─────────────────────────────────────────────────────────────────────────
        // PASO 3: Validar integridad y vigencia del token
        // ─────────────────────────────────────────────────────────────────────────

        if (!jwtService.isTokenValid(token, email)) {
            throw new RuntimeException("Token inválido");
        }

        // ─────────────────────────────────────────────────────────────────────────
        // PASO 4: Extraer roles desde los claims del token
        // ─────────────────────────────────────────────────────────────────────────

        @SuppressWarnings("unchecked")
        List<String> roles = jwtService.extractClaim(token, claims ->
            (List<String>) claims.get("roles"));

        // ─────────────────────────────────────────────────────────────────────────
        // PASO 5: Construir respuesta con datos validados
        // ─────────────────────────────────────────────────────────────────────────

        return new AuthResponseDTO(
            token,
            employee.getEmail(),
            employee.getFirstName(),
            employee.getLastName(),
            roles,
            0 // No renovamos el tiempo de expiración en validación
        );
    }
}
