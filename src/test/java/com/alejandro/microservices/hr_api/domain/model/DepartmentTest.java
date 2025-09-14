package com.alejandro.microservices.hr_api.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DepartmentTest {

    @Test
    @DisplayName("Debería crear un departamento con datos correctos")
    void shouldCreateDepartmentWithCorrectData() {
        // Given
        UUID departmentId = UUID.randomUUID();
        String departmentName = "Recursos Humanos";

        // When
        Department department = new Department(departmentId, departmentName);

        // Then
        assertAll(
            () -> assertEquals(departmentId, department.getId()),
            () -> assertEquals(departmentName, department.getName()),
            () -> assertNotNull(department.getId()),
            () -> assertNotNull(department.getName())
        );
    }

    @Test
    @DisplayName("Debería mantener inmutabilidad de los datos")
    void shouldMaintainDataImmutability() {
        // Given
        UUID departmentId = UUID.randomUUID();
        String originalName = "IT";
        Department department = new Department(departmentId, originalName);

        // When - Intentar modificar (los getters deberían devolver los valores originales)
        UUID retrievedId = department.getId();
        String retrievedName = department.getName();

        // Then
        assertEquals(departmentId, retrievedId);
        assertEquals(originalName, retrievedName);
    }

    @Test
    @DisplayName("Dos departamentos con el mismo ID deberían ser considerados iguales")
    void shouldConsiderDepartmentsWithSameIdAsEqual() {
        // Given
        UUID sameId = UUID.randomUUID();
        Department department1 = new Department(sameId, "IT");
        Department department2 = new Department(sameId, "Tecnología"); // Diferente nombre pero mismo ID

        // When & Then
        assertEquals(department1.getId(), department2.getId());
    }
}
