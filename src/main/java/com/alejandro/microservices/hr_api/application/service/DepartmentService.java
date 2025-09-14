package com.alejandro.microservices.hr_api.application.service;

import com.alejandro.microservices.hr_api.application.dto.DepartmentDTO;
import com.alejandro.microservices.hr_api.domain.model.Department;
import com.alejandro.microservices.hr_api.domain.repository.DepartmentRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public DepartmentDTO createDepartment(String name) {
        validateDepartmentName(name);

        Department department = new Department(UUID.randomUUID(), name.trim());
        Department savedDepartment = departmentRepository.save(department);
        return mapToDTO(savedDepartment);
    }

    public List<DepartmentDTO> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public DepartmentDTO getDepartmentById(UUID id) {
        validateId(id, "El ID del departamento no puede ser nulo");

        return departmentRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new IllegalArgumentException("Departamento no encontrado"));
    }

    public DepartmentDTO updateDepartment(UUID id, String name) {
        validateId(id, "El ID del departamento no puede ser nulo");
        validateDepartmentName(name);

        Department existingDepartment = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Departamento no encontrado"));

        Department updatedDepartment = new Department(id, name.trim());
        Department savedDepartment = departmentRepository.save(updatedDepartment);
        return mapToDTO(savedDepartment);
    }

    public void deleteDepartment(UUID id) {
        validateId(id, "El ID del departamento no puede ser nulo");

        if (!departmentRepository.findById(id).isPresent()) {
            throw new IllegalArgumentException("Departamento no encontrado");
        }
        departmentRepository.deleteById(id);
    }

    public DepartmentDTO findByName(String name) {
        validateDepartmentName(name);

        return departmentRepository.findByName(name.trim())
                .map(this::mapToDTO)
                .orElseThrow(() -> new IllegalArgumentException("Departamento no encontrado"));
    }

    // Métodos de validación privados
    private void validateDepartmentName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del departamento no puede estar vacío");
        }
        if (name.trim().length() < 2) {
            throw new IllegalArgumentException("El nombre del departamento debe tener al menos 2 caracteres");
        }
        if (name.trim().length() > 100) {
            throw new IllegalArgumentException("El nombre del departamento no puede tener más de 100 caracteres");
        }
    }

    private void validateId(UUID id, String message) {
        if (id == null) {
            throw new IllegalArgumentException(message);
        }
    }

    private DepartmentDTO mapToDTO(Department department) {
        return new DepartmentDTO(
                department.getId(),
                department.getName()
        );
    }
}
