package com.alejandro.microservices.hr_api.domain.exception;

import java.util.UUID;

/**
 * Excepción personalizada para cuando un recurso no es encontrado.
 *
 * Extiende RuntimeException para permitir manejo no verificado de errores
 * cuando los recursos solicitados no existen en el sistema.
 *
 * @author Sistema HR API
 * @version 1.0
 * @since 2025-01-14
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructor con mensaje personalizado.
     *
     * @param message Mensaje descriptivo del error
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor con mensaje y causa.
     *
     * @param message Mensaje descriptivo del error
     * @param cause Causa original del error
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Factory method para crear excepción de recurso no encontrado por tipo y ID.
     *
     * @param resourceType Tipo de recurso (ej: "Employee", "Department")
     * @param resourceId ID del recurso
     * @return Nueva instancia de ResourceNotFoundException
     */
    public static ResourceNotFoundException forResource(String resourceType, UUID resourceId) {
        return new ResourceNotFoundException(
            String.format("%s not found with ID: %s", resourceType, resourceId)
        );
    }

    /**
     * Factory method para crear excepción de recurso no encontrado por tipo y ID string.
     *
     * @param resourceType Tipo de recurso (ej: "Employee", "Department")
     * @param resourceId ID del recurso como string
     * @return Nueva instancia de ResourceNotFoundException
     */
    public static ResourceNotFoundException forResource(String resourceType, String resourceId) {
        return new ResourceNotFoundException(
            String.format("%s not found with ID: %s", resourceType, resourceId)
        );
    }
}
