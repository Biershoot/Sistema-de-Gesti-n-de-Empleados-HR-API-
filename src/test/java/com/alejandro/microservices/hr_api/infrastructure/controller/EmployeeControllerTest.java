package com.alejandro.microservices.hr_api.infrastructure.controller;

import com.alejandro.microservices.hr_api.application.dto.EmployeeRequestDTO;
import com.alejandro.microservices.hr_api.application.dto.EmployeeResponseDTO;
import com.alejandro.microservices.hr_api.application.dto.DepartmentDTO;
import com.alejandro.microservices.hr_api.application.dto.RoleDTO;
import com.alejandro.microservices.hr_api.application.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas unitarias para el EmployeeController.
 *
 * Estas pruebas verifican el comportamiento de los endpoints REST del controlador
 * de empleados, incluyendo validaciones de entrada, respuestas HTTP y manejo de errores.
 *
 * @author Sistema HR API
 * @version 1.0
 * @since 2025-01-14
 */
@WebMvcTest(EmployeeController.class)
@DisplayName("EmployeeController - Pruebas Unitarias")
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    private EmployeeRequestDTO employeeRequestDTO;
    private EmployeeResponseDTO employeeResponseDTO;
    private UUID employeeId;
    private UUID departmentId;
    private UUID roleId;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        departmentId = UUID.randomUUID();
        roleId = UUID.randomUUID();

        employeeRequestDTO = new EmployeeRequestDTO(
                "Juan",
                "Pérez",
                "juan.perez@empresa.com",
                departmentId,
                roleId,
                LocalDate.now(),
                20
        );

        employeeResponseDTO = new EmployeeResponseDTO(
                employeeId,
                "Juan",
                "Pérez",
                "juan.perez@empresa.com",
                new DepartmentDTO(departmentId, "Recursos Humanos"),
                new RoleDTO(roleId, "ADMIN"),
                LocalDate.now(),
                20
        );
    }

    @Nested
    @DisplayName("Creación de Empleados")
    class CreateEmployeeTests {

        @Test
        @DisplayName("Debería crear un empleado exitosamente con datos válidos")
        void shouldCreateEmployeeSuccessfullyWithValidData() throws Exception {
            // Given
            when(employeeService.createEmployee(any(EmployeeRequestDTO.class)))
                    .thenReturn(employeeResponseDTO);

            // When & Then
            mockMvc.perform(post("/api/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(employeeRequestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(employeeId.toString()))
                    .andExpect(jsonPath("$.firstName").value("Juan"))
                    .andExpect(jsonPath("$.lastName").value("Pérez"))
                    .andExpect(jsonPath("$.email").value("juan.perez@empresa.com"))
                    .andExpect(jsonPath("$.vacationDays").value(20))
                    .andExpect(jsonPath("$.department.id").value(departmentId.toString()))
                    .andExpect(jsonPath("$.department.name").value("Recursos Humanos"))
                    .andExpect(jsonPath("$.role.id").value(roleId.toString()))
                    .andExpect(jsonPath("$.role.name").value("ADMIN"));

            verify(employeeService).createEmployee(any(EmployeeRequestDTO.class));
        }

        @Test
        @DisplayName("Debería retornar error 400 cuando los datos son inválidos")
        void shouldReturnBadRequestWhenDataIsInvalid() throws Exception {
            // Given
            EmployeeRequestDTO invalidRequest = new EmployeeRequestDTO(
                    "", // Nombre vacío
                    "Pérez",
                    "invalid-email", // Email inválido
                    null, // Departamento nulo
                    roleId,
                    LocalDate.now(),
                    -5 // Días de vacaciones negativos
            );

            // When & Then
            mockMvc.perform(post("/api/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(employeeService, never()).createEmployee(any());
        }

        @Test
        @DisplayName("Debería retornar error 400 cuando el JSON es inválido")
        void shouldReturnBadRequestWhenJsonIsInvalid() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("invalid json"))
                    .andExpect(status().isBadRequest());

            verify(employeeService, never()).createEmployee(any());
        }
    }

    @Nested
    @DisplayName("Consulta de Empleados")
    class GetEmployeeTests {

        @Test
        @DisplayName("Debería obtener todos los empleados exitosamente")
        void shouldGetAllEmployeesSuccessfully() throws Exception {
            // Given
            List<EmployeeResponseDTO> employees = Arrays.asList(employeeResponseDTO);
            when(employeeService.getAllEmployees()).thenReturn(employees);

            // When & Then
            mockMvc.perform(get("/api/employees"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(employeeId.toString()))
                    .andExpect(jsonPath("$[0].firstName").value("Juan"))
                    .andExpect(jsonPath("$[0].lastName").value("Pérez"));

            verify(employeeService).getAllEmployees();
        }

        @Test
        @DisplayName("Debería obtener un empleado por ID exitosamente")
        void shouldGetEmployeeByIdSuccessfully() throws Exception {
            // Given
            when(employeeService.getEmployeeById(employeeId)).thenReturn(employeeResponseDTO);

            // When & Then
            mockMvc.perform(get("/api/employees/{id}", employeeId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(employeeId.toString()))
                    .andExpect(jsonPath("$.firstName").value("Juan"))
                    .andExpect(jsonPath("$.lastName").value("Pérez"));

            verify(employeeService).getEmployeeById(employeeId);
        }

        @Test
        @DisplayName("Debería retornar error 400 cuando el ID es inválido")
        void shouldReturnBadRequestWhenIdIsInvalid() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/employees/{id}", "invalid-uuid"))
                    .andExpect(status().isBadRequest());

            verify(employeeService, never()).getEmployeeById(any());
        }

        @Test
        @DisplayName("Debería obtener empleados por departamento exitosamente")
        void shouldGetEmployeesByDepartmentSuccessfully() throws Exception {
            // Given
            List<EmployeeResponseDTO> employees = Arrays.asList(employeeResponseDTO);
            when(employeeService.getEmployeesByDepartment(departmentId)).thenReturn(employees);

            // When & Then
            mockMvc.perform(get("/api/employees/department/{departmentId}", departmentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(employeeId.toString()));

            verify(employeeService).getEmployeesByDepartment(departmentId);
        }

        @Test
        @DisplayName("Debería obtener empleados por rol exitosamente")
        void shouldGetEmployeesByRoleSuccessfully() throws Exception {
            // Given
            List<EmployeeResponseDTO> employees = Arrays.asList(employeeResponseDTO);
            when(employeeService.getEmployeesByRole(roleId)).thenReturn(employees);

            // When & Then
            mockMvc.perform(get("/api/employees/role/{roleId}", roleId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(employeeId.toString()));

            verify(employeeService).getEmployeesByRole(roleId);
        }
    }

    @Nested
    @DisplayName("Actualización de Empleados")
    class UpdateEmployeeTests {

        @Test
        @DisplayName("Debería actualizar un empleado exitosamente")
        void shouldUpdateEmployeeSuccessfully() throws Exception {
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

            EmployeeResponseDTO updatedResponse = new EmployeeResponseDTO(
                    employeeId,
                    "Juan Carlos",
                    "Pérez García",
                    "juan.perez@empresa.com",
                    new DepartmentDTO(departmentId, "Recursos Humanos"),
                    new RoleDTO(roleId, "ADMIN"),
                    LocalDate.now(),
                    25
            );

            when(employeeService.updateEmployee(eq(employeeId), any(EmployeeRequestDTO.class)))
                    .thenReturn(updatedResponse);

            // When & Then
            mockMvc.perform(put("/api/employees/{id}", employeeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName").value("Juan Carlos"))
                    .andExpect(jsonPath("$.lastName").value("Pérez García"))
                    .andExpect(jsonPath("$.vacationDays").value(25));

            verify(employeeService).updateEmployee(eq(employeeId), any(EmployeeRequestDTO.class));
        }

        @Test
        @DisplayName("Debería retornar error 400 cuando el ID es inválido")
        void shouldReturnBadRequestWhenIdIsInvalid() throws Exception {
            // When & Then
            mockMvc.perform(put("/api/employees/{id}", "invalid-uuid")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(employeeRequestDTO)))
                    .andExpect(status().isBadRequest());

            verify(employeeService, never()).updateEmployee(any(), any());
        }
    }

    @Nested
    @DisplayName("Eliminación de Empleados")
    class DeleteEmployeeTests {

        @Test
        @DisplayName("Debería eliminar un empleado exitosamente")
        void shouldDeleteEmployeeSuccessfully() throws Exception {
            // Given
            doNothing().when(employeeService).deleteEmployee(employeeId);

            // When & Then
            mockMvc.perform(delete("/api/employees/{id}", employeeId))
                    .andExpect(status().isNoContent());

            verify(employeeService).deleteEmployee(employeeId);
        }

        @Test
        @DisplayName("Debería retornar error 400 cuando el ID es inválido")
        void shouldReturnBadRequestWhenIdIsInvalid() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/employees/{id}", "invalid-uuid"))
                    .andExpect(status().isBadRequest());

            verify(employeeService, never()).deleteEmployee(any());
        }
    }

    @Nested
    @DisplayName("Gestión de Vacaciones")
    class VacationManagementTests {

        @Test
        @DisplayName("Debería permitir tomar vacaciones exitosamente")
        void shouldAllowTakingVacationSuccessfully() throws Exception {
            // Given
            int vacationDays = 5;
            EmployeeResponseDTO updatedEmployee = new EmployeeResponseDTO(
                    employeeId,
                    "Juan",
                    "Pérez",
                    "juan.perez@empresa.com",
                    new DepartmentDTO(departmentId, "Recursos Humanos"),
                    new RoleDTO(roleId, "ADMIN"),
                    LocalDate.now(),
                    15 // 20 - 5
            );

            when(employeeService.takeVacation(employeeId, vacationDays)).thenReturn(updatedEmployee);

            // When & Then
            mockMvc.perform(put("/api/employees/{id}/vacation", employeeId)
                            .param("days", String.valueOf(vacationDays)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.vacationDays").value(15));

            verify(employeeService).takeVacation(employeeId, vacationDays);
        }

        @Test
        @DisplayName("Debería agregar días de vacaciones exitosamente")
        void shouldAddVacationDaysSuccessfully() throws Exception {
            // Given
            int additionalDays = 5;
            EmployeeResponseDTO updatedEmployee = new EmployeeResponseDTO(
                    employeeId,
                    "Juan",
                    "Pérez",
                    "juan.perez@empresa.com",
                    new DepartmentDTO(departmentId, "Recursos Humanos"),
                    new RoleDTO(roleId, "ADMIN"),
                    LocalDate.now(),
                    25 // 20 + 5
            );

            when(employeeService.addVacationDays(employeeId, additionalDays)).thenReturn(updatedEmployee);

            // When & Then
            mockMvc.perform(put("/api/employees/{id}/vacation/add", employeeId)
                            .param("days", String.valueOf(additionalDays)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.vacationDays").value(25));

            verify(employeeService).addVacationDays(employeeId, additionalDays);
        }

        @Test
        @DisplayName("Debería retornar error 400 cuando los días son inválidos")
        void shouldReturnBadRequestWhenDaysAreInvalid() throws Exception {
            // When & Then
            mockMvc.perform(put("/api/employees/{id}/vacation", employeeId)
                            .param("days", "invalid-number"))
                    .andExpect(status().isBadRequest());

            verify(employeeService, never()).takeVacation(any(), anyInt());
        }
    }

    @Nested
    @DisplayName("Manejo de Errores")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Debería manejar excepciones del servicio correctamente")
        void shouldHandleServiceExceptionsCorrectly() throws Exception {
            // Given
            when(employeeService.getEmployeeById(employeeId))
                    .thenThrow(new IllegalArgumentException("Empleado no encontrado"));

            // When & Then
            mockMvc.perform(get("/api/employees/{id}", employeeId))
                    .andExpect(status().isBadRequest());

            verify(employeeService).getEmployeeById(employeeId);
        }

        @Test
        @DisplayName("Debería manejar errores internos del servidor")
        void shouldHandleInternalServerErrors() throws Exception {
            // Given
            when(employeeService.getAllEmployees())
                    .thenThrow(new RuntimeException("Error interno del servidor"));

            // When & Then
            mockMvc.perform(get("/api/employees"))
                    .andExpect(status().isInternalServerError());

            verify(employeeService).getAllEmployees();
        }
    }

    @Nested
    @DisplayName("Validaciones de Contenido")
    class ContentValidationTests {

        @Test
        @DisplayName("Debería validar que el Content-Type sea JSON")
        void shouldValidateContentTypeIsJson() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/employees")
                            .contentType(MediaType.TEXT_PLAIN)
                            .content("invalid content"))
                    .andExpect(status().isUnsupportedMediaType());

            verify(employeeService, never()).createEmployee(any());
        }

        @Test
        @DisplayName("Debería aceptar solo métodos HTTP permitidos")
        void shouldAcceptOnlyAllowedHttpMethods() throws Exception {
            // When & Then
            mockMvc.perform(patch("/api/employees/{id}", employeeId))
                    .andExpect(status().isMethodNotAllowed());

            verify(employeeService, never()).updateEmployee(any(), any());
        }
    }
}