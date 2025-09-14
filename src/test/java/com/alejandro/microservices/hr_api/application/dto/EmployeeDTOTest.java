package com.alejandro.microservices.hr_api.application.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeDTOTest {

    @Test
    @DisplayName("Debería crear EmployeeDTO con todos los campos correctos")
    void shouldCreateEmployeeDTOWithAllFields() {
        // Given
        UUID id = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        LocalDate hireDate = LocalDate.of(2023, 1, 15);

        // When
        EmployeeDTO employeeDTO = new EmployeeDTO(
            id,
            "Ana",
            "García",
            "ana.garcia@empresa.com",
            departmentId,
            roleId,
            hireDate,
            20
        );

        // Then
        assertAll(
            () -> assertEquals(id, employeeDTO.id()),
            () -> assertEquals("Ana", employeeDTO.firstName()),
            () -> assertEquals("García", employeeDTO.lastName()),
            () -> assertEquals("ana.garcia@empresa.com", employeeDTO.email()),
            () -> assertEquals(departmentId, employeeDTO.departmentId()),
            () -> assertEquals(roleId, employeeDTO.roleId()),
            () -> assertEquals(hireDate, employeeDTO.hireDate()),
            () -> assertEquals(20, employeeDTO.vacationDays())
        );
    }

    @Test
    @DisplayName("Dos EmployeeDTOs con los mismos datos deberían ser iguales")
    void shouldBeEqualWhenSameData() {
        // Given
        UUID id = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        LocalDate hireDate = LocalDate.of(2023, 1, 15);

        EmployeeDTO dto1 = new EmployeeDTO(id, "Ana", "García", "ana@empresa.com", departmentId, roleId, hireDate, 20);
        EmployeeDTO dto2 = new EmployeeDTO(id, "Ana", "García", "ana@empresa.com", departmentId, roleId, hireDate, 20);

        // When & Then
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }
}
