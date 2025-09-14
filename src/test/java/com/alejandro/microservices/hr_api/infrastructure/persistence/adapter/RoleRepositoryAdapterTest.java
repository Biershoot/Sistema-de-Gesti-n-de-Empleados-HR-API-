package com.alejandro.microservices.hr_api.infrastructure.persistence.adapter;

import com.alejandro.microservices.hr_api.domain.model.Role;
import com.alejandro.microservices.hr_api.infrastructure.persistence.entity.RoleEntity;
import com.alejandro.microservices.hr_api.infrastructure.persistence.repository.JpaRoleRepository;
import org.junit.jupiter.api.BeforeEach;
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
class RoleRepositoryAdapterTest {

    @Mock
    private JpaRoleRepository jpaRepository;

    @InjectMocks
    private RoleRepositoryAdapter roleRepositoryAdapter;

    private Role role;
    private RoleEntity roleEntity;
    private UUID roleId;

    @BeforeEach
    void setUp() {
        roleId = UUID.randomUUID();
        role = new Role(roleId, "Developer");
        roleEntity = new RoleEntity(roleId, "Developer");
    }

    @Test
    void save_ShouldReturnSavedRole() {
        // Given
        when(jpaRepository.save(any(RoleEntity.class))).thenReturn(roleEntity);

        // When
        Role result = roleRepositoryAdapter.save(role);

        // Then
        assertNotNull(result);
        assertEquals(role.getId(), result.getId());
        assertEquals(role.getName(), result.getName());
        verify(jpaRepository).save(any(RoleEntity.class));
    }

    @Test
    void findById_WhenRoleExists_ShouldReturnRole() {
        // Given
        when(jpaRepository.findById(roleId)).thenReturn(Optional.of(roleEntity));

        // When
        Optional<Role> result = roleRepositoryAdapter.findById(roleId);

        // Then
        assertTrue(result.isPresent());
        Role foundRole = result.get();
        assertEquals(roleId, foundRole.getId());
        assertEquals("Developer", foundRole.getName());
        verify(jpaRepository).findById(roleId);
    }

    @Test
    void findById_WhenRoleNotExists_ShouldReturnEmpty() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(jpaRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        Optional<Role> result = roleRepositoryAdapter.findById(nonExistentId);

        // Then
        assertFalse(result.isPresent());
        verify(jpaRepository).findById(nonExistentId);
    }

    @Test
    void findAll_ShouldReturnAllRoles() {
        // Given
        RoleEntity secondRoleEntity = new RoleEntity(UUID.randomUUID(), "Manager");
        List<RoleEntity> roleEntities = Arrays.asList(roleEntity, secondRoleEntity);
        when(jpaRepository.findAll()).thenReturn(roleEntities);

        // When
        List<Role> result = roleRepositoryAdapter.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Developer", result.get(0).getName());
        assertEquals("Manager", result.get(1).getName());
        verify(jpaRepository).findAll();
    }

    @Test
    void deleteById_ShouldCallJpaRepositoryDelete() {
        // When
        roleRepositoryAdapter.deleteById(roleId);

        // Then
        verify(jpaRepository).deleteById(roleId);
    }

    @Test
    void mapToEntity_ShouldCorrectlyMapRoleToEntity() {
        // Given
        when(jpaRepository.save(any(RoleEntity.class))).thenReturn(roleEntity);

        // When
        roleRepositoryAdapter.save(role);

        // Then
        verify(jpaRepository).save(argThat(entity ->
                entity.getId().equals(role.getId()) &&
                entity.getName().equals(role.getName())
        ));
    }

    @Test
    void mapToDomain_ShouldCorrectlyMapEntityToRole() {
        // Given
        when(jpaRepository.findById(roleId)).thenReturn(Optional.of(roleEntity));

        // When
        Optional<Role> result = roleRepositoryAdapter.findById(roleId);

        // Then
        assertTrue(result.isPresent());
        Role mappedRole = result.get();
        assertEquals(roleEntity.getId(), mappedRole.getId());
        assertEquals(roleEntity.getName(), mappedRole.getName());
    }
}
