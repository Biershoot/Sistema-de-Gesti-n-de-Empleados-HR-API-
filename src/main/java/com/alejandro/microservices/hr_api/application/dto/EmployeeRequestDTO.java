package com.alejandro.microservices.hr_api.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "DTO para crear o actualizar un empleado")
public record EmployeeRequestDTO(
        @Schema(description = "Nombre del empleado", example = "Juan", required = true)
        @NotBlank(message = "El nombre no puede estar vacío")
        String firstName,

        @Schema(description = "Apellido del empleado", example = "Pérez", required = true)
        @NotBlank(message = "El apellido no puede estar vacío")
        String lastName,

        @Schema(description = "Email del empleado", example = "juan.perez@company.com", required = true)
        @NotBlank(message = "El email no puede estar vacío")
        @Email(message = "El formato del email no es válido")
        String email,

        @Schema(description = "ID del departamento al que pertenece el empleado", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
        @NotNull(message = "El ID del departamento no puede ser nulo")
        UUID departmentId,

        @Schema(description = "ID del rol del empleado", example = "650e8400-e29b-41d4-a716-446655440000", required = true)
        @NotNull(message = "El ID del rol no puede ser nulo")
        UUID roleId,

        @Schema(description = "Fecha de contratación del empleado", example = "2024-01-15", required = true)
        LocalDate hireDate,

        @Schema(description = "Días de vacaciones disponibles", example = "20", minimum = "0", maximum = "365")
        @Min(value = 0, message = "Los días de vacaciones no pueden ser negativos")
        int vacationDays
) {}
