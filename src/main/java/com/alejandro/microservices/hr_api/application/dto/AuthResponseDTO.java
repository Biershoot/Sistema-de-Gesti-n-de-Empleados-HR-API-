package com.alejandro.microservices.hr_api.application.dto;

import java.util.List;

/**
 * DTO para respuestas de autenticación exitosa.
 *
 * Contiene la información que se devuelve al cliente
 * después de una autenticación exitosa:
 * - Token JWT para futuras peticiones
 * - Información básica del usuario autenticado
 * - Roles/permisos del usuario
 */
public record AuthResponseDTO(
    String token,
    String email,
    String firstName,
    String lastName,
    List<String> roles,
    long expiresIn
) {}
