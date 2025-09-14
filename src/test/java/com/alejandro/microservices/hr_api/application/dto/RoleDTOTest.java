package com.alejandro.microservices.hr_api.application.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RoleDTOTest {

    @Test
    @DisplayName("Debería crear RoleDTO con datos correctos")
    void shouldCreateRoleDTOWithCorrectData() {
        // Given
        UUID roleId = UUID.randomUUID();
        String roleName = "Senior Developer";

        // When
        RoleDTO roleDTO = new RoleDTO(roleId, roleName);

        // Then
        assertAll(
            () -> assertEquals(roleId, roleDTO.id()),
            () -> assertEquals(roleName, roleDTO.name()),
            () -> assertNotNull(roleDTO.id()),
            () -> assertNotNull(roleDTO.name())
        );
    }

    @Test
    @DisplayName("Dos RoleDTOs con los mismos datos deberían ser iguales")
    void shouldBeEqualWhenSameData() {
        // Given
        UUID id = UUID.randomUUID();
        String name = "Manager";

        RoleDTO dto1 = new RoleDTO(id, name);
        RoleDTO dto2 = new RoleDTO(id, name);

        // When & Then
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    @DisplayName("RoleDTOs con datos diferentes no deberían ser iguales")
    void shouldNotBeEqualWhenDifferentData() {
        // Given
        RoleDTO dto1 = new RoleDTO(UUID.randomUUID(), "Developer");
        RoleDTO dto2 = new RoleDTO(UUID.randomUUID(), "Manager");

        // When & Then
        assertNotEquals(dto1, dto2);
    }
}
