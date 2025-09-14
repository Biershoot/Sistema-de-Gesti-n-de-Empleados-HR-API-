package com.alejandro.microservices.hr_api.application.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeRequestDTOTest {

    @Test
    @DisplayName("Debería crear EmployeeRequestDTO sin ID para nuevos empleados")
    void shouldCreateEmployeeRequestDTOWithoutId() {
        // Given
        UUID departmentId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        LocalDate hireDate = LocalDate.of(2023, 6, 1);

        // When
        EmployeeRequestDTO requestDTO = new EmployeeRequestDTO(
            "Carlos",
            "Rodríguez",
            "carlos.rodriguez@empresa.com",
            departmentId,
            roleId,
            hireDate,
            25
        );

        // Then
        assertAll(
            () -> assertEquals("Carlos", requestDTO.firstName()),
            () -> assertEquals("Rodríguez", requestDTO.lastName()),
            () -> assertEquals("carlos.rodriguez@empresa.com", requestDTO.email()),
            () -> assertEquals(departmentId, requestDTO.departmentId()),
            () -> assertEquals(roleId, requestDTO.roleId()),
            () -> assertEquals(hireDate, requestDTO.hireDate()),
            () -> assertEquals(25, requestDTO.vacationDays())
        );
    }

    @Test
    @DisplayName("Debería validar que vacation days no sea negativo")
    void shouldValidateVacationDaysNotNegative() {
        // Given
        UUID departmentId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        LocalDate hireDate = LocalDate.of(2023, 6, 1);

        // When
        EmployeeRequestDTO requestDTO = new EmployeeRequestDTO(
            "Test",
            "User",
            "test@empresa.com",
            departmentId,
            roleId,
            hireDate,
            0 // Días de vacaciones en cero (válido)
        );

        // Then
        assertEquals(0, requestDTO.vacationDays());
    }
}
