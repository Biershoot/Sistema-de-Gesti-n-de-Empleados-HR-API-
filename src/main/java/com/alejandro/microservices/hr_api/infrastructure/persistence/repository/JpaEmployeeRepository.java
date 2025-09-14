package com.alejandro.microservices.hr_api.infrastructure.persistence.repository;

import com.alejandro.microservices.hr_api.infrastructure.persistence.entity.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaEmployeeRepository extends JpaRepository<EmployeeEntity, UUID> {

    List<EmployeeEntity> findByDepartment_Id(UUID departmentId);

    List<EmployeeEntity> findByRole_Id(UUID roleId);
}
