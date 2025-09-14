package com.alejandro.microservices.hr_api.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para la solicitud de registro de nuevos usuarios.
 *
 * Representa los datos necesarios para crear un nuevo usuario en el sistema.
 * Incluye validaciones robustas para asegurar la calidad de los datos.
 *
 * @param username Nombre de usuario único (3-50 caracteres, alfanumérico)
 * @param password Contraseña (mínimo 8 caracteres, debe incluir mayúscula, minúscula y número)
 * @param role Rol del usuario (ADMIN, USER, MANAGER, HR_SPECIALIST)
 *
 * @author Sistema HR API
 * @version 1.0
 * @since 2025-01-14
 */
public record RegisterRequestDTO(
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
             message = "El nombre de usuario solo puede contener letras, números, puntos, guiones y guiones bajos")
    String username,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
             message = "La contraseña debe contener al menos una letra minúscula, una mayúscula y un número")
    String password,

    @NotBlank(message = "El rol es obligatorio")
    @Pattern(regexp = "^(ADMIN|USER|MANAGER|HR_SPECIALIST)$",
             message = "El rol debe ser uno de: ADMIN, USER, MANAGER, HR_SPECIALIST")
    String role
) {}
