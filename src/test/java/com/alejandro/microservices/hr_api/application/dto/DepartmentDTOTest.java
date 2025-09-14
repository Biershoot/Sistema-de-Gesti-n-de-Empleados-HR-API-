package com.alejandro.microservices.hr_api.application.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DepartmentDTOTest {

    @Test
    @DisplayName("Debería crear DepartmentDTO con datos correctos")
    void shouldCreateDepartmentDTOWithCorrectData() {
        // Given
        UUID departmentId = UUID.randomUUID();
        String departmentName = "Recursos Humanos";

        // When
        DepartmentDTO departmentDTO = new DepartmentDTO(departmentId, departmentName);

        // Then
        assertAll(
            () -> assertEquals(departmentId, departmentDTO.id()),
            () -> assertEquals(departmentName, departmentDTO.name()),
            () -> assertNotNull(departmentDTO.id()),
            () -> assertNotNull(departmentDTO.name())
        );
    }

    @Test
    @DisplayName("Dos DepartmentDTOs con los mismos datos deberían ser iguales")
    void shouldBeEqualWhenSameData() {
        // Given
        UUID id = UUID.randomUUID();
        String name = "IT";

        DepartmentDTO dto1 = new DepartmentDTO(id, name);
        DepartmentDTO dto2 = new DepartmentDTO(id, name);

        // When & Then
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    @DisplayName("DepartmentDTOs con datos diferentes no deberían ser iguales")
    void shouldNotBeEqualWhenDifferentData() {
        // Given
        DepartmentDTO dto1 = new DepartmentDTO(UUID.randomUUID(), "IT");
        DepartmentDTO dto2 = new DepartmentDTO(UUID.randomUUID(), "HR");

        // When & Then
        assertNotEquals(dto1, dto2);
    }
}
