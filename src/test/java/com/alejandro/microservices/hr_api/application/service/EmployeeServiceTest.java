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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

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
    private EmployeeRequestDTO employeeRequestDTO;
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
                department,
                role,
                LocalDate.of(2023, 1, 15),
                25
        );

        employeeRequestDTO = new EmployeeRequestDTO(
                "Juan",
                "Pérez",
                "juan.perez@empresa.com",
                departmentId,
                roleId,
                LocalDate.of(2023, 1, 15),
                25
        );
    }

    @Test
    @DisplayName("Debería crear un empleado exitosamente")
    void shouldCreateEmployeeSuccessfully() {
        // Given
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        // When
        EmployeeResponseDTO result = employeeService.createEmployee(employeeRequestDTO);

        // Then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("Juan", result.firstName()),
                () -> assertEquals("Pérez", result.lastName()),
                () -> assertEquals("juan.perez@empresa.com", result.email()),
                () -> assertEquals("IT", result.department().name()),
                () -> assertEquals("Developer", result.role().name()),
                () -> assertEquals(25, result.vacationDays())
        );

        verify(departmentRepository).findById(departmentId);
        verify(roleRepository).findById(roleId);
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando el departamento no existe")
    void shouldThrowExceptionWhenDepartmentNotFound() {
        // Given
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.createEmployee(employeeRequestDTO)
        );

        assertEquals("Departamento no encontrado", exception.getMessage());
        verify(departmentRepository).findById(departmentId);
        verify(roleRepository, never()).findById(any());
        verify(employeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando el rol no existe")
    void shouldThrowExceptionWhenRoleNotFound() {
        // Given
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.createEmployee(employeeRequestDTO)
        );

        assertEquals("Rol no encontrado", exception.getMessage());
        verify(departmentRepository).findById(departmentId);
        verify(roleRepository).findById(roleId);
        verify(employeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería obtener todos los empleados")
    void shouldGetAllEmployees() {
        // Given
        List<Employee> employees = Arrays.asList(employee);
        when(employeeRepository.findAll()).thenReturn(employees);

        // When
        List<EmployeeResponseDTO> result = employeeService.getAllEmployees();

        // Then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(1, result.size()),
                () -> assertEquals("Juan", result.get(0).firstName()),
                () -> assertEquals("IT", result.get(0).department().name())
        );

        verify(employeeRepository).findAll();
    }

    @Test
    @DisplayName("Debería obtener empleado por ID")
    void shouldGetEmployeeById() {
        // Given
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        // When
        EmployeeResponseDTO result = employeeService.getEmployeeById(employeeId);

        // Then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(employeeId, result.id()),
                () -> assertEquals("Juan", result.firstName())
        );

        verify(employeeRepository).findById(employeeId);
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando empleado no existe al buscar por ID")
    void shouldThrowExceptionWhenEmployeeNotFoundById() {
        // Given
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.getEmployeeById(employeeId)
        );

        assertEquals("Empleado no encontrado", exception.getMessage());
        verify(employeeRepository).findById(employeeId);
    }

    @Test
    @DisplayName("Debería obtener empleados por departamento")
    void shouldGetEmployeesByDepartment() {
        // Given
        List<Employee> employees = Arrays.asList(employee);
        when(employeeRepository.findByDepartmentId(departmentId)).thenReturn(employees);

        // When
        List<EmployeeResponseDTO> result = employeeService.getEmployeesByDepartment(departmentId);

        // Then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(1, result.size()),
                () -> assertEquals(departmentId, result.get(0).department().id())
        );

        verify(employeeRepository).findByDepartmentId(departmentId);
    }

    @Test
    @DisplayName("Debería tomar días de vacaciones exitosamente")
    void shouldTakeVacationSuccessfully() {
        // Given
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        // When
        EmployeeResponseDTO result = employeeService.takeVacation(employeeId, 5);

        // Then
        assertNotNull(result);
        verify(employeeRepository).findById(employeeId);
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    @DisplayName("Debería agregar días de vacaciones exitosamente")
    void shouldAddVacationDaysSuccessfully() {
        // Given
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        // When
        EmployeeResponseDTO result = employeeService.addVacationDays(employeeId, 10);

        // Then
        assertNotNull(result);
        verify(employeeRepository).findById(employeeId);
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    @DisplayName("Debería eliminar empleado exitosamente")
    void shouldDeleteEmployeeSuccessfully() {
        // Given
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        // When
        assertDoesNotThrow(() -> employeeService.deleteEmployee(employeeId));

        // Then
        verify(employeeRepository).findById(employeeId);
        verify(employeeRepository).deleteById(employeeId);
    }

    @Test
    @DisplayName("Debería lanzar excepción al intentar eliminar empleado que no existe")
    void shouldThrowExceptionWhenDeletingNonExistentEmployee() {
        // Given
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.deleteEmployee(employeeId)
        );

        assertEquals("Empleado no encontrado", exception.getMessage());
        verify(employeeRepository).findById(employeeId);
        verify(employeeRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Debería actualizar empleado exitosamente")
    void shouldUpdateEmployeeSuccessfully() {
        // Given
        EmployeeRequestDTO updateRequest = new EmployeeRequestDTO(
                "Juan Carlos",
                "Pérez López",
                "juan.carlos@empresa.com",
                departmentId,
                roleId,
                LocalDate.of(2023, 1, 15),
                30
        );

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        // When
        EmployeeResponseDTO result = employeeService.updateEmployee(employeeId, updateRequest);

        // Then
        assertNotNull(result);
        verify(employeeRepository).findById(employeeId);
        verify(departmentRepository).findById(departmentId);
        verify(roleRepository).findById(roleId);
        verify(employeeRepository).save(any(Employee.class));
    }
}
