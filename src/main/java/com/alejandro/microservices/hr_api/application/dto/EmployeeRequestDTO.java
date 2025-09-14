package com.alejandro.microservices.hr_api.application.dto;

import java.time.LocalDate;
import java.util.UUID;

public record EmployeeRequestDTO(
        String firstName,
        String lastName,
        String email,
        UUID departmentId,
        UUID roleId,
        LocalDate hireDate,
        int vacationDays
) {}
