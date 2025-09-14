package com.alejandro.microservices.hr_api.infrastructure.persistence.adapter;

import com.alejandro.microservices.hr_api.domain.model.Role;
import com.alejandro.microservices.hr_api.domain.repository.RoleRepository;
import com.alejandro.microservices.hr_api.infrastructure.persistence.entity.RoleEntity;
import com.alejandro.microservices.hr_api.infrastructure.persistence.repository.JpaRoleRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class RoleRepositoryAdapter implements RoleRepository {

    private final JpaRoleRepository jpaRepository;

    public RoleRepositoryAdapter(JpaRoleRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Role save(Role role) {
        RoleEntity entity = mapToEntity(role);
        RoleEntity saved = jpaRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<Role> findById(UUID id) {
        return jpaRepository.findById(id).map(this::mapToDomain);
    }

    @Override
    public List<Role> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public Optional<Role> findByName(String name) {
        return jpaRepository.findByName(name).map(this::mapToDomain);
    }

    // Métodos de mapeo
    private RoleEntity mapToEntity(Role role) {
        return new RoleEntity(
                role.getId(),
                role.getName()
        );
    }

    private Role mapToDomain(RoleEntity entity) {
        return new Role(
                entity.getId(),
                entity.getName()
        );
    }
}
