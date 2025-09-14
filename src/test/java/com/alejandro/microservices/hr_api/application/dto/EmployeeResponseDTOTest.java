package com.alejandro.microservices.hr_api.application.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeResponseDTOTest {

    @Test
    @DisplayName("Debería crear EmployeeResponseDTO con objetos anidados completos")
    void shouldCreateEmployeeResponseDTOWithNestedObjects() {
        // Given
        UUID employeeId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        LocalDate hireDate = LocalDate.of(2023, 3, 10);

        DepartmentDTO department = new DepartmentDTO(departmentId, "Marketing");
        RoleDTO role = new RoleDTO(roleId, "Marketing Manager");

        // When
        EmployeeResponseDTO responseDTO = new EmployeeResponseDTO(
            employeeId,
            "María",
            "López",
            "maria.lopez@empresa.com",
            department,
            role,
            hireDate,
            30
        );

        // Then
        assertAll(
            () -> assertEquals(employeeId, responseDTO.id()),
            () -> assertEquals("María", responseDTO.firstName()),
            () -> assertEquals("López", responseDTO.lastName()),
            () -> assertEquals("maria.lopez@empresa.com", responseDTO.email()),
            () -> assertEquals(department, responseDTO.department()),
            () -> assertEquals(role, responseDTO.role()),
            () -> assertEquals(hireDate, responseDTO.hireDate()),
            () -> assertEquals(30, responseDTO.vacationDays()),
            () -> assertEquals("Marketing", responseDTO.department().name()),
            () -> assertEquals("Marketing Manager", responseDTO.role().name())
        );
    }

    @Test
    @DisplayName("Debería mantener la integridad de los objetos anidados")
    void shouldMaintainNestedObjectsIntegrity() {
        // Given
        DepartmentDTO department = new DepartmentDTO(UUID.randomUUID(), "Ventas");
        RoleDTO role = new RoleDTO(UUID.randomUUID(), "Sales Representative");

        EmployeeResponseDTO responseDTO = new EmployeeResponseDTO(
            UUID.randomUUID(),
            "Pedro",
            "Martínez",
            "pedro@empresa.com",
            department,
            role,
            LocalDate.now(),
            15
        );

        // When & Then
        assertNotNull(responseDTO.department());
        assertNotNull(responseDTO.role());
        assertEquals(department.id(), responseDTO.department().id());
        assertEquals(role.id(), responseDTO.role().id());
    }
}
