package com.alejandro.microservices.hr_api.infrastructure.controller;

import com.alejandro.microservices.hr_api.application.dto.DepartmentReportDTO;
import com.alejandro.microservices.hr_api.application.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    private UUID departmentId;
    private DepartmentReportDTO mockReport;
    private List<DepartmentReportDTO> mockReports;

    @BeforeEach
    void setUp() {
        departmentId = UUID.randomUUID();

        mockReport = new DepartmentReportDTO("Desarrollo", 5, 12);

        DepartmentReportDTO report2 = new DepartmentReportDTO("Marketing", 3, 8);
        mockReports = Arrays.asList(mockReport, report2);
    }

    @Test
    void shouldGetAllDepartmentReports() throws Exception {
        // Arrange
        when(reportService.generateDepartmentReports()).thenReturn(mockReports);

        // Act & Assert
        mockMvc.perform(get("/api/reports/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].departmentName").value("Desarrollo"))
                .andExpect(jsonPath("$[0].totalEmployees").value(5))
                .andExpect(jsonPath("$[0].totalLeaves").value(12))
                .andExpect(jsonPath("$[1].departmentName").value("Marketing"))
                .andExpect(jsonPath("$[1].totalEmployees").value(3))
                .andExpect(jsonPath("$[1].totalLeaves").value(8));
    }

    @Test
    void shouldGetReportForSpecificDepartment() throws Exception {
        // Arrange
        when(reportService.generateReportForDepartment(departmentId)).thenReturn(mockReport);

        // Act & Assert
        mockMvc.perform(get("/api/reports/departments/{departmentId}", departmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departmentName").value("Desarrollo"))
                .andExpect(jsonPath("$.totalEmployees").value(5))
                .andExpect(jsonPath("$.totalLeaves").value(12));
    }

    @Test
    void shouldReturnEmptyListWhenNoDepartments() throws Exception {
        // Arrange
        when(reportService.generateDepartmentReports()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/reports/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturnDepartmentWithZeroEmployeesAndLeaves() throws Exception {
        // Arrange
        DepartmentReportDTO emptyReport = new DepartmentReportDTO("Ventas", 0, 0);
        when(reportService.generateDepartmentReports()).thenReturn(Arrays.asList(emptyReport));

        // Act & Assert
        mockMvc.perform(get("/api/reports/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].departmentName").value("Ventas"))
                .andExpect(jsonPath("$[0].totalEmployees").value(0))
                .andExpect(jsonPath("$[0].totalLeaves").value(0));
    }

    @Test
    void shouldHandleInvalidUUIDFormat() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/reports/departments/invalid-uuid"))
                .andExpect(status().isBadRequest());
    }
}
