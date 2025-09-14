package com.alejandro.microservices.hr_api.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para la solicitud de login.
 *
 * Representa las credenciales de usuario para el proceso de autenticación.
 * Incluye validaciones para asegurar que los datos requeridos estén presentes.
 *
 * @param username Nombre de usuario (requerido, 3-50 caracteres)
 * @param password Contraseña (requerido, mínimo 6 caracteres)
 *
 * @author Sistema HR API
 * @version 1.0
 * @since 2025-01-14
 */
public record LoginRequestDTO(
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    String username,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    String password
) {}
