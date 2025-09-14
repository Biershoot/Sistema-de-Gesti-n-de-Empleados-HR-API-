package com.alejandro.microservices.hr_api.application.service;

import com.alejandro.microservices.hr_api.application.dto.EmployeeRequestDTO;
import com.alejandro.microservices.hr_api.application.dto.EmployeeResponseDTO;
import com.alejandro.microservices.hr_api.domain.model.Department;
import com.alejandro.microservices.hr_api.domain.model.Employee;
import com.alejandro.microservices.hr_api.domain.model.Role;
import com.alejandro.microservices.hr_api.domain.repository.DepartmentRepository;
import com.alejandro.microservices.hr_api.domain.repository.EmployeeRepository;
import com.alejandro.microservices.hr_api.domain.repository.RoleRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceValidationTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Department department;
    private Role role;
    private Employee employee;
    private UUID departmentId;
    private UUID roleId;
    private UUID employeeId;

    @BeforeEach
    void setUp() {
        departmentId = UUID.randomUUID();
        roleId = UUID.randomUUID();
        employeeId = UUID.randomUUID();

        department = new Department(departmentId, "IT");
        role = new Role(roleId, "Developer");

        employee = new Employee(
                employeeId,
                "Juan",
                "Pérez",
                "juan.perez@empresa.com",
                "hashedPassword123",
                department,
                role,
                LocalDate.of(2023, 1, 15),
                25
        );
    }

    // Pruebas de validación para createEmployee con EmployeeRequestDTO

    @Test
    @DisplayName("Debería lanzar excepción cuando EmployeeRequestDTO es nulo")
    void shouldThrowExceptionWhenEmployeeRequestDTOIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.createEmployee((EmployeeRequestDTO) null)
        );

        assertEquals("Los datos del empleado no pueden ser nulos", exception.getMessage());
        verify(employeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando firstName es nulo o vacío")
    void shouldThrowExceptionWhenFirstNameIsNullOrEmpty() {
        // Given
        EmployeeRequestDTO requestWithNullName = new EmployeeRequestDTO(
                null, "Pérez", "test@empresa.com", departmentId, roleId, LocalDate.now(), 15
        );

        EmployeeRequestDTO requestWithEmptyName = new EmployeeRequestDTO(
                "", "Pérez", "test@empresa.com", departmentId, roleId, LocalDate.now(), 15
        );

        EmployeeRequestDTO requestWithWhitespaceName = new EmployeeRequestDTO(
                "   ", "Pérez", "test@empresa.com", departmentId, roleId, LocalDate.now(), 15
        );

        // When & Then
        assertAll(
                () -> {
                    IllegalArgumentException exception = assertThrows(
                            IllegalArgumentException.class,
                            () -> employeeService.createEmployee(requestWithNullName)
                    );
                    assertEquals("El nombre no puede estar vacío", exception.getMessage());
                },
                () -> {
                    IllegalArgumentException exception = assertThrows(
                            IllegalArgumentException.class,
                            () -> employeeService.createEmployee(requestWithEmptyName)
                    );
                    assertEquals("El nombre no puede estar vacío", exception.getMessage());
                },
                () -> {
                    IllegalArgumentException exception = assertThrows(
                            IllegalArgumentException.class,
                            () -> employeeService.createEmployee(requestWithWhitespaceName)
                    );
                    assertEquals("El nombre no puede estar vacío", exception.getMessage());
                }
        );
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando lastName es nulo o vacío")
    void shouldThrowExceptionWhenLastNameIsNullOrEmpty() {
        // Given
        EmployeeRequestDTO requestWithNullLastName = new EmployeeRequestDTO(
                "Juan", null, "test@empresa.com", departmentId, roleId, LocalDate.now(), 15
        );

        EmployeeRequestDTO requestWithEmptyLastName = new EmployeeRequestDTO(
                "Juan", "", "test@empresa.com", departmentId, roleId, LocalDate.now(), 15
        );

        // When & Then
        assertAll(
                () -> {
                    IllegalArgumentException exception = assertThrows(
                            IllegalArgumentException.class,
                            () -> employeeService.createEmployee(requestWithNullLastName)
                    );
                    assertEquals("El apellido no puede estar vacío", exception.getMessage());
                },
                () -> {
                    IllegalArgumentException exception = assertThrows(
                            IllegalArgumentException.class,
                            () -> employeeService.createEmployee(requestWithEmptyLastName)
                    );
                    assertEquals("El apellido no puede estar vacío", exception.getMessage());
                }
        );
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando email es inválido")
    void shouldThrowExceptionWhenEmailIsInvalid() {
        // Given
        EmployeeRequestDTO requestWithNullEmail = new EmployeeRequestDTO(
                "Juan", "Pérez", null, departmentId, roleId, LocalDate.now(), 15
        );

        EmployeeRequestDTO requestWithEmptyEmail = new EmployeeRequestDTO(
                "Juan", "Pérez", "", departmentId, roleId, LocalDate.now(), 15
        );

        EmployeeRequestDTO requestWithInvalidEmail = new EmployeeRequestDTO(
                "Juan", "Pérez", "invalid-email", departmentId, roleId, LocalDate.now(), 15
        );

        // When & Then
        assertAll(
                () -> {
                    IllegalArgumentException exception = assertThrows(
                            IllegalArgumentException.class,
                            () -> employeeService.createEmployee(requestWithNullEmail)
                    );
                    assertEquals("El email no puede estar vacío", exception.getMessage());
                },
                () -> {
                    IllegalArgumentException exception = assertThrows(
                            IllegalArgumentException.class,
                            () -> employeeService.createEmployee(requestWithEmptyEmail)
                    );
                    assertEquals("El email no puede estar vacío", exception.getMessage());
                },
                () -> {
                    IllegalArgumentException exception = assertThrows(
                            IllegalArgumentException.class,
                            () -> employeeService.createEmployee(requestWithInvalidEmail)
                    );
                    assertEquals("El formato del email no es válido", exception.getMessage());
                }
        );
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando departmentId es nulo")
    void shouldThrowExceptionWhenDepartmentIdIsNull() {
        // Given
        EmployeeRequestDTO request = new EmployeeRequestDTO(
                "Juan", "Pérez", "juan@empresa.com", null, roleId, LocalDate.now(), 15
        );

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.createEmployee(request)
        );

        assertEquals("El ID del departamento no puede ser nulo", exception.getMessage());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando roleId es nulo")
    void shouldThrowExceptionWhenRoleIdIsNull() {
        // Given
        EmployeeRequestDTO request = new EmployeeRequestDTO(
                "Juan", "Pérez", "juan@empresa.com", departmentId, null, LocalDate.now(), 15
        );

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.createEmployee(request)
        );

        assertEquals("El ID del rol no puede ser nulo", exception.getMessage());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando fecha de contratación es futura")
    void shouldThrowExceptionWhenHireDateIsFuture() {
        // Given
        LocalDate futureDate = LocalDate.now().plusDays(1);
        EmployeeRequestDTO request = new EmployeeRequestDTO(
                "Juan", "Pérez", "juan@empresa.com", departmentId, roleId, futureDate, 15
        );

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.createEmployee(request)
        );

        assertEquals("La fecha de contratación no puede ser futura", exception.getMessage());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando días de vacaciones son negativos")
    void shouldThrowExceptionWhenVacationDaysAreNegative() {
        // Given
        EmployeeRequestDTO request = new EmployeeRequestDTO(
                "Juan", "Pérez", "juan@empresa.com", departmentId, roleId, LocalDate.now(), -5
        );

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.createEmployee(request)
        );

        assertEquals("Los días de vacaciones no pueden ser negativos", exception.getMessage());
    }

    @Test
    @DisplayName("Debería crear empleado con días de vacaciones 0 cuando son negativos en DTO")
    void shouldCreateEmployeeWithZeroVacationDaysWhenNegativeInDTO() {
        // Given
        EmployeeRequestDTO request = new EmployeeRequestDTO(
                "Juan", "Pérez", "juan@empresa.com", departmentId, roleId, LocalDate.now(), 0
        );

        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        // When
        EmployeeResponseDTO result = employeeService.createEmployee(request);

        // Then
        assertNotNull(result);
        verify(employeeRepository).save(any(Employee.class));
    }

    // Pruebas de validación para métodos con UUID

    @Test
    @DisplayName("Debería lanzar excepción cuando ID es nulo en getEmployeeById")
    void shouldThrowExceptionWhenIdIsNullInGetEmployeeById() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.getEmployeeById(null)
        );

        assertEquals("El ID del empleado no puede ser nulo", exception.getMessage());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando ID es nulo en getEmployeesByDepartment")
    void shouldThrowExceptionWhenIdIsNullInGetEmployeesByDepartment() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.getEmployeesByDepartment(null)
        );

        assertEquals("El ID del departamento no puede ser nulo", exception.getMessage());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando ID es nulo en getEmployeesByRole")
    void shouldThrowExceptionWhenIdIsNullInGetEmployeesByRole() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.getEmployeesByRole(null)
        );

        assertEquals("El ID del rol no puede ser nulo", exception.getMessage());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando ID es nulo en updateEmployee")
    void shouldThrowExceptionWhenIdIsNullInUpdateEmployee() {
        // Given
        EmployeeRequestDTO request = new EmployeeRequestDTO(
                "Juan", "Pérez", "juan@empresa.com", departmentId, roleId, LocalDate.now(), 15
        );

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.updateEmployee(null, request)
        );

        assertEquals("El ID del empleado no puede ser nulo", exception.getMessage());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando ID es nulo en deleteEmployee")
    void shouldThrowExceptionWhenIdIsNullInDeleteEmployee() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.deleteEmployee(null)
        );

        assertEquals("El ID del empleado no puede ser nulo", exception.getMessage());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando ID es nulo en takeVacation")
    void shouldThrowExceptionWhenIdIsNullInTakeVacation() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.takeVacation(null, 5)
        );

        assertEquals("El ID del empleado no puede ser nulo", exception.getMessage());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando días son cero o negativos en takeVacation")
    void shouldThrowExceptionWhenDaysAreZeroOrNegativeInTakeVacation() {
        // When & Then
        assertAll(
                () -> {
                    IllegalArgumentException exception = assertThrows(
                            IllegalArgumentException.class,
                            () -> employeeService.takeVacation(employeeId, 0)
                    );
                    assertEquals("Los días de vacaciones deben ser positivos", exception.getMessage());
                },
                () -> {
                    IllegalArgumentException exception = assertThrows(
                            IllegalArgumentException.class,
                            () -> employeeService.takeVacation(employeeId, -5)
                    );
                    assertEquals("Los días de vacaciones deben ser positivos", exception.getMessage());
                }
        );
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando ID es nulo en addVacationDays")
    void shouldThrowExceptionWhenIdIsNullInAddVacationDays() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.addVacationDays(null, 5)
        );

        assertEquals("El ID del empleado no puede ser nulo", exception.getMessage());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando días son cero o negativos en addVacationDays")
    void shouldThrowExceptionWhenDaysAreZeroOrNegativeInAddVacationDays() {
        // When & Then
        assertAll(
                () -> {
                    IllegalArgumentException exception = assertThrows(
                            IllegalArgumentException.class,
                            () -> employeeService.addVacationDays(employeeId, 0)
                    );
                    assertEquals("Los días a agregar deben ser positivos", exception.getMessage());
                },
                () -> {
                    IllegalArgumentException exception = assertThrows(
                            IllegalArgumentException.class,
                            () -> employeeService.addVacationDays(employeeId, -5)
                    );
                    assertEquals("Los días a agregar deben ser positivos", exception.getMessage());
                }
        );
    }

    // Pruebas de validación para createEmployee con parámetros básicos

    @Test
    @DisplayName("Debería lanzar excepción en createEmployee básico cuando parámetros son inválidos")
    void shouldThrowExceptionInBasicCreateEmployeeWhenParametersAreInvalid() {
        // When & Then
        assertAll(
                () -> {
                    IllegalArgumentException exception = assertThrows(
                            IllegalArgumentException.class,
                            () -> employeeService.createEmployee(null, "Pérez", "test@empresa.com", departmentId, roleId)
                    );
                    assertEquals("El nombre no puede estar vacío", exception.getMessage());
                },
                () -> {
                    IllegalArgumentException exception = assertThrows(
                            IllegalArgumentException.class,
                            () -> employeeService.createEmployee("Juan", null, "test@empresa.com", departmentId, roleId)
                    );
                    assertEquals("El apellido no puede estar vacío", exception.getMessage());
                },
                () -> {
                    IllegalArgumentException exception = assertThrows(
                            IllegalArgumentException.class,
                            () -> employeeService.createEmployee("Juan", "Pérez", "invalid-email", departmentId, roleId)
                    );
                    assertEquals("El formato del email no es válido", exception.getMessage());
                },
                () -> {
                    IllegalArgumentException exception = assertThrows(
                            IllegalArgumentException.class,
                            () -> employeeService.createEmployee("Juan", "Pérez", "test@empresa.com", null, roleId)
                    );
                    assertEquals("El ID del departamento no puede ser nulo", exception.getMessage());
                },
                () -> {
                    IllegalArgumentException exception = assertThrows(
                            IllegalArgumentException.class,
                            () -> employeeService.createEmployee("Juan", "Pérez", "test@empresa.com", departmentId, null)
                    );
                    assertEquals("El ID del rol no puede ser nulo", exception.getMessage());
                }
        );
    }

    // Prueba de validación de email

    @Test
    @DisplayName("Debería validar correctamente diferentes formatos de email")
    void shouldValidateEmailFormatsCorrectly() {
        // Emails válidos
        String[] validEmails = {
                "test@empresa.com",
                "usuario.nombre@dominio.org",
                "admin@test.net"
        };

        // Emails inválidos
        String[] invalidEmails = {
                "test",
                "test@",
                "@empresa.com",
                "test.com",
                "test@.com",
                "test@com"
        };

        for (String validEmail : validEmails) {
            EmployeeRequestDTO request = new EmployeeRequestDTO(
                    "Juan", "Pérez", validEmail, departmentId, roleId, LocalDate.now(), 15
            );

            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
            when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

            assertDoesNotThrow(() -> employeeService.createEmployee(request));
        }

        for (String invalidEmail : invalidEmails) {
            EmployeeRequestDTO request = new EmployeeRequestDTO(
                    "Juan", "Pérez", invalidEmail, departmentId, roleId, LocalDate.now(), 15
            );

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> employeeService.createEmployee(request)
            );
            assertEquals("El formato del email no es válido", exception.getMessage());
        }
    }

    @Test
    @DisplayName("Debería crear empleado exitosamente con datos válidos")
    void shouldCreateEmployeeSuccessfullyWithValidData() {
        // Given
        EmployeeRequestDTO request = new EmployeeRequestDTO(
                "Juan", "Pérez", "juan.perez@empresa.com", departmentId, roleId, LocalDate.now(), 15
        );

        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        // When
        EmployeeResponseDTO result = employeeService.createEmployee(request);

        // Then
        assertNotNull(result);
        verify(departmentRepository).findById(departmentId);
        verify(roleRepository).findById(roleId);
        verify(employeeRepository).save(any(Employee.class));
    }
}
