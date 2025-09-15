package com.alejandro.microservices.hr_api.application.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.UUID;

public record EmployeeRequestDTO(
        @NotBlank(message = "El nombre no puede estar vacío")
        String firstName,

        @NotBlank(message = "El apellido no puede estar vacío")
        String lastName,

        @NotBlank(message = "El email no puede estar vacío")
        @Email(message = "El formato del email no es válido")
        String email,

        @NotNull(message = "El ID del departamento no puede ser nulo")
        UUID departmentId,

        @NotNull(message = "El ID del rol no puede ser nulo")
        UUID roleId,

        LocalDate hireDate,

        @Min(value = 0, message = "Los días de vacaciones no pueden ser negativos")
        int vacationDays
) {}
