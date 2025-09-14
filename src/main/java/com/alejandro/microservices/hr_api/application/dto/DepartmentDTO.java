package com.alejandro.microservices.hr_api.application.dto;

import java.util.UUID;

public record DepartmentDTO(
        UUID id,
        String name
) {}
