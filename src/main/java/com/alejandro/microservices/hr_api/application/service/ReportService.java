package com.alejandro.microservices.hr_api.application.service;

import com.alejandro.microservices.hr_api.application.dto.DepartmentReportDTO;
import com.alejandro.microservices.hr_api.domain.model.Department;
import com.alejandro.microservices.hr_api.domain.model.Employee;
import com.alejandro.microservices.hr_api.domain.repository.DepartmentRepository;
import com.alejandro.microservices.hr_api.domain.repository.EmployeeRepository;
import com.alejandro.microservices.hr_api.domain.repository.LeaveRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación para la generación de reportes del sistema HR.
 *
 * Este servicio se encarga de generar reportes analíticos y estadísticos
 * sobre los datos de recursos humanos, proporcionando información valiosa
 * para la toma de decisiones gerenciales.
 *
 * Funcionalidades principales:
 * - Reportes por departamento (empleados y ausencias)
 * - Estadísticas de utilización de permisos
 * - Análisis de distribución de personal
 *
 * @author Sistema HR API
 * @version 1.0
 */
@Service
public class ReportService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveRepository leaveRepository;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param departmentRepository Repositorio para acceso a datos de departamentos
     * @param employeeRepository Repositorio para acceso a datos de empleados
     * @param leaveRepository Repositorio para acceso a datos de permisos/ausencias
     */
    public ReportService(DepartmentRepository departmentRepository,
                         EmployeeRepository employeeRepository,
                         LeaveRepository leaveRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.leaveRepository = leaveRepository;
    }

    /**
     * Genera reportes para todos los departamentos de la organización.
     *
     * Este método proporciona una vista consolidada de:
     * - Número total de empleados por departamento
     * - Cantidad total de permisos solicitados por departamento
     *
     * Útil para análisis organizacional y distribución de recursos.
     *
     * @return Lista de reportes, uno por cada departamento
     */
    public List<DepartmentReportDTO> generateDepartmentReports() {
        return departmentRepository.findAll().stream().map(dept -> {
            // Obtener empleados del departamento
            List<Employee> departmentEmployees = employeeRepository.findByDepartmentId(dept.getId());
            long employeeCount = departmentEmployees.size();

            // Calcular total de permisos/ausencias del departamento
            long leaveCount = departmentEmployees.stream()
                    .mapToLong(emp -> leaveRepository.findByEmployeeId(emp.getId()).size())
                    .sum();

            return new DepartmentReportDTO(dept.getName(), employeeCount, leaveCount);
        }).collect(Collectors.toList());
    }

    /**
     * Genera un reporte detallado para un departamento específico.
     *
     * Proporciona métricas específicas de un departamento, incluyendo:
     * - Conteo exacto de empleados
     * - Suma de todos los permisos solicitados por empleados del departamento
     *
     * @param departmentId ID del departamento para el cual generar el reporte
     * @return Reporte del departamento especificado
     * @throws IllegalArgumentException si el departamento no existe
     */
    public DepartmentReportDTO generateReportForDepartment(UUID departmentId) {
        // Validar existencia del departamento
        Department dept = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Departamento no encontrado"));

        // Obtener empleados y calcular métricas
        List<Employee> departmentEmployees = employeeRepository.findByDepartmentId(departmentId);
        long employeeCount = departmentEmployees.size();

        // Sumar permisos de todos los empleados del departamento
        long leaveCount = departmentEmployees.stream()
                .mapToLong(emp -> leaveRepository.findByEmployeeId(emp.getId()).size())
                .sum();

        return new DepartmentReportDTO(dept.getName(), employeeCount, leaveCount);
    }
}
