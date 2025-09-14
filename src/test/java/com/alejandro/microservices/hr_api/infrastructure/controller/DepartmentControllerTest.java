package com.alejandro.microservices.hr_api.infrastructure.controller;

import com.alejandro.microservices.hr_api.application.dto.DepartmentDTO;
import com.alejandro.microservices.hr_api.application.service.DepartmentService;
import com.alejandro.microservices.hr_api.infrastructure.controller.DepartmentController.CreateDepartmentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepartmentController.class)
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DepartmentService departmentService;

    @Autowired
    private ObjectMapper objectMapper;

    private DepartmentDTO departmentDTO;
    private UUID departmentId;

    @BeforeEach
    void setUp() {
        departmentId = UUID.randomUUID();
        departmentDTO = new DepartmentDTO(departmentId, "IT");
    }

    @Test
    void createDepartment_ShouldReturnCreatedDepartment() throws Exception {
        CreateDepartmentRequest request = new CreateDepartmentRequest("IT");
        when(departmentService.createDepartment("IT")).thenReturn(departmentDTO);

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(departmentId.toString()))
                .andExpect(jsonPath("$.name").value("IT"));
    }

    @Test
    void getAllDepartments_ShouldReturnDepartmentList() throws Exception {
        List<DepartmentDTO> departments = Arrays.asList(
                departmentDTO,
                new DepartmentDTO(UUID.randomUUID(), "HR")
        );
        when(departmentService.getAllDepartments()).thenReturn(departments);

        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("IT"))
                .andExpect(jsonPath("$[1].name").value("HR"));
    }

    @Test
    void getDepartmentById_ShouldReturnDepartment() throws Exception {
        when(departmentService.getDepartmentById(departmentId)).thenReturn(departmentDTO);

        mockMvc.perform(get("/api/departments/{id}", departmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(departmentId.toString()))
                .andExpect(jsonPath("$.name").value("IT"));
    }

    @Test
    void deleteDepartment_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/departments/{id}", departmentId))
                .andExpect(status().isNoContent());
    }

    @Test
    void createDepartment_WithBlankName_ShouldReturnBadRequest() throws Exception {
        CreateDepartmentRequest invalidRequest = new CreateDepartmentRequest("");

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDepartmentById_WithInvalidUUID_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/departments/invalid-uuid"))
                .andExpect(status().isBadRequest());
    }
}
