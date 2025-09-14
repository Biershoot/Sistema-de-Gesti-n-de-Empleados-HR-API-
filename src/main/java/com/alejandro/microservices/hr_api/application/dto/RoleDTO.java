package com.alejandro.microservices.hr_api.application.dto;

import java.util.UUID;

public record RoleDTO(
        UUID id,
        String name
) {}
