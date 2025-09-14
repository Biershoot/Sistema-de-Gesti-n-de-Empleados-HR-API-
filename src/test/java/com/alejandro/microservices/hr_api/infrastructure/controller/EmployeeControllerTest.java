package com.alejandro.microservices.hr_api.infrastructure.controller;

import com.alejandro.microservices.hr_api.application.dto.DepartmentDTO;
import com.alejandro.microservices.hr_api.application.dto.EmployeeRequestDTO;
import com.alejandro.microservices.hr_api.application.dto.EmployeeResponseDTO;
import com.alejandro.microservices.hr_api.application.dto.RoleDTO;
import com.alejandro.microservices.hr_api.application.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
@ActiveProfiles("test")
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    private EmployeeRequestDTO employeeRequest;
    private EmployeeResponseDTO employeeResponse;
    private UUID employeeId;
    private UUID departmentId;
    private UUID roleId;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        departmentId = UUID.randomUUID();
        roleId = UUID.randomUUID();

        employeeRequest = new EmployeeRequestDTO(
                "Juan",
                "Pérez",
                "juan.perez@company.com",
                departmentId,
                roleId,
                LocalDate.of(2023, 1, 15),
                15
        );

        DepartmentDTO departmentDTO = new DepartmentDTO(departmentId, "IT");
        RoleDTO roleDTO = new RoleDTO(roleId, "Developer");

        employeeResponse = new EmployeeResponseDTO(
                employeeId,
                "Juan",
                "Pérez",
                "juan.perez@company.com",
                departmentDTO,
                roleDTO,
                LocalDate.of(2023, 1, 15),
                15
        );
    }

    @Test
    void createEmployee_ShouldReturnCreatedEmployee() throws Exception {
        when(employeeService.createEmployee(any(EmployeeRequestDTO.class)))
                .thenReturn(employeeResponse);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(employeeId.toString()))
                .andExpect(jsonPath("$.firstName").value("Juan"))
                .andExpect(jsonPath("$.lastName").value("Pérez"))
                .andExpect(jsonPath("$.email").value("juan.perez@company.com"));
    }

    @Test
    void getAllEmployees_ShouldReturnEmployeeList() throws Exception {
        List<EmployeeResponseDTO> employees = Arrays.asList(employeeResponse);
        when(employeeService.getAllEmployees()).thenReturn(employees);

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Juan"))
                .andExpect(jsonPath("$[0].email").value("juan.perez@company.com"));
    }

    @Test
    void getEmployeeById_ShouldReturnEmployee() throws Exception {
        when(employeeService.getEmployeeById(employeeId)).thenReturn(employeeResponse);

        mockMvc.perform(get("/api/employees/{id}", employeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(employeeId.toString()))
                .andExpect(jsonPath("$.firstName").value("Juan"));
    }

    @Test
    void updateEmployee_ShouldReturnUpdatedEmployee() throws Exception {
        when(employeeService.updateEmployee(eq(employeeId), any(EmployeeRequestDTO.class)))
                .thenReturn(employeeResponse);

        mockMvc.perform(put("/api/employees/{id}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(employeeId.toString()));
    }

    @Test
    void deleteEmployee_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/employees/{id}", employeeId))
                .andExpect(status().isNoContent());
    }

    @Test
    void getEmployeesByDepartment_ShouldReturnEmployeeList() throws Exception {
        List<EmployeeResponseDTO> employees = Arrays.asList(employeeResponse);
        when(employeeService.getEmployeesByDepartment(departmentId)).thenReturn(employees);

        mockMvc.perform(get("/api/employees/department/{departmentId}", departmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].department.id").value(departmentId.toString()));
    }

    @Test
    void takeVacation_ShouldReturnUpdatedEmployee() throws Exception {
        int vacationDays = 5;
        EmployeeResponseDTO updatedEmployee = new EmployeeResponseDTO(
                employeeId,
                "Juan",
                "Pérez",
                "juan.perez@company.com",
                new DepartmentDTO(departmentId, "IT"),
                new RoleDTO(roleId, "Developer"),
                LocalDate.of(2023, 1, 15),
                10 // 15 - 5 días de vacaciones
        );

        when(employeeService.takeVacation(employeeId, vacationDays)).thenReturn(updatedEmployee);

        mockMvc.perform(put("/api/employees/{id}/vacation", employeeId)
                        .param("days", String.valueOf(vacationDays)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vacationDays").value(10));
    }

    @Test
    void createEmployee_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        EmployeeRequestDTO invalidRequest = new EmployeeRequestDTO(
                "", // nombre vacío
                "Pérez",
                "invalid-email", // email inválido
                departmentId,
                roleId,
                LocalDate.of(2023, 1, 15),
                -5 // días negativos
        );

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
