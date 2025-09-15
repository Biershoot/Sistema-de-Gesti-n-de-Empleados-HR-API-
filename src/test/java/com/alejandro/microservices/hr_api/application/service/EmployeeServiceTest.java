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
import org.junit.jupiter.api.Nested;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias completas para el EmployeeService.
 *
 * Esta clase de pruebas verifica todos los casos de uso del servicio de empleados,
 * incluyendo operaciones CRUD, validaciones de negocio y manejo de errores.
 *
 * @author Sistema HR API
 * @version 1.0
 * @since 2025-01-14
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeService - Pruebas Unitarias")
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
        // Configurar datos de prueba
        departmentId = UUID.randomUUID();
        roleId = UUID.randomUUID();
        employeeId = UUID.randomUUID();

        department = new Department(departmentId, "Recursos Humanos");
        role = new Role(roleId, "ADMIN");
        
        employee = new Employee(
                employeeId,
                "Juan",
                "Pérez",
                "juan.perez@empresa.com",
                "password123",
                department,
                role,
                LocalDate.now(),
                20
        );

        employeeRequestDTO = new EmployeeRequestDTO(
                "Juan",
                "Pérez",
                "juan.perez@empresa.com",
                departmentId,
                roleId,
                LocalDate.now(),
                20
        );
    }

    @Nested
    @DisplayName("Creación de Empleados")
    class CreateEmployeeTests {

        @Test
        @DisplayName("Debería crear un empleado exitosamente cuando todos los datos son válidos")
        void shouldCreateEmployeeSuccessfullyWhenAllDataIsValid() {
            // Given
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
            when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

            // When
            EmployeeResponseDTO result = employeeService.createEmployee(employeeRequestDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(employeeId);
            assertThat(result.firstName()).isEqualTo("Juan");
            assertThat(result.lastName()).isEqualTo("Pérez");
            assertThat(result.email()).isEqualTo("juan.perez@empresa.com");
            assertThat(result.vacationDays()).isEqualTo(20);

            verify(departmentRepository).findById(departmentId);
            verify(roleRepository).findById(roleId);
            verify(employeeRepository).findByEmail("juan.perez@empresa.com");
            verify(employeeRepository).save(any(Employee.class));
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando el departamento no existe")
        void shouldThrowExceptionWhenDepartmentDoesNotExist() {
            // Given
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> employeeService.createEmployee(employeeRequestDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Departamento no encontrado");

            verify(departmentRepository).findById(departmentId);
            verify(roleRepository, never()).findById(any());
            verify(employeeRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando el rol no existe")
        void shouldThrowExceptionWhenRoleDoesNotExist() {
            // Given
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> employeeService.createEmployee(employeeRequestDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Rol no encontrado");

            verify(departmentRepository).findById(departmentId);
            verify(roleRepository).findById(roleId);
            verify(employeeRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando el email ya existe")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            // Given
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
            when(employeeRepository.findByEmail("juan.perez@empresa.com")).thenReturn(Optional.of(employee));

            // When & Then
            assertThatThrownBy(() -> employeeService.createEmployee(employeeRequestDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Ya existe un empleado con este email");

            verify(departmentRepository).findById(departmentId);
            verify(roleRepository).findById(roleId);
            verify(employeeRepository).findByEmail("juan.perez@empresa.com");
            verify(employeeRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Consulta de Empleados")
    class GetEmployeeTests {

        @Test
        @DisplayName("Debería obtener todos los empleados exitosamente")
        void shouldGetAllEmployeesSuccessfully() {
            // Given
            List<Employee> employees = Arrays.asList(employee);
            when(employeeRepository.findAll()).thenReturn(employees);
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

            // When
            List<EmployeeResponseDTO> result = employeeService.getAllEmployees();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(employeeId);
            assertThat(result.get(0).firstName()).isEqualTo("Juan");

            verify(employeeRepository).findAll();
        }

        @Test
        @DisplayName("Debería obtener un empleado por ID exitosamente")
        void shouldGetEmployeeByIdSuccessfully() {
            // Given
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

            // When
            EmployeeResponseDTO result = employeeService.getEmployeeById(employeeId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(employeeId);
            assertThat(result.firstName()).isEqualTo("Juan");

            verify(employeeRepository).findById(employeeId);
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando el empleado no existe")
        void shouldThrowExceptionWhenEmployeeDoesNotExist() {
            // Given
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> employeeService.getEmployeeById(employeeId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Empleado no encontrado");

            verify(employeeRepository).findById(employeeId);
        }

        @Test
        @DisplayName("Debería obtener empleados por departamento exitosamente")
        void shouldGetEmployeesByDepartmentSuccessfully() {
            // Given
            List<Employee> employees = Arrays.asList(employee);
            when(employeeRepository.findByDepartmentId(departmentId)).thenReturn(employees);
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

            // When
            List<EmployeeResponseDTO> result = employeeService.getEmployeesByDepartment(departmentId);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(employeeId);

            verify(employeeRepository).findByDepartmentId(departmentId);
        }

        @Test
        @DisplayName("Debería obtener empleados por rol exitosamente")
        void shouldGetEmployeesByRoleSuccessfully() {
            // Given
            List<Employee> employees = Arrays.asList(employee);
            when(employeeRepository.findByRoleId(roleId)).thenReturn(employees);
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

            // When
            List<EmployeeResponseDTO> result = employeeService.getEmployeesByRole(roleId);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(employeeId);

            verify(employeeRepository).findByRoleId(roleId);
        }
    }

    @Nested
    @DisplayName("Actualización de Empleados")
    class UpdateEmployeeTests {

        @Test
        @DisplayName("Debería actualizar un empleado exitosamente")
        void shouldUpdateEmployeeSuccessfully() {
            // Given
            EmployeeRequestDTO updateRequest = new EmployeeRequestDTO(
                    "Juan Carlos",
                    "Pérez García",
                    "juan.perez@empresa.com",
                    departmentId,
                    roleId,
                    LocalDate.now(),
                    25
            );

            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
            when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

            // When
            EmployeeResponseDTO result = employeeService.updateEmployee(employeeId, updateRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(employeeId);

            verify(employeeRepository).findById(employeeId);
            verify(employeeRepository).save(any(Employee.class));
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando el empleado a actualizar no existe")
        void shouldThrowExceptionWhenEmployeeToUpdateDoesNotExist() {
            // Given
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> employeeService.updateEmployee(employeeId, employeeRequestDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Empleado no encontrado");

            verify(employeeRepository).findById(employeeId);
            verify(employeeRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Eliminación de Empleados")
    class DeleteEmployeeTests {

        @Test
        @DisplayName("Debería eliminar un empleado exitosamente")
        void shouldDeleteEmployeeSuccessfully() {
            // Given
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            // When
            employeeService.deleteEmployee(employeeId);

            // Then
            verify(employeeRepository).findById(employeeId);
            verify(employeeRepository).deleteById(employeeId);
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando el empleado a eliminar no existe")
        void shouldThrowExceptionWhenEmployeeToDeleteDoesNotExist() {
            // Given
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> employeeService.deleteEmployee(employeeId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Empleado no encontrado");

            verify(employeeRepository).findById(employeeId);
            verify(employeeRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("Gestión de Vacaciones")
    class VacationManagementTests {

        @Test
        @DisplayName("Debería permitir tomar vacaciones cuando hay días suficientes")
        void shouldAllowTakingVacationWhenSufficientDaysAvailable() {
            // Given
            int vacationDays = 5;
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

            // When
            EmployeeResponseDTO result = employeeService.takeVacation(employeeId, vacationDays);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.vacationDays()).isEqualTo(15); // 20 - 5

            verify(employeeRepository).findById(employeeId);
            verify(employeeRepository).save(any(Employee.class));
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando no hay suficientes días de vacaciones")
        void shouldThrowExceptionWhenInsufficientVacationDays() {
            // Given
            int vacationDays = 25; // Más de los disponibles (20)
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            // When & Then
            assertThatThrownBy(() -> employeeService.takeVacation(employeeId, vacationDays))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("No tiene suficientes días de vacaciones disponibles");

            verify(employeeRepository).findById(employeeId);
            verify(employeeRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debería agregar días de vacaciones exitosamente")
        void shouldAddVacationDaysSuccessfully() {
            // Given
            int additionalDays = 5;
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

            // When
            EmployeeResponseDTO result = employeeService.addVacationDays(employeeId, additionalDays);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.vacationDays()).isEqualTo(25); // 20 + 5

            verify(employeeRepository).findById(employeeId);
            verify(employeeRepository).save(any(Employee.class));
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando el empleado para vacaciones no existe")
        void shouldThrowExceptionWhenEmployeeForVacationDoesNotExist() {
            // Given
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> employeeService.takeVacation(employeeId, 5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Empleado no encontrado");

            verify(employeeRepository).findById(employeeId);
            verify(employeeRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Casos Edge y Validaciones")
    class EdgeCasesAndValidationsTests {

        @Test
        @DisplayName("Debería manejar lista vacía de empleados")
        void shouldHandleEmptyEmployeeList() {
            // Given
            when(employeeRepository.findAll()).thenReturn(Arrays.asList());

            // When
            List<EmployeeResponseDTO> result = employeeService.getAllEmployees();

            // Then
            assertThat(result).isEmpty();
            verify(employeeRepository).findAll();
        }

        @Test
        @DisplayName("Debería manejar actualización con datos nulos")
        void shouldHandleUpdateWithNullData() {
            // Given
            EmployeeRequestDTO updateRequest = new EmployeeRequestDTO(
                    null, null, null, null, null, null, 0
            );

            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
            when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

            // When
            EmployeeResponseDTO result = employeeService.updateEmployee(employeeId, updateRequest);

            // Then
            assertThat(result).isNotNull();
            // Los campos nulos no deberían cambiar los valores existentes
            assertThat(result.firstName()).isEqualTo("Juan");
            assertThat(result.lastName()).isEqualTo("Pérez");

            verify(employeeRepository).findById(employeeId);
            verify(employeeRepository).save(any(Employee.class));
        }

        @Test
        @DisplayName("Debería manejar empleado con fecha de contratación nula")
        void shouldHandleEmployeeWithNullHireDate() {
            // Given
            EmployeeRequestDTO requestWithNullDate = new EmployeeRequestDTO(
                    "Juan", "Pérez", "juan.perez@empresa.com", departmentId, roleId, null, 20
            );

            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
            when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

            // When
            EmployeeResponseDTO result = employeeService.createEmployee(requestWithNullDate);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.hireDate()).isEqualTo(LocalDate.now()); // Debería usar la fecha actual

            verify(employeeRepository).save(any(Employee.class));
        }
    }
}