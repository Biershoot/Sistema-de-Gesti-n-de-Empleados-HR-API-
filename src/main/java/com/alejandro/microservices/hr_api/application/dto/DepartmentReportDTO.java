package com.alejandro.microservices.hr_api.application.dto;

public record DepartmentReportDTO(
        String departmentName,
        long totalEmployees,
        long totalLeaves
) {}
