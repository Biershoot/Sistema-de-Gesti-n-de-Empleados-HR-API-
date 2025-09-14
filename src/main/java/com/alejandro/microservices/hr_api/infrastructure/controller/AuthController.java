package com.alejandro.microservices.hr_api.infrastructure.controller;

import com.alejandro.microservices.hr_api.application.dto.AuthResponseDTO;
import com.alejandro.microservices.hr_api.application.dto.LoginRequestDTO;
import com.alejandro.microservices.hr_api.application.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para operaciones de autenticación.
 *
 * Endpoints disponibles:
 * - POST /auth/login - Autenticación de usuarios
 * - POST /auth/validate - Validación de tokens JWT
 *
 * Estos endpoints son públicos (no requieren autenticación previa).
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Endpoint para autenticación de usuarios.
     *
     * @param loginRequest Credenciales del usuario (email y contraseña)
     * @return Token JWT y información del usuario autenticado
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            AuthResponseDTO response = authService.authenticate(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // En producción, evitar exponer detalles del error
            throw new RuntimeException("Credenciales inválidas");
        }
    }

    /**
     * Endpoint para validar tokens JWT.
     *
     * @param token Token JWT a validar
     * @return Información del usuario si el token es válido
     */
    @PostMapping("/validate")
    public ResponseEntity<AuthResponseDTO> validateToken(@RequestParam String token) {
        try {
            AuthResponseDTO response = authService.validateToken(token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint de prueba para verificar que el servicio está funcionando.
     *
     * @return Mensaje de estado
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth service is running");
    }
}
