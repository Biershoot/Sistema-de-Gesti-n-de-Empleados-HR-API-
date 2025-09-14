package com.alejandro.microservices.hr_api.infrastructure.persistence.adapter;

import com.alejandro.microservices.hr_api.domain.model.Employee;
import com.alejandro.microservices.hr_api.domain.model.Department;
import com.alejandro.microservices.hr_api.domain.model.Role;
import com.alejandro.microservices.hr_api.domain.repository.EmployeeRepository;
import com.alejandro.microservices.hr_api.infrastructure.persistence.entity.EmployeeEntity;
import com.alejandro.microservices.hr_api.infrastructure.persistence.entity.DepartmentEntity;
import com.alejandro.microservices.hr_api.infrastructure.persistence.entity.RoleEntity;
import com.alejandro.microservices.hr_api.infrastructure.persistence.repository.JpaEmployeeRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class EmployeeRepositoryAdapter implements EmployeeRepository {

    private final JpaEmployeeRepository jpaRepository;

    public EmployeeRepositoryAdapter(JpaEmployeeRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Employee save(Employee employee) {
        EmployeeEntity entity = mapToEntity(employee);
        EmployeeEntity saved = jpaRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<Employee> findById(UUID id) {
        return jpaRepository.findById(id).map(this::mapToDomain);
    }

    @Override
    public List<Employee> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<Employee> findByDepartmentId(UUID departmentId) {
        return jpaRepository.findByDepartment_Id(departmentId)
                .stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Employee> findByRoleId(UUID roleId) {
        return jpaRepository.findByRole_Id(roleId)
                .stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    // Métodos de mapeo
    private EmployeeEntity mapToEntity(Employee employee) {
        DepartmentEntity departmentEntity = new DepartmentEntity(
                employee.getDepartment().getId(),
                employee.getDepartment().getName()
        );

        RoleEntity roleEntity = new RoleEntity(
                employee.getRole().getId(),
                employee.getRole().getName()
        );

        return new EmployeeEntity(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                departmentEntity,
                roleEntity,
                employee.getHireDate(),
                employee.getVacationDays()
        );
    }

    private Employee mapToDomain(EmployeeEntity entity) {
        Department department = new Department(
                entity.getDepartment().getId(),
                entity.getDepartment().getName()
        );

        Role role = new Role(
                entity.getRole().getId(),
                entity.getRole().getName()
        );

        return new Employee(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                department,
                role,
                entity.getHireDate(),
                entity.getVacationDays()
        );
    }
}
