package com.alejandro.microservices.hr_api.infrastructure.controller;

import com.alejandro.microservices.hr_api.application.dto.LeaveRequestDTO;
import com.alejandro.microservices.hr_api.application.dto.LeaveResponseDTO;
import com.alejandro.microservices.hr_api.application.service.LeaveService;
import com.alejandro.microservices.hr_api.domain.model.LeaveType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LeaveController.class)
class LeaveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LeaveService leaveService;

    @Autowired
    private ObjectMapper objectMapper;

    private LeaveRequestDTO validRequest;
    private LeaveResponseDTO mockResponse;
    private UUID employeeId;
    private UUID leaveId;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        leaveId = UUID.randomUUID();

        validRequest = new LeaveRequestDTO(
                employeeId,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5),
                LeaveType.VACATION
        );

        mockResponse = new LeaveResponseDTO(
                leaveId,
                employeeId,
                validRequest.startDate(),
                validRequest.endDate(),
                validRequest.type()
        );
    }

    @Test
    void shouldCreateLeaveSuccessfully() throws Exception {
        // Arrange
        when(leaveService.requestLeave(any(LeaveRequestDTO.class))).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/leaves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(leaveId.toString()))
                .andExpect(jsonPath("$.employeeId").value(employeeId.toString()))
                .andExpect(jsonPath("$.type").value("VACATION"));
    }

    @Test
    void shouldGetLeaveById() throws Exception {
        // Arrange
        when(leaveService.getLeaveById(leaveId)).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/leaves/{id}", leaveId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(leaveId.toString()))
                .andExpect(jsonPath("$.employeeId").value(employeeId.toString()));
    }

    @Test
    void shouldGetLeavesByEmployee() throws Exception {
        // Arrange
        List<LeaveResponseDTO> leaves = Arrays.asList(mockResponse);
        when(leaveService.getLeavesByEmployee(employeeId)).thenReturn(leaves);

        // Act & Assert
        mockMvc.perform(get("/api/leaves/employee/{employeeId}", employeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].employeeId").value(employeeId.toString()));
    }

    @Test
    void shouldGetLeavesByDateRange() throws Exception {
        // Arrange
        List<LeaveResponseDTO> leaves = Arrays.asList(mockResponse);
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(30);
        when(leaveService.getLeavesByDateRange(startDate, endDate)).thenReturn(leaves);

        // Act & Assert
        mockMvc.perform(get("/api/leaves/range")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(leaveId.toString()));
    }

    @Test
    void shouldCancelLeave() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/leaves/{id}", leaveId))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnBadRequestForInvalidLeaveRequest() throws Exception {
        // Arrange
        LeaveRequestDTO invalidRequest = new LeaveRequestDTO(
                null, // Invalid: null employeeId
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5),
                LeaveType.VACATION
        );

        // Act & Assert
        mockMvc.perform(post("/api/leaves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
