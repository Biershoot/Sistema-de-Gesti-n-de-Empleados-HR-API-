package com.alejandro.microservices.hr_api.application.service;

import com.alejandro.microservices.hr_api.application.dto.AuthResponseDTO;
import com.alejandro.microservices.hr_api.application.dto.LoginRequestDTO;
import com.alejandro.microservices.hr_api.application.dto.RegisterRequestDTO;
import com.alejandro.microservices.hr_api.domain.model.User;
import com.alejandro.microservices.hr_api.domain.repository.UserRepository;
import com.alejandro.microservices.hr_api.infrastructure.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio de autenticación JWT completo.
 *
 * Proporciona funcionalidades para:
 * - Registro de nuevos usuarios con validaciones
 * - Autenticación de usuarios existentes
 * - Generación y validación de tokens JWT
 * - Manejo de roles y permisos
 *
 * Integra Spring Security para autenticación segura y gestión de roles.
 *
 * @author Sistema HR API
 * @version 2.0
 * @since 2025-01-14
 */
@Service
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       PasswordEncoder passwordEncoder,
                       UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param request Datos del nuevo usuario
     * @return Respuesta con token JWT y datos del usuario
     * @throws RuntimeException si el username ya existe
     */
    public AuthResponseDTO register(RegisterRequestDTO request) {
        // Verificar si el usuario ya existe
        if (userRepository.existsByUsername(request.username())) {
            throw new RuntimeException("El nombre de usuario ya está en uso");
        }

        // Crear nuevo usuario con contraseña hasheada
        User user = new User(
                request.username(),
                passwordEncoder.encode(request.password()),
                "ROLE_" + request.role().toUpperCase()
        );

        // Guardar usuario en la base de datos
        User savedUser = userRepository.save(user);

        // Generar token JWT con claims del usuario
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", savedUser.getRole());
        claims.put("userId", savedUser.getId().toString());

        String token = jwtService.generateToken(claims, savedUser.getUsername());

        return new AuthResponseDTO(
                token,
                savedUser.getUsername(),
                savedUser.getRole(),
                jwtService.getExpirationTime()
        );
    }

    /**
     * Autentica un usuario existente.
     *
     * @param request Credenciales de login
     * @return Respuesta con token JWT y datos del usuario
     * @throws RuntimeException si las credenciales son inválidas
     */
    public AuthResponseDTO authenticate(LoginRequestDTO request) {
        // Autenticar credenciales con Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        // Buscar usuario en la base de datos
        User user = userRepository.findByUsernameAndEnabled(request.username())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado o deshabilitado"));

        // Extraer roles de la autenticación
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Generar token JWT con claims del usuario
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole());
        claims.put("userId", user.getId().toString());
        claims.put("roles", roles);

        String token = jwtService.generateToken(claims, user.getUsername());

        return new AuthResponseDTO(
                token,
                user.getUsername(),
                roles,
                jwtService.getExpirationTime()
        );
    }

    /**
     * Valida un token JWT y retorna información del usuario.
     *
     * @param token Token JWT a validar
     * @return Información del usuario si el token es válido
     * @throws RuntimeException si el token es inválido
     */
    public AuthResponseDTO validateToken(String token) {
        // Validar entrada
        if (token == null || token.trim().isEmpty()) {
            throw new RuntimeException("Token no puede estar vacío");
        }

        try {
            // Extraer username del token
            String username = jwtService.extractUsername(token);

            // Buscar usuario en la base de datos
            User user = userRepository.findByUsernameAndEnabled(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Validar que el token sea válido para este usuario
            if (!jwtService.isTokenValid(token, username)) {
                throw new RuntimeException("Token inválido o expirado");
            }

            // Extraer roles del token
            @SuppressWarnings("unchecked")
            List<String> roles = jwtService.extractClaim(token, claims ->
                (List<String>) claims.get("roles"));

            if (roles == null) {
                roles = List.of(user.getRole());
            }

            return new AuthResponseDTO(
                    token,
                    user.getUsername(),
                    roles,
                    0 // No renovamos el tiempo de expiración en validación
            );

        } catch (Exception e) {
            throw new RuntimeException("Error validando token: " + e.getMessage());
        }
    }

    /**
     * Verifica si un username está disponible.
     *
     * @param username Username a verificar
     * @return true si está disponible, false si ya existe
     */
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    /**
     * Obtiene todos los usuarios que tienen un rol específico.
     *
     * @param role Rol a buscar
     * @return Lista de usuarios con el rol especificado
     */
    @Transactional(readOnly = true)
    public List<User> getUsersByRole(String role) {
        return userRepository.findByRoleAndEnabled("ROLE_" + role.toUpperCase());
    }
}
