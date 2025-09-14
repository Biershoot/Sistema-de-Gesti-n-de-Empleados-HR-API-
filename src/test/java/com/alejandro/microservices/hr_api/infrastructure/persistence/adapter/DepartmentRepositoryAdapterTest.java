package com.alejandro.microservices.hr_api.infrastructure.persistence.adapter;

import com.alejandro.microservices.hr_api.domain.model.Department;
import com.alejandro.microservices.hr_api.infrastructure.persistence.entity.DepartmentEntity;
import com.alejandro.microservices.hr_api.infrastructure.persistence.repository.JpaDepartmentRepository;
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
class DepartmentRepositoryAdapterTest {

    @Mock
    private JpaDepartmentRepository jpaRepository;

    @InjectMocks
    private DepartmentRepositoryAdapter departmentRepositoryAdapter;

    private Department department;
    private DepartmentEntity departmentEntity;
    private UUID departmentId;

    @BeforeEach
    void setUp() {
        departmentId = UUID.randomUUID();
        department = new Department(departmentId, "IT");
        departmentEntity = new DepartmentEntity(departmentId, "IT");
    }

    @Test
    void save_ShouldReturnSavedDepartment() {
        // Given
        when(jpaRepository.save(any(DepartmentEntity.class))).thenReturn(departmentEntity);

        // When
        Department result = departmentRepositoryAdapter.save(department);

        // Then
        assertNotNull(result);
        assertEquals(department.getId(), result.getId());
        assertEquals(department.getName(), result.getName());
        verify(jpaRepository).save(any(DepartmentEntity.class));
    }

    @Test
    void findById_WhenDepartmentExists_ShouldReturnDepartment() {
        // Given
        when(jpaRepository.findById(departmentId)).thenReturn(Optional.of(departmentEntity));

        // When
        Optional<Department> result = departmentRepositoryAdapter.findById(departmentId);

        // Then
        assertTrue(result.isPresent());
        Department foundDepartment = result.get();
        assertEquals(departmentId, foundDepartment.getId());
        assertEquals("IT", foundDepartment.getName());
        verify(jpaRepository).findById(departmentId);
    }

    @Test
    void findById_WhenDepartmentNotExists_ShouldReturnEmpty() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(jpaRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        Optional<Department> result = departmentRepositoryAdapter.findById(nonExistentId);

        // Then
        assertFalse(result.isPresent());
        verify(jpaRepository).findById(nonExistentId);
    }

    @Test
    void findAll_ShouldReturnAllDepartments() {
        // Given
        DepartmentEntity secondDepartmentEntity = new DepartmentEntity(UUID.randomUUID(), "HR");
        List<DepartmentEntity> departmentEntities = Arrays.asList(departmentEntity, secondDepartmentEntity);
        when(jpaRepository.findAll()).thenReturn(departmentEntities);

        // When
        List<Department> result = departmentRepositoryAdapter.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("IT", result.get(0).getName());
        assertEquals("HR", result.get(1).getName());
        verify(jpaRepository).findAll();
    }

    @Test
    void deleteById_ShouldCallJpaRepositoryDelete() {
        // When
        departmentRepositoryAdapter.deleteById(departmentId);

        // Then
        verify(jpaRepository).deleteById(departmentId);
    }

    @Test
    void mapToEntity_ShouldCorrectlyMapDepartmentToEntity() {
        // Given
        when(jpaRepository.save(any(DepartmentEntity.class))).thenReturn(departmentEntity);

        // When
        departmentRepositoryAdapter.save(department);

        // Then
        verify(jpaRepository).save(argThat(entity ->
                entity.getId().equals(department.getId()) &&
                entity.getName().equals(department.getName())
        ));
    }

    @Test
    void mapToDomain_ShouldCorrectlyMapEntityToDepartment() {
        // Given
        when(jpaRepository.findById(departmentId)).thenReturn(Optional.of(departmentEntity));

        // When
        Optional<Department> result = departmentRepositoryAdapter.findById(departmentId);

        // Then
        assertTrue(result.isPresent());
        Department mappedDepartment = result.get();
        assertEquals(departmentEntity.getId(), mappedDepartment.getId());
        assertEquals(departmentEntity.getName(), mappedDepartment.getName());
    }
}
