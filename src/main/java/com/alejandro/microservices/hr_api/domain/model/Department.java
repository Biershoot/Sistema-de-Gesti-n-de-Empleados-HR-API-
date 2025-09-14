package com.alejandro.microservices.hr_api.domain.model;

import java.util.UUID;

public class Department {

    private UUID id;
    private String name;

    public Department(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
