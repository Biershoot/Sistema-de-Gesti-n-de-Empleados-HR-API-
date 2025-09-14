package com.alejandro.microservices.hr_api.application.service;

import com.alejandro.microservices.hr_api.application.dto.RoleDTO;
import com.alejandro.microservices.hr_api.domain.model.Role;
import com.alejandro.microservices.hr_api.domain.repository.RoleRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    private Role role;
    private UUID roleId;

    @BeforeEach
    void setUp() {
        roleId = UUID.randomUUID();
        role = new Role(roleId, "Developer");
    }

    @Test
    @DisplayName("Debería crear un rol exitosamente")
    void shouldCreateRoleSuccessfully() {
        // Given
        String roleName = "Senior Developer";
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        // When
        RoleDTO result = roleService.createRole(roleName);

        // Then
        assertNotNull(result);
        assertEquals(roleId, result.id());
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando el nombre del rol es nulo")
    void shouldThrowExceptionWhenRoleNameIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> roleService.createRole(null)
        );

        assertEquals("El nombre del rol no puede estar vacío", exception.getMessage());
        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando el nombre del rol está vacío")
    void shouldThrowExceptionWhenRoleNameIsEmpty() {
        // When & Then
        assertAll(
                () -> {
                    IllegalArgumentException exception = assertThrows(
                            IllegalArgumentException.class,
                            () -> roleService.createRole("")
                    );
                    assertEquals("El nombre del rol no puede estar vacío", exception.getMessage());
                },
                () -> {
                    IllegalArgumentException exception = assertThrows(
                            IllegalArgumentException.class,
                            () -> roleService.createRole("   ")
                    );
                    assertEquals("El nombre del rol no puede estar vacío", exception.getMessage());
                }
        );
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando el nombre del rol es muy corto")
    void shouldThrowExceptionWhenRoleNameIsTooShort() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> roleService.createRole("A")
        );

        assertEquals("El nombre del rol debe tener al menos 2 caracteres", exception.getMessage());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando el nombre del rol es muy largo")
    void shouldThrowExceptionWhenRoleNameIsTooLong() {
        // Given
        String longName = "A".repeat(101);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> roleService.createRole(longName)
        );

        assertEquals("El nombre del rol no puede tener más de 100 caracteres", exception.getMessage());
    }

    @Test
    @DisplayName("Debería obtener todos los roles")
    void shouldGetAllRoles() {
        // Given
        List<Role> roles = Arrays.asList(role);
        when(roleRepository.findAll()).thenReturn(roles);

        // When
        List<RoleDTO> result = roleService.getAllRoles();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(roleId, result.get(0).id());
        verify(roleRepository).findAll();
    }

    @Test
    @DisplayName("Debería obtener rol por ID")
    void shouldGetRoleById() {
        // Given
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        // When
        RoleDTO result = roleService.getRoleById(roleId);

        // Then
        assertNotNull(result);
        assertEquals(roleId, result.id());
        assertEquals("Developer", result.name());
        verify(roleRepository).findById(roleId);
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando el ID es nulo al buscar rol")
    void shouldThrowExceptionWhenIdIsNullInGetRoleById() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> roleService.getRoleById(null)
        );

        assertEquals("El ID del rol no puede ser nulo", exception.getMessage());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando rol no existe")
    void shouldThrowExceptionWhenRoleNotFound() {
        // Given
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> roleService.getRoleById(roleId)
        );

        assertEquals("Rol no encontrado", exception.getMessage());
    }

    @Test
    @DisplayName("Debería actualizar rol exitosamente")
    void shouldUpdateRoleSuccessfully() {
        // Given
        String newName = "Senior Developer";
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        // When
        RoleDTO result = roleService.updateRole(roleId, newName);

        // Then
        assertNotNull(result);
        verify(roleRepository).findById(roleId);
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción al actualizar con ID nulo")
    void shouldThrowExceptionWhenUpdatingWithNullId() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> roleService.updateRole(null, "Nuevo Nombre")
        );

        assertEquals("El ID del rol no puede ser nulo", exception.getMessage());
    }

    @Test
    @DisplayName("Debería eliminar rol exitosamente")
    void shouldDeleteRoleSuccessfully() {
        // Given
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        // When
        assertDoesNotThrow(() -> roleService.deleteRole(roleId));

        // Then
        verify(roleRepository).findById(roleId);
        verify(roleRepository).deleteById(roleId);
    }

    @Test
    @DisplayName("Debería lanzar excepción al eliminar con ID nulo")
    void shouldThrowExceptionWhenDeletingWithNullId() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> roleService.deleteRole(null)
        );

        assertEquals("El ID del rol no puede ser nulo", exception.getMessage());
    }

    @Test
    @DisplayName("Debería lanzar excepción al eliminar rol que no existe")
    void shouldThrowExceptionWhenDeletingNonExistentRole() {
        // Given
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> roleService.deleteRole(roleId)
        );

        assertEquals("Rol no encontrado", exception.getMessage());
    }

    @Test
    @DisplayName("Debería encontrar rol por nombre")
    void shouldFindRoleByName() {
        // Given
        String roleName = "Developer";
        when(roleRepository.findByName(roleName)).thenReturn(Optional.of(role));

        // When
        RoleDTO result = roleService.findByName(roleName);

        // Then
        assertNotNull(result);
        assertEquals(roleId, result.id());
        assertEquals("Developer", result.name());
        verify(roleRepository).findByName(roleName);
    }

    @Test
    @DisplayName("Debería lanzar excepción al buscar por nombre nulo")
    void shouldThrowExceptionWhenFindingByNullName() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> roleService.findByName(null)
        );

        assertEquals("El nombre del rol no puede estar vacío", exception.getMessage());
    }

    @Test
    @DisplayName("Debería manejar nombres con espacios en blanco correctamente")
    void shouldHandleNamesWithWhitespaceCorrectly() {
        // Given
        String nameWithSpaces = "  Senior Developer  ";
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        // When
        RoleDTO result = roleService.createRole(nameWithSpaces);

        // Then
        assertNotNull(result);
        verify(roleRepository).save(any(Role.class));
    }
}
