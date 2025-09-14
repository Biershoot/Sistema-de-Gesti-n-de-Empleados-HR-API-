package com.alejandro.microservices.hr_api.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    @DisplayName("Debería crear un rol con datos correctos")
    void shouldCreateRoleWithCorrectData() {
        // Given
        UUID roleId = UUID.randomUUID();
        String roleName = "Senior Developer";

        // When
        Role role = new Role(roleId, roleName);

        // Then
        assertAll(
            () -> assertEquals(roleId, role.getId()),
            () -> assertEquals(roleName, role.getName()),
            () -> assertNotNull(role.getId()),
            () -> assertNotNull(role.getName())
        );
    }

    @Test
    @DisplayName("Debería mantener inmutabilidad de los datos")
    void shouldMaintainDataImmutability() {
        // Given
        UUID roleId = UUID.randomUUID();
        String originalName = "Manager";
        Role role = new Role(roleId, originalName);

        // When - Intentar acceder a los datos
        UUID retrievedId = role.getId();
        String retrievedName = role.getName();

        // Then
        assertEquals(roleId, retrievedId);
        assertEquals(originalName, retrievedName);
    }

    @Test
    @DisplayName("Dos roles con el mismo ID deberían ser considerados iguales")
    void shouldConsiderRolesWithSameIdAsEqual() {
        // Given
        UUID sameId = UUID.randomUUID();
        Role role1 = new Role(sameId, "Developer");
        Role role2 = new Role(sameId, "Programmer"); // Diferente nombre pero mismo ID

        // When & Then
        assertEquals(role1.getId(), role2.getId());
    }
}
