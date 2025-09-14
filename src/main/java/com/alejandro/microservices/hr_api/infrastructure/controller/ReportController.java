package com.alejandro.microservices.hr_api.infrastructure.controller;

import com.alejandro.microservices.hr_api.application.dto.DepartmentReportDTO;
import com.alejandro.microservices.hr_api.application.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para la generación y consulta de reportes del sistema HR.
 *
 * Este controlador expone endpoints para obtener reportes analíticos y estadísticos
 * del sistema de recursos humanos, proporcionando información valiosa para
 * la gestión y toma de decisiones organizacionales.
 *
 * Funcionalidades disponibles:
 * - Reportes consolidados de todos los departamentos
 * - Reportes específicos por departamento
 * - Métricas de empleados y utilización de permisos
 *
 * Endpoints disponibles:
 * - GET /api/reports/departments - Obtener reportes de todos los departamentos
 * - GET /api/reports/departments/{departmentId} - Reporte de departamento específico
 *
 * @author Sistema HR API
 * @version 1.0
 */
@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*") // Permitir CORS para dashboards frontend
public class ReportController {

    private final ReportService reportService;

    /**
     * Constructor con inyección de dependencias del servicio de reportes.
     *
     * @param reportService Servicio que contiene la lógica para generar reportes
     */
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Endpoint para obtener reportes de todos los departamentos.
     *
     * Proporciona una vista consolidada de la organización mostrando:
     * - Nombre del departamento
     * - Total de empleados por departamento
     * - Total de permisos/ausencias solicitados
     *
     * Útil para análisis organizacional y distribución de recursos humanos.
     *
     * @return ResponseEntity con lista de reportes departamentales y código HTTP 200
     */
    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentReportDTO>> getAllReports() {
        List<DepartmentReportDTO> reports = reportService.generateDepartmentReports();
        return ResponseEntity.ok(reports);
    }

    /**
     * Endpoint para obtener el reporte de un departamento específico.
     *
     * Proporciona métricas detalladas de un departamento en particular,
     * incluyendo conteo de empleados y estadísticas de permisos.
     *
     * @param departmentId UUID del departamento para el cual generar el reporte
     * @return ResponseEntity con el reporte del departamento y código HTTP 200
     * @throws IllegalArgumentException si el departamento no existe
     */
    @GetMapping("/departments/{departmentId}")
    public ResponseEntity<DepartmentReportDTO> getReportForDepartment(@PathVariable UUID departmentId) {
        DepartmentReportDTO report = reportService.generateReportForDepartment(departmentId);
        return ResponseEntity.ok(report);
    }
}
