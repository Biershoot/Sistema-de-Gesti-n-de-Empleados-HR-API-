package com.alejandro.microservices.hr_api.domain.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para ResourceNotFoundException.
 *
 * @author Sistema HR API
 * @version 1.0
 * @since 2025-01-14
 */
class ResourceNotFoundExceptionTest {

    @Test
    @DisplayName("Constructor con mensaje debe crear excepción correctamente")
    void constructor_ShouldCreateException_WhenMessageProvided() {
        // ARRANGE
        String message = "Resource not found";

        // ACT
        ResourceNotFoundException exception = new ResourceNotFoundException(message);

        // ASSERT
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
        assertNotNull(exception);
    }

    @Test
    @DisplayName("Constructor con mensaje y causa debe crear excepción correctamente")
    void constructor_ShouldCreateException_WhenMessageAndCauseProvided() {
        // ARRANGE
        String message = "Resource not found";
        Throwable cause = new IllegalArgumentException("Invalid argument");

        // ACT
        ResourceNotFoundException exception = new ResourceNotFoundException(message, cause);

        // ASSERT
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertNotNull(exception);
    }

    @Test
    @DisplayName("forResource con UUID debe crear excepción correctamente")
    void forResource_ShouldCreateException_WhenUUIDProvided() {
        // ARRANGE
        String resourceType = "Employee";
        UUID resourceId = UUID.randomUUID();

        // ACT
        ResourceNotFoundException exception = ResourceNotFoundException.forResource(resourceType, resourceId);

        // ASSERT
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains(resourceType));
        assertTrue(exception.getMessage().contains(resourceId.toString()));
        assertTrue(exception.getMessage().contains("not found with ID:"));
    }

    @Test
    @DisplayName("forResource con String debe crear excepción correctamente")
    void forResource_ShouldCreateException_WhenStringIdProvided() {
        // ARRANGE
        String resourceType = "Department";
        String resourceId = "DEPT-001";

        // ACT
        ResourceNotFoundException exception = ResourceNotFoundException.forResource(resourceType, resourceId);

        // ASSERT
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains(resourceType));
        assertTrue(exception.getMessage().contains(resourceId));
        assertTrue(exception.getMessage().contains("not found with ID:"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Employee", "Department", "Role", "User", "Project"})
    @DisplayName("forResource debe manejar diferentes tipos de recursos")
    void forResource_ShouldHandleDifferentResourceTypes(String resourceType) {
        // ARRANGE
        UUID resourceId = UUID.randomUUID();

        // ACT
        ResourceNotFoundException exception = ResourceNotFoundException.forResource(resourceType, resourceId);

        // ASSERT
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains(resourceType));
        assertTrue(exception.getMessage().contains(resourceId.toString()));
    }

    @Test
    @DisplayName("Excepción debe poder ser lanzada y capturada")
    void exception_ShouldBeThrowableAndCatchable() {
        // ARRANGE
        String message = "Test throwing exception";

        // ACT & ASSERT
        ResourceNotFoundException thrownException = assertThrows(
            ResourceNotFoundException.class,
            () -> {
                throw new ResourceNotFoundException(message);
            }
        );

        assertEquals(message, thrownException.getMessage());
    }

    @Test
    @DisplayName("toString debe incluir información relevante")
    void toString_ShouldIncludeRelevantInformation() {
        // ARRANGE
        String message = "Employee not found with ID: 123";
        ResourceNotFoundException exception = new ResourceNotFoundException(message);

        // ACT
        String stringRepresentation = exception.toString();

        // ASSERT
        assertNotNull(stringRepresentation);
        assertTrue(stringRepresentation.contains("ResourceNotFoundException"));
        assertTrue(stringRepresentation.contains(message));
    }

    @Test
    @DisplayName("Factory methods deben crear instancias independientes")
    void factoryMethods_ShouldCreateIndependentInstances() {
        // ARRANGE
        String resourceType = "Employee";
        UUID uuid = UUID.randomUUID();
        String stringId = "EMP-001";

        // ACT
        ResourceNotFoundException exception1 = ResourceNotFoundException.forResource(resourceType, uuid);
        ResourceNotFoundException exception2 = ResourceNotFoundException.forResource(resourceType, stringId);
        ResourceNotFoundException exception3 = ResourceNotFoundException.forResource(resourceType, uuid);

        // ASSERT
        assertNotSame(exception1, exception2);
        assertNotSame(exception1, exception3);
        assertNotEquals(exception1.getMessage(), exception2.getMessage());
        assertEquals(exception1.getMessage(), exception3.getMessage());
    }

    @Test
    @DisplayName("Factory methods deben generar mensajes consistentes")
    void factoryMethods_ShouldGenerateConsistentMessages() {
        // ARRANGE
        String resourceType = "User";
        String stringId = "user123";
        UUID uuidFromString = UUID.nameUUIDFromBytes(stringId.getBytes());

        // ACT
        ResourceNotFoundException exceptionWithString = ResourceNotFoundException.forResource(resourceType, stringId);
        ResourceNotFoundException exceptionWithUUID = ResourceNotFoundException.forResource(resourceType, uuidFromString);

        // ASSERT
        assertTrue(exceptionWithString.getMessage().contains("User not found with ID:"));
        assertTrue(exceptionWithUUID.getMessage().contains("User not found with ID:"));
        assertTrue(exceptionWithString.getMessage().contains(stringId));
        assertTrue(exceptionWithUUID.getMessage().contains(uuidFromString.toString()));
    }

    @Test
    @DisplayName("Constructor debe manejar mensajes nulos")
    void constructor_ShouldHandleNullMessage() {
        // ACT & ASSERT
        assertDoesNotThrow(() -> {
            ResourceNotFoundException exception = new ResourceNotFoundException(null);
            assertNotNull(exception);
        });
    }

    @Test
    @DisplayName("forResource debe manejar parámetros nulos")
    void forResource_ShouldHandleNullParameters() {
        // ACT & ASSERT
        assertDoesNotThrow(() -> {
            ResourceNotFoundException exception1 = ResourceNotFoundException.forResource(null, UUID.randomUUID());
            ResourceNotFoundException exception2 = ResourceNotFoundException.forResource("Type", (UUID) null);
            ResourceNotFoundException exception3 = ResourceNotFoundException.forResource("Type", (String) null);

            assertNotNull(exception1);
            assertNotNull(exception2);
            assertNotNull(exception3);
        });
    }
}
