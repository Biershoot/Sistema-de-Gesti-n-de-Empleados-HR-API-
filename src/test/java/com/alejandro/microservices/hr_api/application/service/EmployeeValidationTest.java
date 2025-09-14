package com.alejandro.microservices.hr_api.application.service;

import com.alejandro.microservices.hr_api.application.dto.EmployeeRequestDTO;
import com.alejandro.microservices.hr_api.domain.model.Department;
import com.alejandro.microservices.hr_api.domain.model.Employee;
import com.alejandro.microservices.hr_api.domain.model.Role;
import com.alejandro.microservices.hr_api.domain.repository.DepartmentRepository;
import com.alejandro.microservices.hr_api.domain.repository.EmployeeRepository;
import com.alejandro.microservices.hr_api.domain.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para las validaciones de negocio del EmployeeService.
 *
 * Estas pruebas verifican que las reglas de negocio se apliquen correctamente:
 * - Email único
 * - Departamento y rol existentes
 * - Validaciones de datos de entrada
 */
class EmployeeValidationTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private RoleRepository roleRepository;

    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        employeeService = new EmployeeService(employeeRepository, departmentRepository, roleRepository);
    }

    @Test
    void createEmployee_shouldThrowException_whenEmailAlreadyExists() {
        // Arrange
        String existingEmail = "john.doe@company.com";
        UUID departmentId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        EmployeeRequestDTO request = new EmployeeRequestDTO(
            "John", "Doe", existingEmail, departmentId, roleId, LocalDate.now(), 15
        );

        // Simular que ya existe un empleado con ese email
        Employee existingEmployee = buildEmployee(UUID.randomUUID(), existingEmail);
        when(employeeRepository.findByEmail(existingEmail)).thenReturn(Optional.of(existingEmployee));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> employeeService.createEmployee(request)
        );

        assertEquals("El correo ya está registrado para otro empleado", exception.getMessage());
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void createEmployee_shouldThrowException_whenDepartmentNotExists() {
        // Arrange
        String email = "new.employee@company.com";
        UUID departmentId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        EmployeeRequestDTO request = new EmployeeRequestDTO(
            "New", "Employee", email, departmentId, roleId, LocalDate.now(), 15
        );

        when(employeeRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> employeeService.createEmployee(request)
        );

        assertEquals("Departamento no encontrado", exception.getMessage());
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void createEmployee_shouldThrowException_whenRoleNotExists() {
        // Arrange
        String email = "new.employee@company.com";
        UUID departmentId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        EmployeeRequestDTO request = new EmployeeRequestDTO(
            "New", "Employee", email, departmentId, roleId, LocalDate.now(), 15
        );

        when(employeeRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(buildDepartment()));
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> employeeService.createEmployee(request)
        );

        assertEquals("Rol no encontrado", exception.getMessage());
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void createEmployee_shouldThrowException_whenEmailFormatInvalid() {
        // Arrange
        String invalidEmail = "invalid-email";
        UUID departmentId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        EmployeeRequestDTO request = new EmployeeRequestDTO(
            "John", "Doe", invalidEmail, departmentId, roleId, LocalDate.now(), 15
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> employeeService.createEmployee(request)
        );

        assertEquals("El formato del email no es válido", exception.getMessage());
    }

    @Test
    void updateEmployee_shouldAllowSameEmailForSameEmployee() {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        String email = "john.doe@company.com";
        UUID departmentId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        EmployeeRequestDTO request = new EmployeeRequestDTO(
            "John", "Doe", email, departmentId, roleId, LocalDate.now(), 15
        );

        Employee existingEmployee = buildEmployee(employeeId, email);
        Department department = buildDepartment();
        Role role = buildRole();

        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(existingEmployee));
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(employeeRepository.save(any())).thenReturn(existingEmployee);

        // Act & Assert - No debería lanzar excepción
        assertDoesNotThrow(() -> employeeService.updateEmployee(employeeId, request));
        verify(employeeRepository).save(any());
    }

    @Test
    void updateEmployee_shouldThrowException_whenEmailExistsForDifferentEmployee() {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        UUID otherEmployeeId = UUID.randomUUID();
        String email = "john.doe@company.com";
        UUID departmentId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        EmployeeRequestDTO request = new EmployeeRequestDTO(
            "John", "Doe", email, departmentId, roleId, LocalDate.now(), 15
        );

        Employee currentEmployee = buildEmployee(employeeId, "current@company.com");
        Employee otherEmployee = buildEmployee(otherEmployeeId, email);

        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(otherEmployee));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(currentEmployee));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> employeeService.updateEmployee(employeeId, request)
        );

        assertEquals("El correo ya está registrado para otro empleado", exception.getMessage());
        verify(employeeRepository, never()).save(any());
    }

    // Métodos auxiliares para construir objetos de prueba
    private Employee buildEmployee(UUID id, String email) {
        Department dept = new Department(UUID.randomUUID(), "IT");
        Role role = new Role(UUID.randomUUID(), "Developer");
        return new Employee(id, "John", "Doe", email, dept, role, LocalDate.now(), 15);
    }

    private Department buildDepartment() {
        return new Department(UUID.randomUUID(), "IT Department");
    }

    private Role buildRole() {
        return new Role(UUID.randomUUID(), "Software Developer");
    }
}
