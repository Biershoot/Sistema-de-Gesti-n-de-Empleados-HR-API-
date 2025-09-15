package com.alejandro.microservices.hr_api.infrastructure.controller;

import com.alejandro.microservices.hr_api.application.dto.AuthResponseDTO;
import com.alejandro.microservices.hr_api.application.dto.LoginRequestDTO;
import com.alejandro.microservices.hr_api.application.dto.RegisterRequestDTO;
import com.alejandro.microservices.hr_api.application.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para operaciones de autenticación.
 *
 * Proporciona endpoints para:
 * - Registro de nuevos usuarios
 * - Autenticación de usuarios existentes
 * - Validación de tokens JWT
 * - Verificación de disponibilidad de username
 *
 * Todos los endpoints incluyen validación de datos y manejo de errores apropiado.
 *
 * @author Sistema HR API
 * @version 1.0
 * @since 2025-01-14
 */
// @Tag(name = "Authentication", description = "Operaciones de autenticación y registro de usuarios")
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registra un nuevo usuario en el sistema.
     * 
     * Este endpoint permite crear una nueva cuenta de usuario con validación
     * de datos, verificación de unicidad de email/username y generación
     * automática de token JWT para autenticación inmediata.
     *
     * @param request DTO con los datos del nuevo usuario (username, email, password, role)
     * @return ResponseEntity con el token JWT y datos del usuario registrado
     * @throws ValidationException si los datos proporcionados no son válidos
     * @throws BusinessException si el email o username ya están en uso
     */
    // @Operation(summary = "Registrar nuevo usuario")
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDTO request) {
        try {
            AuthResponseDTO response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error en el registro");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            error.put("message", "Ocurrió un error inesperado durante el registro");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Autentica un usuario existente.
     *
     * @param request Credenciales de login (validadas)
     * @return ResponseEntity con el token JWT y datos del usuario autenticado
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO request) {
        try {
            AuthResponseDTO response = authService.authenticate(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error de autenticación");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            error.put("message", "Ocurrió un error inesperado durante la autenticación");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Valida un token JWT.
     *
     * @param authHeader Header de autorización con el token Bearer
     * @return ResponseEntity con la información del usuario si el token es válido
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            // Extraer token del header Authorization
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Token requerido");
                error.put("message", "Header Authorization con Bearer token es requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            String token = authHeader.substring(7); // Remover "Bearer "
            AuthResponseDTO response = authService.validateToken(token);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Token inválido");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            error.put("message", "Ocurrió un error inesperado durante la validación");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Verifica si un username está disponible.
     *
     * @param username Username a verificar
     * @return ResponseEntity indicando si el username está disponible
     */
    @GetMapping("/check-username/{username}")
    public ResponseEntity<Map<String, Object>> checkUsernameAvailability(@PathVariable String username) {
        try {
            boolean available = authService.isUsernameAvailable(username);
            Map<String, Object> response = new HashMap<>();
            response.put("username", username);
            response.put("available", available);
            response.put("message", available ? "Username disponible" : "Username ya está en uso");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error verificando username");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Endpoint de salud para verificar que el servicio de autenticación está funcionando.
     *
     * @return ResponseEntity con el estado del servicio
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Authentication Service");
        response.put("timestamp", java.time.Instant.now().toString());
        return ResponseEntity.ok(response);
    }
}
