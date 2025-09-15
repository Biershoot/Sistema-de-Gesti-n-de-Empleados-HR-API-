package com.alejandro.microservices.hr_api.integration;

import com.alejandro.microservices.hr_api.application.dto.EmployeeRequestDTO;
import com.alejandro.microservices.hr_api.domain.model.Department;
import com.alejandro.microservices.hr_api.domain.model.Role;
import com.alejandro.microservices.hr_api.domain.repository.DepartmentRepository;
import com.alejandro.microservices.hr_api.domain.repository.EmployeeRepository;
import com.alejandro.microservices.hr_api.domain.repository.RoleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración para el sistema de empleados.
 *
 * Estas pruebas verifican el comportamiento completo del sistema,
 * incluyendo la interacción entre controladores, servicios y repositorios.
 *
 * @author Sistema HR API
 * @version 1.0
 * @since 2025-01-14
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Employee Integration Tests")
class EmployeeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Department department;
    private Role role;
    private UUID departmentId;
    private UUID roleId;

    @BeforeEach
    void setUp() {
        // Crear datos de prueba
        departmentId = UUID.randomUUID();
        roleId = UUID.randomUUID();

        department = new Department(departmentId, "Recursos Humanos");
        role = new Role(roleId, "ADMIN");

        departmentRepository.save(department);
        roleRepository.save(role);
    }

    @Nested
    @DisplayName("Flujo Completo de Empleados")
    class CompleteEmployeeFlowTests {

        @Test
        @DisplayName("Debería completar el flujo completo de CRUD de empleados")
        void shouldCompleteFullEmployeeCrudFlow() throws Exception {
            // 1. Crear empleado
            EmployeeRequestDTO createRequest = new EmployeeRequestDTO(
                    "Juan",
                    "Pérez",
                    "juan.perez@empresa.com",
                    departmentId,
                    roleId,
                    LocalDate.now(),
                    20
            );

            String createResponse = mockMvc.perform(post("/api/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.firstName").value("Juan"))
                    .andExpect(jsonPath("$.lastName").value("Pérez"))
                    .andExpect(jsonPath("$.email").value("juan.perez@empresa.com"))
                    .andExpect(jsonPath("$.vacationDays").value(20))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            // Extraer ID del empleado creado
            String employeeId = objectMapper.readTree(createResponse).get("id").asText();

            // 2. Obtener empleado por ID
            mockMvc.perform(get("/api/employees/{id}", employeeId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(employeeId))
                    .andExpect(jsonPath("$.firstName").value("Juan"))
                    .andExpect(jsonPath("$.lastName").value("Pérez"));

            // 3. Obtener todos los empleados
            mockMvc.perform(get("/api/employees"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(employeeId))
                    .andExpect(jsonPath("$[0].firstName").value("Juan"));

            // 4. Actualizar empleado
            EmployeeRequestDTO updateRequest = new EmployeeRequestDTO(
                    "Juan Carlos",
                    "Pérez García",
                    "juan.perez@empresa.com",
                    departmentId,
                    roleId,
                    LocalDate.now(),
                    25
            );

            mockMvc.perform(put("/api/employees/{id}", employeeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName").value("Juan Carlos"))
                    .andExpect(jsonPath("$.lastName").value("Pérez García"))
                    .andExpect(jsonPath("$.vacationDays").value(25));

            // 5. Tomar vacaciones
            mockMvc.perform(put("/api/employees/{id}/vacation", employeeId)
                            .param("days", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.vacationDays").value(20)); // 25 - 5

            // 6. Agregar días de vacaciones
            mockMvc.perform(put("/api/employees/{id}/vacation/add", employeeId)
                            .param("days", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.vacationDays").value(30)); // 20 + 10

            // 7. Obtener empleados por departamento
            mockMvc.perform(get("/api/employees/department/{departmentId}", departmentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(employeeId));

            // 8. Obtener empleados por rol
            mockMvc.perform(get("/api/employees/role/{roleId}", roleId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(employeeId));

            // 9. Eliminar empleado
            mockMvc.perform(delete("/api/employees/{id}", employeeId))
                    .andExpect(status().isNoContent());

            // 10. Verificar que el empleado fue eliminado
            mockMvc.perform(get("/api/employees/{id}", employeeId))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Debería manejar múltiples empleados correctamente")
        void shouldHandleMultipleEmployeesCorrectly() throws Exception {
            // Crear múltiples empleados
            for (int i = 1; i <= 3; i++) {
                EmployeeRequestDTO request = new EmployeeRequestDTO(
                        "Empleado" + i,
                        "Apellido" + i,
                        "empleado" + i + "@empresa.com",
                        departmentId,
                        roleId,
                        LocalDate.now(),
                        20
                );

                mockMvc.perform(post("/api/employees")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated());
            }

            // Verificar que se crearon todos los empleados
            mockMvc.perform(get("/api/employees"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(3));

            // Verificar empleados por departamento
            mockMvc.perform(get("/api/employees/department/{departmentId}", departmentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(3));

            // Verificar empleados por rol
            mockMvc.perform(get("/api/employees/role/{roleId}", roleId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(3));
        }
    }

    @Nested
    @DisplayName("Validaciones de Integración")
    class IntegrationValidationTests {

        @Test
        @DisplayName("Debería validar que no se puedan crear empleados con email duplicado")
        void shouldValidateNoDuplicateEmails() throws Exception {
            // Crear primer empleado
            EmployeeRequestDTO firstEmployee = new EmployeeRequestDTO(
                    "Juan",
                    "Pérez",
                    "juan.perez@empresa.com",
                    departmentId,
                    roleId,
                    LocalDate.now(),
                    20
            );

            mockMvc.perform(post("/api/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(firstEmployee)))
                    .andExpect(status().isCreated());

            // Intentar crear segundo empleado con mismo email
            EmployeeRequestDTO secondEmployee = new EmployeeRequestDTO(
                    "Pedro",
                    "García",
                    "juan.perez@empresa.com", // Mismo email
                    departmentId,
                    roleId,
                    LocalDate.now(),
                    15
            );

            mockMvc.perform(post("/api/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(secondEmployee)))
                    .andExpect(status().isBadRequest());

            // Verificar que solo existe un empleado
            mockMvc.perform(get("/api/employees"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("Debería validar que no se puedan crear empleados con departamento inexistente")
        void shouldValidateNonExistentDepartment() throws Exception {
            UUID nonExistentDepartmentId = UUID.randomUUID();

            EmployeeRequestDTO request = new EmployeeRequestDTO(
                    "Juan",
                    "Pérez",
                    "juan.perez@empresa.com",
                    nonExistentDepartmentId,
                    roleId,
                    LocalDate.now(),
                    20
            );

            mockMvc.perform(post("/api/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            // Verificar que no se creó ningún empleado
            mockMvc.perform(get("/api/employees"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("Debería validar que no se puedan crear empleados con rol inexistente")
        void shouldValidateNonExistentRole() throws Exception {
            UUID nonExistentRoleId = UUID.randomUUID();

            EmployeeRequestDTO request = new EmployeeRequestDTO(
                    "Juan",
                    "Pérez",
                    "juan.perez@empresa.com",
                    departmentId,
                    nonExistentRoleId,
                    LocalDate.now(),
                    20
            );

            mockMvc.perform(post("/api/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            // Verificar que no se creó ningún empleado
            mockMvc.perform(get("/api/employees"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("Gestión de Vacaciones")
    class VacationManagementTests {

        @Test
        @DisplayName("Debería manejar el flujo completo de gestión de vacaciones")
        void shouldHandleCompleteVacationManagementFlow() throws Exception {
            // Crear empleado
            EmployeeRequestDTO createRequest = new EmployeeRequestDTO(
                    "Juan",
                    "Pérez",
                    "juan.perez@empresa.com",
                    departmentId,
                    roleId,
                    LocalDate.now(),
                    20
            );

            String createResponse = mockMvc.perform(post("/api/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            String employeeId = objectMapper.readTree(createResponse).get("id").asText();

            // Tomar 5 días de vacaciones
            mockMvc.perform(put("/api/employees/{id}/vacation", employeeId)
                            .param("days", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.vacationDays").value(15));

            // Tomar 10 días más de vacaciones
            mockMvc.perform(put("/api/employees/{id}/vacation", employeeId)
                            .param("days", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.vacationDays").value(5));

            // Intentar tomar más días de los disponibles
            mockMvc.perform(put("/api/employees/{id}/vacation", employeeId)
                            .param("days", "10"))
                    .andExpect(status().isBadRequest());

            // Agregar 10 días de vacaciones
            mockMvc.perform(put("/api/employees/{id}/vacation/add", employeeId)
                            .param("days", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.vacationDays").value(15));

            // Verificar estado final
            mockMvc.perform(get("/api/employees/{id}", employeeId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.vacationDays").value(15));
        }
    }

    @Nested
    @DisplayName("Manejo de Errores de Integración")
    class IntegrationErrorHandlingTests {

        @Test
        @DisplayName("Debería manejar errores de validación de datos")
        void shouldHandleDataValidationErrors() throws Exception {
            // Intentar crear empleado con datos inválidos
            EmployeeRequestDTO invalidRequest = new EmployeeRequestDTO(
                    "", // Nombre vacío
                    "Pérez",
                    "invalid-email", // Email inválido
                    departmentId,
                    roleId,
                    LocalDate.now(),
                    -5 // Días de vacaciones negativos
            );

            mockMvc.perform(post("/api/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Debería manejar operaciones en empleados inexistentes")
        void shouldHandleOperationsOnNonExistentEmployees() throws Exception {
            UUID nonExistentEmployeeId = UUID.randomUUID();

            // Intentar obtener empleado inexistente
            mockMvc.perform(get("/api/employees/{id}", nonExistentEmployeeId))
                    .andExpect(status().isBadRequest());

            // Intentar actualizar empleado inexistente
            EmployeeRequestDTO updateRequest = new EmployeeRequestDTO(
                    "Juan",
                    "Pérez",
                    "juan.perez@empresa.com",
                    departmentId,
                    roleId,
                    LocalDate.now(),
                    20
            );

            mockMvc.perform(put("/api/employees/{id}", nonExistentEmployeeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest());

            // Intentar eliminar empleado inexistente
            mockMvc.perform(delete("/api/employees/{id}", nonExistentEmployeeId))
                    .andExpect(status().isBadRequest());
        }
    }
}
