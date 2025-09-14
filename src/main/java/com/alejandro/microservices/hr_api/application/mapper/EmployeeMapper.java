package com.alejandro.microservices.hr_api.application.mapper;

import com.alejandro.microservices.hr_api.application.dto.DepartmentDTO;
import com.alejandro.microservices.hr_api.application.dto.EmployeeRequestDTO;
import com.alejandro.microservices.hr_api.application.dto.EmployeeResponseDTO;
import com.alejandro.microservices.hr_api.application.dto.RoleDTO;
import com.alejandro.microservices.hr_api.domain.model.Department;
import com.alejandro.microservices.hr_api.domain.model.Employee;
import com.alejandro.microservices.hr_api.domain.model.Role;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre entidades Employee y DTOs.
 *
 * Proporciona métodos de conversión bidireccional entre:
 * - Employee ↔ EmployeeResponseDTO
 * - EmployeeRequestDTO → Employee
 *
 * @author Sistema HR API
 * @version 1.0
 * @since 2025-01-14
 */
@Component
public class EmployeeMapper {

    /**
     * Convierte una entidad Employee a EmployeeResponseDTO.
     *
     * @param employee Entidad Employee a convertir
     * @return EmployeeResponseDTO o null si employee es null
     */
    public EmployeeResponseDTO toResponseDTO(Employee employee) {
        if (employee == null) {
            return null;
        }

        // Mapear Department
        DepartmentDTO departmentDTO = null;
        if (employee.getDepartment() != null) {
            departmentDTO = new DepartmentDTO(
                employee.getDepartment().getId(),
                employee.getDepartment().getName()
            );
        }

        // Mapear Role
        RoleDTO roleDTO = null;
        if (employee.getRole() != null) {
            roleDTO = new RoleDTO(
                employee.getRole().getId(),
                employee.getRole().getName()
            );
        }

        // Crear EmployeeResponseDTO usando el constructor del record
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

    /**
     * Convierte una lista de Employee a lista de EmployeeResponseDTO.
     *
     * @param employees Lista de empleados
     * @return Lista de DTOs o null si la lista es null
     */
    public List<EmployeeResponseDTO> toResponseDTOList(List<Employee> employees) {
        if (employees == null) {
            return null;
        }

        return employees.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convierte un EmployeeRequestDTO a entidad Employee.
     *
     * @param requestDTO DTO con datos del empleado
     * @param department Departamento asignado
     * @param role Role asignado
     * @return Nueva entidad Employee o null si requestDTO es null
     */
    public Employee toEntity(EmployeeRequestDTO requestDTO, Department department, Role role) {
        if (requestDTO == null) {
            return null;
        }

        return new Employee(
            null, // ID será generado por la base de datos
            requestDTO.firstName(),
            requestDTO.lastName(),
            requestDTO.email(),
            null, // Password será asignado por el servicio
            department,
            role,
            requestDTO.hireDate(),
            requestDTO.vacationDays() // Usar vacationDays del record
        );
    }

    /**
     * Actualiza una entidad Employee existente con datos de un EmployeeRequestDTO.
     *
     * @param employee Entidad existente a actualizar
     * @param requestDTO DTO con nuevos datos
     * @param department Nuevo departamento
     * @param role Nuevo role
     */
    public void updateEntity(Employee employee, EmployeeRequestDTO requestDTO,
                           Department department, Role role) {
        if (employee == null || requestDTO == null) {
            return;
        }

        // Actualizar campos usando métodos setter y syntax de record
        if (requestDTO.firstName() != null) {
            employee.setFirstName(requestDTO.firstName());
        }
        if (requestDTO.lastName() != null) {
            employee.setLastName(requestDTO.lastName());
        }
        if (requestDTO.email() != null) {
            employee.setEmail(requestDTO.email());
        }
        if (requestDTO.hireDate() != null) {
            employee.setHireDate(requestDTO.hireDate());
        }
        // Actualizar vacation days
        employee.setVacationDays(requestDTO.vacationDays());

        if (department != null) {
            employee.setDepartment(department);
        }
        if (role != null) {
            employee.setRole(role);
        }
    }
}
