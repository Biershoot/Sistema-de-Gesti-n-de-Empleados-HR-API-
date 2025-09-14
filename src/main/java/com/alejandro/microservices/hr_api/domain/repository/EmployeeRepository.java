package com.alejandro.microservices.hr_api.domain.repository;

import com.alejandro.microservices.hr_api.domain.model.Employee;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository {

    Employee save(Employee employee);

    Optional<Employee> findById(UUID id);

    List<Employee> findAll();

    void deleteById(UUID id);

    List<Employee> findByDepartmentId(UUID departmentId);

    List<Employee> findByRoleId(UUID roleId);

    Optional<Employee> findByEmail(String email);
}
