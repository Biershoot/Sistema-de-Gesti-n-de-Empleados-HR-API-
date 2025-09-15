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
 * <p>Este servicio actúa como intermediario entre la capa de presentación (controladores)
 * y la capa de dominio, implementando la lógica de negocio específica para la
 * gestión de empleados en el sistema de recursos humanos.</p>
 *
 * <p>Responsabilidades principales:</p>
 * <ul>
 *   <li>Creación y registro de nuevos empleados</li>
 *   <li>Consulta y búsqueda de empleados existentes</li>
 *   <li>Actualización de información de empleados</li>
 *   <li>Eliminación de empleados del sistema</li>
 *   <li>Gestión de vacaciones y permisos</li>
 *   <li>Validación de reglas de negocio</li>
 * </ul>
 *
 * @author Sistema HR API
 * @version 1.0
 * @since 2025-01-14
 */
@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;

    /**
     * Constructor con inyección de dependencias de los repositorios necesarios.
     *
     * @param employeeRepository Repositorio para operaciones de persistencia de empleados
     * @param departmentRepository Repositorio para consultas de departamentos
     * @param roleRepository Repositorio para consultas de roles
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
     * Este método implementa el caso de uso de creación de empleados, incluyendo
     * validaciones de negocio, verificación de dependencias y persistencia de datos.
     *
     * @param request DTO con los datos del empleado a crear
     * @return DTO con la información del empleado creado
     * @throws IllegalArgumentException si los datos proporcionados no son válidos
     * @throws BusinessException si no se pueden cumplir las reglas de negocio
     */
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO request) {
        // Validar que el departamento existe
        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new IllegalArgumentException("Departamento no encontrado"));

        // Validar que el rol existe
        Role role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));

        // Verificar que no existe un empleado con el mismo email
        if (employeeRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un empleado con este email");
        }

        // Crear la entidad empleado
        Employee employee = new Employee(
                UUID.randomUUID(),
                request.firstName(),
                request.lastName(),
                request.email(),
                "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.", // password por defecto
                department,
                role,
                request.hireDate() != null ? request.hireDate() : LocalDate.now(),
                request.vacationDays()
        );

        // Persistir el empleado
        Employee savedEmployee = employeeRepository.save(employee);

        // Convertir a DTO de respuesta
        return convertToResponseDTO(savedEmployee, department, role);
    }

    /**
     * Obtiene todos los empleados del sistema.
     *
     * @return Lista de DTOs con la información de todos los empleados
     */
    public List<EmployeeResponseDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un empleado específico por su ID.
     *
     * @param id UUID del empleado a buscar
     * @return DTO con la información del empleado
     * @throws IllegalArgumentException si el empleado no existe
     */
    public EmployeeResponseDTO getEmployeeById(UUID id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));
        return convertToResponseDTO(employee);
    }

    /**
     * Actualiza la información de un empleado existente.
     *
     * @param id UUID del empleado a actualizar
     * @param request DTO con los nuevos datos del empleado
     * @return DTO con la información actualizada del empleado
     * @throws IllegalArgumentException si el empleado no existe o los datos no son válidos
     */
    public EmployeeResponseDTO updateEmployee(UUID id, EmployeeRequestDTO request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));

        // Validar departamento si se proporciona
        Department department = null;
        if (request.departmentId() != null) {
            department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Departamento no encontrado"));
        }

        // Validar rol si se proporciona
        Role role = null;
        if (request.roleId() != null) {
            role = roleRepository.findById(request.roleId())
                    .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));
        }

        // Actualizar campos si se proporcionan
        if (request.firstName() != null) {
            employee.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            employee.setLastName(request.lastName());
        }
        if (request.email() != null) {
            employee.setEmail(request.email());
        }
        if (request.departmentId() != null) {
            employee.setDepartment(department);
        }
        if (request.roleId() != null) {
            employee.setRole(role);
        }
        if (request.hireDate() != null) {
            employee.setHireDate(request.hireDate());
        }
        employee.setVacationDays(request.vacationDays());

        // Persistir cambios
        Employee updatedEmployee = employeeRepository.save(employee);

        // Obtener departamento y rol actualizados
        if (department == null) {
            department = updatedEmployee.getDepartment();
        }
        if (role == null) {
            role = updatedEmployee.getRole();
        }

        return convertToResponseDTO(updatedEmployee, department, role);
    }

    /**
     * Elimina un empleado del sistema.
     *
     * @param id UUID del empleado a eliminar
     * @throws IllegalArgumentException si el empleado no existe
     */
    public void deleteEmployee(UUID id) {
        if (employeeRepository.findById(id).isEmpty()) {
            throw new IllegalArgumentException("Empleado no encontrado");
        }
        employeeRepository.deleteById(id);
    }

    /**
     * Obtiene empleados por departamento.
     *
     * @param departmentId UUID del departamento
     * @return Lista de DTOs con los empleados del departamento
     */
    public List<EmployeeResponseDTO> getEmployeesByDepartment(UUID departmentId) {
        return employeeRepository.findByDepartmentId(departmentId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene empleados por rol.
     *
     * @param roleId UUID del rol
     * @return Lista de DTOs con los empleados del rol
     */
    public List<EmployeeResponseDTO> getEmployeesByRole(UUID roleId) {
        return employeeRepository.findByRoleId(roleId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Permite a un empleado tomar vacaciones.
     *
     * @param id UUID del empleado
     * @param days Número de días de vacaciones a tomar
     * @return DTO con la información actualizada del empleado
     * @throws IllegalArgumentException si el empleado no existe o no tiene suficientes días
     */
    public EmployeeResponseDTO takeVacation(UUID id, int days) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));

        if (employee.getVacationDays() < days) {
            throw new IllegalArgumentException("No tiene suficientes días de vacaciones disponibles");
        }

        employee.setVacationDays(employee.getVacationDays() - days);
        Employee updatedEmployee = employeeRepository.save(employee);

        return convertToResponseDTO(updatedEmployee);
    }

    /**
     * Agrega días de vacaciones a un empleado.
     *
     * @param id UUID del empleado
     * @param days Número de días de vacaciones a agregar
     * @return DTO con la información actualizada del empleado
     * @throws IllegalArgumentException si el empleado no existe
     */
    public EmployeeResponseDTO addVacationDays(UUID id, int days) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));

        employee.setVacationDays(employee.getVacationDays() + days);
        Employee updatedEmployee = employeeRepository.save(employee);

        return convertToResponseDTO(updatedEmployee);
    }

    /**
     * Convierte una entidad Employee a EmployeeResponseDTO.
     *
     * @param employee Entidad empleado
     * @return DTO de respuesta
     */
    private EmployeeResponseDTO convertToResponseDTO(Employee employee) {
        // Obtener departamento y rol directamente de la entidad
        Department department = employee.getDepartment();
        Role role = employee.getRole();

        return convertToResponseDTO(employee, department, role);
    }

    /**
     * Convierte una entidad Employee a EmployeeResponseDTO con departamento y rol.
     *
     * @param employee Entidad empleado
     * @param department Entidad departamento
     * @param role Entidad rol
     * @return DTO de respuesta
     */
    private EmployeeResponseDTO convertToResponseDTO(Employee employee, Department department, Role role) {
        return new EmployeeResponseDTO(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                new DepartmentDTO(department.getId(), department.getName()),
                new RoleDTO(role.getId(), role.getName()),
                employee.getHireDate(),
                employee.getVacationDays()
        );
    }
}