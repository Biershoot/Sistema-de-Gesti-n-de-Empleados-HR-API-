package com.alejandro.microservices.hr_api.application.dto;

import com.alejandro.microservices.hr_api.domain.model.LeaveType;

import java.time.LocalDate;
import java.util.UUID;

public record LeaveResponseDTO(
        UUID id,
        UUID employeeId,
        LocalDate startDate,
        LocalDate endDate,
        LeaveType type
) {}
