package com.alejandro.microservices.hr_api.application.service;

import com.alejandro.microservices.hr_api.application.dto.DepartmentReportDTO;
import com.alejandro.microservices.hr_api.domain.model.Department;
import com.alejandro.microservices.hr_api.domain.model.Employee;
import com.alejandro.microservices.hr_api.domain.model.Leave;
import com.alejandro.microservices.hr_api.domain.model.LeaveType;
import com.alejandro.microservices.hr_api.domain.repository.DepartmentRepository;
import com.alejandro.microservices.hr_api.domain.repository.EmployeeRepository;
import com.alejandro.microservices.hr_api.domain.repository.LeaveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private LeaveRepository leaveRepository;

    @InjectMocks
    private ReportService reportService;

    private UUID departmentId;
    private UUID employeeId1;
    private UUID employeeId2;
    private Department mockDepartment;
    private Employee mockEmployee1;
    private Employee mockEmployee2;
    private Leave mockLeave1;
    private Leave mockLeave2;

    @BeforeEach
    void setUp() {
        departmentId = UUID.randomUUID();
        employeeId1 = UUID.randomUUID();
        employeeId2 = UUID.randomUUID();

        mockDepartment = new Department(departmentId, "Desarrollo");

        mockEmployee1 = new Employee(
                employeeId1, "Juan", "Pérez", "juan@test.com",
                mockDepartment, null, LocalDate.now(), 15
        );

        mockEmployee2 = new Employee(
                employeeId2, "María", "García", "maria@test.com",
                mockDepartment, null, LocalDate.now(), 20
        );

        mockLeave1 = new Leave(
                UUID.randomUUID(), employeeId1,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5),
                LeaveType.VACATION
        );

        mockLeave2 = new Leave(
                UUID.randomUUID(), employeeId2,
                LocalDate.now().plusDays(10), LocalDate.now().plusDays(12),
                LeaveType.SICK
        );
    }

    @Test
    void shouldGenerateDepartmentReportsSuccessfully() {
        // Arrange
        List<Department> departments = Arrays.asList(mockDepartment);
        List<Employee> employees = Arrays.asList(mockEmployee1, mockEmployee2);
        List<Leave> leavesForEmployee1 = Arrays.asList(mockLeave1);
        List<Leave> leavesForEmployee2 = Arrays.asList(mockLeave2);

        when(departmentRepository.findAll()).thenReturn(departments);
        when(employeeRepository.findByDepartmentId(departmentId)).thenReturn(employees);
        when(leaveRepository.findByEmployeeId(employeeId1)).thenReturn(leavesForEmployee1);
        when(leaveRepository.findByEmployeeId(employeeId2)).thenReturn(leavesForEmployee2);

        // Act
        List<DepartmentReportDTO> reports = reportService.generateDepartmentReports();

        // Assert
        assertNotNull(reports);
        assertEquals(1, reports.size());

        DepartmentReportDTO report = reports.get(0);
        assertEquals("Desarrollo", report.departmentName());
        assertEquals(2, report.totalEmployees());
        assertEquals(2, report.totalLeaves());

        verify(departmentRepository).findAll();
        verify(employeeRepository).findByDepartmentId(departmentId);
        verify(leaveRepository).findByEmployeeId(employeeId1);
        verify(leaveRepository).findByEmployeeId(employeeId2);
    }

    @Test
    void shouldGenerateReportForSpecificDepartment() {
        // Arrange
        List<Employee> employees = Arrays.asList(mockEmployee1, mockEmployee2);
        List<Leave> leavesForEmployee1 = Arrays.asList(mockLeave1);
        List<Leave> leavesForEmployee2 = Collections.emptyList();

        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(mockDepartment));
        when(employeeRepository.findByDepartmentId(departmentId)).thenReturn(employees);
        when(leaveRepository.findByEmployeeId(employeeId1)).thenReturn(leavesForEmployee1);
        when(leaveRepository.findByEmployeeId(employeeId2)).thenReturn(leavesForEmployee2);

        // Act
        DepartmentReportDTO report = reportService.generateReportForDepartment(departmentId);

        // Assert
        assertNotNull(report);
        assertEquals("Desarrollo", report.departmentName());
        assertEquals(2, report.totalEmployees());
        assertEquals(1, report.totalLeaves());

        verify(departmentRepository).findById(departmentId);
        verify(employeeRepository).findByDepartmentId(departmentId);
        verify(leaveRepository).findByEmployeeId(employeeId1);
        verify(leaveRepository).findByEmployeeId(employeeId2);
    }

    @Test
    void shouldReturnEmptyReportForDepartmentWithNoEmployees() {
        // Arrange
        when(departmentRepository.findAll()).thenReturn(Arrays.asList(mockDepartment));
        when(employeeRepository.findByDepartmentId(departmentId)).thenReturn(Collections.emptyList());

        // Act
        List<DepartmentReportDTO> reports = reportService.generateDepartmentReports();

        // Assert
        assertNotNull(reports);
        assertEquals(1, reports.size());

        DepartmentReportDTO report = reports.get(0);
        assertEquals("Desarrollo", report.departmentName());
        assertEquals(0, report.totalEmployees());
        assertEquals(0, report.totalLeaves());
    }

    @Test
    void shouldThrowExceptionWhenDepartmentNotFound() {
        // Arrange
        UUID nonExistentDepartmentId = UUID.randomUUID();
        when(departmentRepository.findById(nonExistentDepartmentId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reportService.generateReportForDepartment(nonExistentDepartmentId)
        );

        assertEquals("Departamento no encontrado", exception.getMessage());
        verify(departmentRepository).findById(nonExistentDepartmentId);
        verifyNoInteractions(employeeRepository, leaveRepository);
    }

    @Test
    void shouldHandleMultipleDepartments() {
        // Arrange
        UUID departmentId2 = UUID.randomUUID();
        Department mockDepartment2 = new Department(departmentId2, "Marketing");

        List<Department> departments = Arrays.asList(mockDepartment, mockDepartment2);

        when(departmentRepository.findAll()).thenReturn(departments);
        when(employeeRepository.findByDepartmentId(departmentId)).thenReturn(Arrays.asList(mockEmployee1));
        when(employeeRepository.findByDepartmentId(departmentId2)).thenReturn(Collections.emptyList());
        when(leaveRepository.findByEmployeeId(employeeId1)).thenReturn(Arrays.asList(mockLeave1));

        // Act
        List<DepartmentReportDTO> reports = reportService.generateDepartmentReports();

        // Assert
        assertNotNull(reports);
        assertEquals(2, reports.size());

        // Verificar primer departamento
        DepartmentReportDTO report1 = reports.stream()
                .filter(r -> r.departmentName().equals("Desarrollo"))
                .findFirst()
                .orElseThrow();
        assertEquals(1, report1.totalEmployees());
        assertEquals(1, report1.totalLeaves());

        // Verificar segundo departamento
        DepartmentReportDTO report2 = reports.stream()
                .filter(r -> r.departmentName().equals("Marketing"))
                .findFirst()
                .orElseThrow();
        assertEquals(0, report2.totalEmployees());
        assertEquals(0, report2.totalLeaves());
    }

    @Test
    void shouldCalculateLeavesCorrectlyForEmployeeWithMultipleLeaves() {
        // Arrange
        Leave additionalLeave = new Leave(
                UUID.randomUUID(), employeeId1,
                LocalDate.now().plusDays(20), LocalDate.now().plusDays(22),
                LeaveType.UNPAID
        );

        List<Employee> employees = Arrays.asList(mockEmployee1);
        List<Leave> multipleLeavesForEmployee1 = Arrays.asList(mockLeave1, additionalLeave);

        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(mockDepartment));
        when(employeeRepository.findByDepartmentId(departmentId)).thenReturn(employees);
        when(leaveRepository.findByEmployeeId(employeeId1)).thenReturn(multipleLeavesForEmployee1);

        // Act
        DepartmentReportDTO report = reportService.generateReportForDepartment(departmentId);

        // Assert
        assertNotNull(report);
        assertEquals("Desarrollo", report.departmentName());
        assertEquals(1, report.totalEmployees());
        assertEquals(2, report.totalLeaves()); // Dos permisos para el mismo empleado
    }
}
