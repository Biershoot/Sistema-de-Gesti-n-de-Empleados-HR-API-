package com.alejandro.microservices.hr_api.infrastructure.persistence.adapter;

import com.alejandro.microservices.hr_api.domain.model.Department;
import com.alejandro.microservices.hr_api.domain.repository.DepartmentRepository;
import com.alejandro.microservices.hr_api.infrastructure.persistence.entity.DepartmentEntity;
import com.alejandro.microservices.hr_api.infrastructure.persistence.repository.JpaDepartmentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class DepartmentRepositoryAdapter implements DepartmentRepository {

    private final JpaDepartmentRepository jpaRepository;

    public DepartmentRepositoryAdapter(JpaDepartmentRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Department save(Department department) {
        DepartmentEntity entity = mapToEntity(department);
        DepartmentEntity saved = jpaRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<Department> findById(UUID id) {
        return jpaRepository.findById(id).map(this::mapToDomain);
    }

    @Override
    public List<Department> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public Optional<Department> findByName(String name) {
        return jpaRepository.findByName(name).map(this::mapToDomain);
    }

    // Métodos de mapeo
    private DepartmentEntity mapToEntity(Department department) {
        return new DepartmentEntity(
                department.getId(),
                department.getName()
        );
    }

    private Department mapToDomain(DepartmentEntity entity) {
        return new Department(
                entity.getId(),
                entity.getName()
        );
    }
}
