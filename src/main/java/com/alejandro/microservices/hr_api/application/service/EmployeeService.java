package com.alejandro.microservices.hr_api.application.service;

import com.alejandro.microservices.hr_api.application.dto.EmployeeDTO;
import com.alejandro.microservices.hr_api.application.dto.EmployeeRequestDTO;
import com.alejandro.microservices.hr_api.application.dto.EmployeeResponseDTO;
import com.alejandro.microservices.hr_api.application.dto.DepartmentDTO;
import com.alejandro.microservices.hr_api.application.dto.RoleDTO;
import com.alejandro.microservices.hr_api.domain.model.Department;
import com.alejandro.microservices.hr_api.domain.model.Employee;
import com.alejandro.microservices.hr_api.domain.model.Role;
import com.alejandro.microservices.hr_api.domain.repository.DepartmentRepository;
import com.alejandro.microservices.hr_api.domain.repository.EmployeeRepository;
import com.alejandro.microservices.hr_api.domain.repository.RoleRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository,
                           RoleRepository roleRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.roleRepository = roleRepository;
    }

    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO request) {
        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new IllegalArgumentException("Departamento no encontrado"));
        Role role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));

        Employee employee = new Employee(
                UUID.randomUUID(),
                request.firstName(),
                request.lastName(),
                request.email(),
                department,
                role,
                request.hireDate() != null ? request.hireDate() : LocalDate.now(),
                request.vacationDays() > 0 ? request.vacationDays() : 15 // vacaciones iniciales por defecto
        );

        Employee savedEmployee = employeeRepository.save(employee);
        return mapToResponseDTO(savedEmployee);
    }

    public EmployeeResponseDTO createEmployee(String firstName, String lastName, String email,
                                            UUID departmentId, UUID roleId) {
        EmployeeRequestDTO request = new EmployeeRequestDTO(
                firstName, lastName, email, departmentId, roleId, LocalDate.now(), 15
        );
        return createEmployee(request);
    }

    public List<EmployeeResponseDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<EmployeeDTO> getAllEmployeesBasic() {
        return employeeRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public EmployeeResponseDTO getEmployeeById(UUID id) {
        return employeeRepository.findById(id)
                .map(this::mapToResponseDTO)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));
    }

    public List<EmployeeResponseDTO> getEmployeesByDepartment(UUID departmentId) {
        return employeeRepository.findByDepartmentId(departmentId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<EmployeeResponseDTO> getEmployeesByRole(UUID roleId) {
        return employeeRepository.findByRoleId(roleId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public EmployeeResponseDTO updateEmployee(UUID id, EmployeeRequestDTO request) {
        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));

        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new IllegalArgumentException("Departamento no encontrado"));
        Role role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));

        Employee updatedEmployee = new Employee(
                id,
                request.firstName(),
                request.lastName(),
                request.email(),
                department,
                role,
                request.hireDate() != null ? request.hireDate() : existingEmployee.getHireDate(),
                request.vacationDays() >= 0 ? request.vacationDays() : existingEmployee.getVacationDays()
        );

        Employee savedEmployee = employeeRepository.save(updatedEmployee);
        return mapToResponseDTO(savedEmployee);
    }

    public void deleteEmployee(UUID id) {
        if (!employeeRepository.findById(id).isPresent()) {
            throw new IllegalArgumentException("Empleado no encontrado");
        }
        employeeRepository.deleteById(id);
    }

    public EmployeeResponseDTO takeVacation(UUID employeeId, int days) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));

        employee.takeVacation(days);
        Employee savedEmployee = employeeRepository.save(employee);
        return mapToResponseDTO(savedEmployee);
    }

    public EmployeeResponseDTO addVacationDays(UUID employeeId, int days) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));

        employee.addVacationDays(days);
        Employee savedEmployee = employeeRepository.save(employee);
        return mapToResponseDTO(savedEmployee);
    }

    // Mappers
    private EmployeeDTO mapToDTO(Employee employee) {
        return new EmployeeDTO(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getDepartment().getId(),
                employee.getRole().getId(),
                employee.getHireDate(),
                employee.getVacationDays()
        );
    }

    private EmployeeResponseDTO mapToResponseDTO(Employee employee) {
        DepartmentDTO departmentDTO = new DepartmentDTO(
                employee.getDepartment().getId(),
                employee.getDepartment().getName()
        );

        RoleDTO roleDTO = new RoleDTO(
                employee.getRole().getId(),
                employee.getRole().getName()
        );

        return new EmployeeResponseDTO(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                departmentDTO,
                roleDTO,
                employee.getHireDate(),
                employee.getVacationDays()
        );
    }
}
