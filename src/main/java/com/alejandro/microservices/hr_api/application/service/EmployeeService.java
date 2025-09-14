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
        validateEmployeeRequest(request);

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
                Math.max(request.vacationDays(), 0) // Asegurar que no sea negativo, con 0 como mínimo
        );

        Employee savedEmployee = employeeRepository.save(employee);
        return mapToResponseDTO(savedEmployee);
    }

    public EmployeeResponseDTO createEmployee(String firstName, String lastName, String email,
                                            UUID departmentId, UUID roleId) {
        validateBasicEmployeeData(firstName, lastName, email, departmentId, roleId);

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
        validateId(id, "El ID del empleado no puede ser nulo");

        return employeeRepository.findById(id)
                .map(this::mapToResponseDTO)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));
    }

    public List<EmployeeResponseDTO> getEmployeesByDepartment(UUID departmentId) {
        validateId(departmentId, "El ID del departamento no puede ser nulo");

        return employeeRepository.findByDepartmentId(departmentId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<EmployeeResponseDTO> getEmployeesByRole(UUID roleId) {
        validateId(roleId, "El ID del rol no puede ser nulo");

        return employeeRepository.findByRoleId(roleId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public EmployeeResponseDTO updateEmployee(UUID id, EmployeeRequestDTO request) {
        validateId(id, "El ID del empleado no puede ser nulo");
        validateEmployeeRequest(request);

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
        validateId(id, "El ID del empleado no puede ser nulo");

        if (!employeeRepository.findById(id).isPresent()) {
            throw new IllegalArgumentException("Empleado no encontrado");
        }
        employeeRepository.deleteById(id);
    }

    public EmployeeResponseDTO takeVacation(UUID employeeId, int days) {
        validateId(employeeId, "El ID del empleado no puede ser nulo");
        validateVacationDays(days, "Los días de vacaciones deben ser positivos");

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));

        employee.takeVacation(days);
        Employee savedEmployee = employeeRepository.save(employee);
        return mapToResponseDTO(savedEmployee);
    }

    public EmployeeResponseDTO addVacationDays(UUID employeeId, int days) {
        validateId(employeeId, "El ID del empleado no puede ser nulo");
        validateVacationDays(days, "Los días a agregar deben ser positivos");

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));

        employee.addVacationDays(days);
        Employee savedEmployee = employeeRepository.save(employee);
        return mapToResponseDTO(savedEmployee);
    }

    // Métodos de validación privados
    private void validateEmployeeRequest(EmployeeRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Los datos del empleado no pueden ser nulos");
        }
        validateBasicEmployeeData(request.firstName(), request.lastName(),
                                request.email(), request.departmentId(), request.roleId());

        if (request.hireDate() != null && request.hireDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de contratación no puede ser futura");
        }

        if (request.vacationDays() < 0) {
            throw new IllegalArgumentException("Los días de vacaciones no pueden ser negativos");
        }
    }

    private void validateBasicEmployeeData(String firstName, String lastName, String email,
                                         UUID departmentId, UUID roleId) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido no puede estar vacío");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("El email no puede estar vacío");
        }
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("El formato del email no es válido");
        }
        validateId(departmentId, "El ID del departamento no puede ser nulo");
        validateId(roleId, "El ID del rol no puede ser nulo");
    }

    private void validateId(UUID id, String message) {
        if (id == null) {
            throw new IllegalArgumentException(message);
        }
    }

    private void validateVacationDays(int days, String message) {
        if (days <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".") &&
               email.length() > 5 &&
               email.indexOf("@") > 0 &&
               email.indexOf("@") < email.lastIndexOf(".") &&
               email.lastIndexOf(".") < email.length() - 1;
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
