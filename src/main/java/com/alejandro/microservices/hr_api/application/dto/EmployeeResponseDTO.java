package com.alejandro.microservices.hr_api.application.dto;

import java.time.LocalDate;
import java.util.UUID;

public record EmployeeResponseDTO(
        UUID id,
        String firstName,
        String lastName,
        String email,
        DepartmentDTO department,
        RoleDTO role,
        LocalDate hireDate,
        int vacationDays
) {}
