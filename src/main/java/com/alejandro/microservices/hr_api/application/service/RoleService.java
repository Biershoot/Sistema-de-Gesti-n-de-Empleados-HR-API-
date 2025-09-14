package com.alejandro.microservices.hr_api.application.service;

import com.alejandro.microservices.hr_api.application.dto.RoleDTO;
import com.alejandro.microservices.hr_api.domain.model.Role;
import com.alejandro.microservices.hr_api.domain.repository.RoleRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public RoleDTO createRole(String name) {
        validateRoleName(name);

        Role role = new Role(UUID.randomUUID(), name.trim());
        Role savedRole = roleRepository.save(role);
        return mapToDTO(savedRole);
    }

    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public RoleDTO getRoleById(UUID id) {
        validateId(id, "El ID del rol no puede ser nulo");

        return roleRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));
    }

    public RoleDTO updateRole(UUID id, String name) {
        validateId(id, "El ID del rol no puede ser nulo");
        validateRoleName(name);

        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));

        Role updatedRole = new Role(id, name.trim());
        Role savedRole = roleRepository.save(updatedRole);
        return mapToDTO(savedRole);
    }

    public void deleteRole(UUID id) {
        validateId(id, "El ID del rol no puede ser nulo");

        if (!roleRepository.findById(id).isPresent()) {
            throw new IllegalArgumentException("Rol no encontrado");
        }
        roleRepository.deleteById(id);
    }

    public RoleDTO findByName(String name) {
        validateRoleName(name);

        return roleRepository.findByName(name.trim())
                .map(this::mapToDTO)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));
    }

    // Métodos de validación privados
    private void validateRoleName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del rol no puede estar vacío");
        }
        if (name.trim().length() < 2) {
            throw new IllegalArgumentException("El nombre del rol debe tener al menos 2 caracteres");
        }
        if (name.trim().length() > 100) {
            throw new IllegalArgumentException("El nombre del rol no puede tener más de 100 caracteres");
        }
    }

    private void validateId(UUID id, String message) {
        if (id == null) {
            throw new IllegalArgumentException(message);
        }
    }

    private RoleDTO mapToDTO(Role role) {
        return new RoleDTO(
                role.getId(),
                role.getName()
        );
    }
}
