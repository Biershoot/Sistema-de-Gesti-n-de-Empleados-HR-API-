package com.alejandro.microservices.hr_api.application.dto;

import java.util.List;

/**
 * DTO para la respuesta de autenticación.
 *
 * Contiene toda la información necesaria que el cliente necesita
 * después de una autenticación exitosa, incluyendo el token JWT
 * y metadatos del usuario autenticado.
 *
 * @param token Token JWT generado para la sesión
 * @param username Nombre de usuario autenticado
 * @param roles Lista de roles asignados al usuario
 * @param expiresIn Tiempo de expiración del token en segundos
 *
 * @author Sistema HR API
 * @version 1.0
 * @since 2025-01-14
 */
public record AuthResponseDTO(
    String token,
    String username,
    List<String> roles,
    long expiresIn
) {

    /**
     * Constructor de conveniencia para respuestas simples con un solo rol.
     *
     * @param token Token JWT generado
     * @param username Nombre de usuario
     * @param role Rol único del usuario
     * @param expiresIn Tiempo de expiración en segundos
     */
    public AuthResponseDTO(String token, String username, String role, long expiresIn) {
        this(token, username, List.of(role), expiresIn);
    }
}
