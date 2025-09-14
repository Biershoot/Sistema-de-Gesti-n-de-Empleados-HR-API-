package com.alejandro.microservices.hr_api.application.dto;

import com.alejandro.microservices.hr_api.domain.model.LeaveType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record LeaveRequestDTO(
        @NotNull(message = "El ID del empleado no puede ser nulo")
        UUID employeeId,

        @NotNull(message = "La fecha de inicio no puede ser nula")
        LocalDate startDate,

        @NotNull(message = "La fecha de fin no puede ser nula")
        LocalDate endDate,

        @NotNull(message = "El tipo de permiso no puede ser nulo")
        LeaveType type
) {}
