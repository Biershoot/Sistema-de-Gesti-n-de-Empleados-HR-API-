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
 * Servicio de autenticación que maneja login y generación de tokens JWT.
 *
 * Responsabilidades:
 * - Autenticar usuarios con email y contraseña
 * - Generar tokens JWT con información del usuario
 * - Extraer roles y permisos del usuario autenticado
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmployeeRepository employeeRepository;

    public AuthService(AuthenticationManager authenticationManager,
                      JwtService jwtService,
                      EmployeeRepository employeeRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Autentica un usuario y genera un token JWT.
     *
     * @param loginRequest Datos de login (email y contraseña)
     * @return Respuesta con token JWT e información del usuario
     * @throws RuntimeException Si las credenciales son inválidas
     */
    public AuthResponseDTO authenticate(LoginRequestDTO loginRequest) {
        // Autenticar con Spring Security
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.email(),
                loginRequest.password()
            )
        );

        // Obtener información del empleado
        Employee employee = employeeRepository.findByEmail(loginRequest.email())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Extraer roles del usuario autenticado
        List<String> roles = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();

        // Crear claims personalizados para el JWT
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        claims.put("employeeId", employee.getId().toString());
        claims.put("departmentId", employee.getDepartment().getId().toString());

        // Generar token JWT
        String token = jwtService.generateToken(claims, employee.getEmail());

        // Construir respuesta
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
     * Valida un token JWT y extrae información del usuario.
     *
     * @param token Token JWT a validar
     * @return Información del usuario si el token es válido
     * @throws RuntimeException Si el token es inválido
     */
    public AuthResponseDTO validateToken(String token) {
        String email = jwtService.extractUsername(token);

        Employee employee = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!jwtService.isTokenValid(token, email)) {
            throw new RuntimeException("Token inválido");
        }

        // Extraer roles del token
        @SuppressWarnings("unchecked")
        List<String> roles = jwtService.extractClaim(token, claims ->
            (List<String>) claims.get("roles"));

        return new AuthResponseDTO(
            token,
            employee.getEmail(),
            employee.getFirstName(),
            employee.getLastName(),
            roles,
            0 // No renovamos el tiempo de expiración
        );
    }
}
