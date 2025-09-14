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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación para la gestión de empleados.
 *
 * Esta clase implementa los casos de uso del sistema relacionados con empleados,
 * siguiendo los principios de la Arquitectura Hexagonal donde los servicios
 * de aplicación coordinan las operaciones entre el dominio y la infraestructura.
 *
 * Responsabilidades:
 * - Orquestar operaciones CRUD de empleados
 * - Validar reglas de negocio
 * - Coordinar con repositorios de dominio
 * - Transformar entre DTOs y entidades de dominio
 * - Gestionar vacaciones de empleados
 *
 * @author Sistema HR API
 * @version 1.0
 */
@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param employeeRepository Repositorio para operaciones de empleados
     * @param departmentRepository Repositorio para operaciones de departamentos
     * @param roleRepository Repositorio para operaciones de roles
     */
    public EmployeeService(EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository,
                           RoleRepository roleRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.roleRepository = roleRepository;
    }

    /**
     * Crea un nuevo empleado en el sistema.
     *
     * Validaciones aplicadas:
     * - Email único
     * - Departamento existente
     * - Rol existente
     * - Datos requeridos completos
     *
     * @param request DTO con los datos del empleado a crear
     * @return DTO con los datos del empleado creado
     * @throws IllegalArgumentException si la validación falla
     */
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO request) {
        // Validar datos de entrada
        validateEmployeeRequest(request);

        // Verificar existencia de departamento y rol
        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new IllegalArgumentException("Departamento no encontrado"));
        Role role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));

        // Crear entidad de dominio con lógica de negocio
        Employee employee = new Employee(
                UUID.randomUUID(),
                request.firstName(),
                request.lastName(),
                request.email(),
                department,
                role,
                request.hireDate() != null ? request.hireDate() : LocalDate.now(),
                request.vacationDays() > 0 ? request.vacationDays() : 15 // Días por defecto
        );

        // Persistir y retornar resultado
        employeeRepository.save(employee);
        return mapToResponseDTO(employee);
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

        // Validar datos básicos primero
        if (request == null) {
            throw new IllegalArgumentException("Los datos del empleado no pueden ser nulos");
        }

        if (request.firstName() == null || request.firstName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        if (request.lastName() == null || request.lastName().trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido no puede estar vacío");
        }
        if (request.email() == null || request.email().trim().isEmpty()) {
            throw new IllegalArgumentException("El email no puede estar vacío");
        }
        if (!isValidEmail(request.email())) {
            throw new IllegalArgumentException("El formato del email no es válido");
        }

        // VALIDACIÓN DE EMAIL ÚNICO para updates - excluir el empleado actual
        validateUniqueEmail(request.email(), id);

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

    /**
     * Procesa la solicitud de vacaciones de un empleado.
     *
     * Este método delega la lógica de negocio a la entidad Employee,
     * siguiendo los principios de DDD donde el dominio contiene
     * las reglas de negocio.
     *
     * @param employeeId ID del empleado
     * @param days Número de días de vacaciones a tomar
     * @return DTO actualizado del empleado
     * @throws IllegalArgumentException si el empleado no existe o no tiene días suficientes
     */
    public EmployeeResponseDTO takeVacation(UUID employeeId, int days) {
        // Validar ID del empleado primero
        validateId(employeeId, "El ID del empleado no puede ser nulo");

        // Validar días de vacaciones
        validateVacationDays(days, "Los días de vacaciones deben ser positivos");

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));

        // La lógica de negocio está en la entidad de dominio
        employee.takeVacation(days);

        employeeRepository.save(employee);
        return mapToResponseDTO(employee);
    }

    /**
     * Agrega días de vacaciones a un empleado.
     *
     * @param employeeId ID del empleado
     * @param days Número de días a agregar
     * @return DTO actualizado del empleado
     */
    public EmployeeResponseDTO addVacationDays(UUID employeeId, int days) {
        // Validar ID del empleado primero
        validateId(employeeId, "El ID del empleado no puede ser nulo");

        // Validar días a agregar
        validateVacationDays(days, "Los días a agregar deben ser positivos");

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));

        employee.addVacationDays(days);

        employeeRepository.save(employee);
        return mapToResponseDTO(employee);
    }

    // Métodos de validación privados
    /**
     * Valida los datos de entrada para la creación/actualización de empleados.
     *
     * @param request DTO con los datos a validar
     * @throws IllegalArgumentException si alguna validación falla
     */
    private void validateEmployeeRequest(EmployeeRequestDTO request) {
        // Validar que el request no sea nulo
        if (request == null) {
            throw new IllegalArgumentException("Los datos del empleado no pueden ser nulos");
        }

        if (request.firstName() == null || request.firstName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        if (request.lastName() == null || request.lastName().trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido no puede estar vacío");
        }
        if (request.email() == null || request.email().trim().isEmpty()) {
            throw new IllegalArgumentException("El email no puede estar vacío");
        }
        if (!isValidEmail(request.email())) {
            throw new IllegalArgumentException("El formato del email no es válido");
        }

        // NUEVA VALIDACIÓN: Email único
        validateUniqueEmail(request.email(), null);

        if (request.departmentId() == null) {
            throw new IllegalArgumentException("El ID del departamento no puede ser nulo");
        }
        if (request.roleId() == null) {
            throw new IllegalArgumentException("El ID del rol no puede ser nulo");
        }

        // Validar fechas de contratación futuras
        if (request.hireDate() != null && request.hireDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de contratación no puede ser futura");
        }

        // Validar días de vacaciones negativos
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

        // NUEVA VALIDACIÓN: Email único
        validateUniqueEmail(email, null);

        validateId(departmentId, "El ID del departamento no puede ser nulo");
        validateId(roleId, "El ID del rol no puede ser nulo");
    }

    /**
     * Valida que el email sea único en el sistema.
     *
     * @param email Email a validar
     * @param excludeEmployeeId ID del empleado a excluir de la validación (para updates)
     * @throws IllegalArgumentException si el email ya existe
     */
    private void validateUniqueEmail(String email, UUID excludeEmployeeId) {
        Optional<Employee> existingEmployee = employeeRepository.findByEmail(email);

        if (existingEmployee.isPresent()) {
            // Si es una actualización, excluir el empleado actual
            if (excludeEmployeeId == null || !existingEmployee.get().getId().equals(excludeEmployeeId)) {
                throw new IllegalArgumentException("El correo ya está registrado para otro empleado");
            }
        }
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
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        // Patrón regex más robusto para validar emails
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
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
