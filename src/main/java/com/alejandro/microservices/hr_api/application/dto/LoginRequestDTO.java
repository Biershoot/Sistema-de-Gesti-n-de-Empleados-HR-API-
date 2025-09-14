package com.alejandro.microservices.hr_api.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para solicitudes de login de usuarios.
 *
 * Contiene las credenciales necesarias para autenticación:
 * - Email como nombre de usuario
 * - Contraseña
 */
public record LoginRequestDTO(
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    String password
) {}
