package com.alejandro.microservices.hr_api.application.service;

import com.alejandro.microservices.hr_api.application.dto.DepartmentDTO;
import com.alejandro.microservices.hr_api.domain.model.Department;
import com.alejandro.microservices.hr_api.domain.repository.DepartmentRepository;

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
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentService departmentService;

    private Department department;
    private UUID departmentId;

    @BeforeEach
    void setUp() {
        departmentId = UUID.randomUUID();
        department = new Department(departmentId, "IT");
    }

    @Test
    @DisplayName("Debería crear un departamento exitosamente")
    void shouldCreateDepartmentSuccessfully() {
        // Given
        String departmentName = "Recursos Humanos";
        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        // When
        DepartmentDTO result = departmentService.createDepartment(departmentName);

        // Then
        assertNotNull(result);
        assertEquals(departmentId, result.id());
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando el nombre del departamento es nulo")
    void shouldThrowExceptionWhenDepartmentNameIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> departmentService.createDepartment(null)
        );

        assertEquals("El nombre del departamento no puede estar vacío", exception.getMessage());
        verify(departmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando el nombre del departamento está vacío")
    void shouldThrowExceptionWhenDepartmentNameIsEmpty() {
        // When & Then
        assertAll(
                () -> {
                    IllegalArgumentException exception = assertThrows(
                            IllegalArgumentException.class,
                            () -> departmentService.createDepartment("")
                    );
                    assertEquals("El nombre del departamento no puede estar vacío", exception.getMessage());
                },
                () -> {
                    IllegalArgumentException exception = assertThrows(
                            IllegalArgumentException.class,
                            () -> departmentService.createDepartment("   ")
                    );
                    assertEquals("El nombre del departamento no puede estar vacío", exception.getMessage());
                }
        );
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando el nombre del departamento es muy corto")
    void shouldThrowExceptionWhenDepartmentNameIsTooShort() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> departmentService.createDepartment("A")
        );

        assertEquals("El nombre del departamento debe tener al menos 2 caracteres", exception.getMessage());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando el nombre del departamento es muy largo")
    void shouldThrowExceptionWhenDepartmentNameIsTooLong() {
        // Given
        String longName = "A".repeat(101);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> departmentService.createDepartment(longName)
        );

        assertEquals("El nombre del departamento no puede tener más de 100 caracteres", exception.getMessage());
    }

    @Test
    @DisplayName("Debería obtener todos los departamentos")
    void shouldGetAllDepartments() {
        // Given
        List<Department> departments = Arrays.asList(department);
        when(departmentRepository.findAll()).thenReturn(departments);

        // When
        List<DepartmentDTO> result = departmentService.getAllDepartments();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(departmentId, result.get(0).id());
        verify(departmentRepository).findAll();
    }

    @Test
    @DisplayName("Debería obtener departamento por ID")
    void shouldGetDepartmentById() {
        // Given
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));

        // When
        DepartmentDTO result = departmentService.getDepartmentById(departmentId);

        // Then
        assertNotNull(result);
        assertEquals(departmentId, result.id());
        assertEquals("IT", result.name());
        verify(departmentRepository).findById(departmentId);
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando el ID es nulo al buscar departamento")
    void shouldThrowExceptionWhenIdIsNullInGetDepartmentById() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> departmentService.getDepartmentById(null)
        );

        assertEquals("El ID del departamento no puede ser nulo", exception.getMessage());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando departamento no existe")
    void shouldThrowExceptionWhenDepartmentNotFound() {
        // Given
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> departmentService.getDepartmentById(departmentId)
        );

        assertEquals("Departamento no encontrado", exception.getMessage());
    }

    @Test
    @DisplayName("Debería actualizar departamento exitosamente")
    void shouldUpdateDepartmentSuccessfully() {
        // Given
        String newName = "Tecnología";
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        // When
        DepartmentDTO result = departmentService.updateDepartment(departmentId, newName);

        // Then
        assertNotNull(result);
        verify(departmentRepository).findById(departmentId);
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción al actualizar con ID nulo")
    void shouldThrowExceptionWhenUpdatingWithNullId() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> departmentService.updateDepartment(null, "Nuevo Nombre")
        );

        assertEquals("El ID del departamento no puede ser nulo", exception.getMessage());
    }

    @Test
    @DisplayName("Debería eliminar departamento exitosamente")
    void shouldDeleteDepartmentSuccessfully() {
        // Given
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));

        // When
        assertDoesNotThrow(() -> departmentService.deleteDepartment(departmentId));

        // Then
        verify(departmentRepository).findById(departmentId);
        verify(departmentRepository).deleteById(departmentId);
    }

    @Test
    @DisplayName("Debería lanzar excepción al eliminar con ID nulo")
    void shouldThrowExceptionWhenDeletingWithNullId() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> departmentService.deleteDepartment(null)
        );

        assertEquals("El ID del departamento no puede ser nulo", exception.getMessage());
    }

    @Test
    @DisplayName("Debería lanzar excepción al eliminar departamento que no existe")
    void shouldThrowExceptionWhenDeletingNonExistentDepartment() {
        // Given
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> departmentService.deleteDepartment(departmentId)
        );

        assertEquals("Departamento no encontrado", exception.getMessage());
    }

    @Test
    @DisplayName("Debería encontrar departamento por nombre")
    void shouldFindDepartmentByName() {
        // Given
        String departmentName = "IT";
        when(departmentRepository.findByName(departmentName)).thenReturn(Optional.of(department));

        // When
        DepartmentDTO result = departmentService.findByName(departmentName);

        // Then
        assertNotNull(result);
        assertEquals(departmentId, result.id());
        assertEquals("IT", result.name());
        verify(departmentRepository).findByName(departmentName);
    }

    @Test
    @DisplayName("Debería lanzar excepción al buscar por nombre nulo")
    void shouldThrowExceptionWhenFindingByNullName() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> departmentService.findByName(null)
        );

        assertEquals("El nombre del departamento no puede estar vacío", exception.getMessage());
    }

    @Test
    @DisplayName("Debería manejar nombres con espacios en blanco correctamente")
    void shouldHandleNamesWithWhitespaceCorrectly() {
        // Given
        String nameWithSpaces = "  IT Department  ";
        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        // When
        DepartmentDTO result = departmentService.createDepartment(nameWithSpaces);

        // Then
        assertNotNull(result);
        verify(departmentRepository).save(any(Department.class));
    }
}
