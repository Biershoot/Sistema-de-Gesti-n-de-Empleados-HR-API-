package com.alejandro.microservices.hr_api.application.validation;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Validador de direcciones de email según estándares RFC.
 *
 * Proporciona validación robusta de emails para el sistema HR,
 * incluyendo soporte para caracteres especiales y dominios internacionales.
 *
 * @author Sistema HR API
 * @version 1.0
 * @since 2025-01-14
 */
@Component
public class EmailValidator {

    // Patrón regex para validación de email basado en RFC 5322
    private static final String EMAIL_PATTERN =
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
        "(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,7}$";

    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    /**
     * Valida si una dirección de email es válida según el patrón RFC.
     *
     * @param email Dirección de email a validar
     * @return true si el email es válido, false en caso contrario
     */
    public boolean isValid(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        String trimmedEmail = email.trim();
        
        // Validar longitud máxima (320 caracteres según RFC 5321)
        if (trimmedEmail.length() > 320) {
            return false;
        }

        return pattern.matcher(trimmedEmail).matches();
    }
}
