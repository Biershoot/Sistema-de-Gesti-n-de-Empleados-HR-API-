package com.alejandro.microservices.hr_api.infrastructure.persistence.adapter;

import com.alejandro.microservices.hr_api.domain.model.Department;
import com.alejandro.microservices.hr_api.domain.model.Employee;
import com.alejandro.microservices.hr_api.domain.model.Role;
import com.alejandro.microservices.hr_api.infrastructure.persistence.entity.DepartmentEntity;
import com.alejandro.microservices.hr_api.infrastructure.persistence.entity.EmployeeEntity;
import com.alejandro.microservices.hr_api.infrastructure.persistence.entity.RoleEntity;
import com.alejandro.microservices.hr_api.infrastructure.persistence.repository.JpaEmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeRepositoryAdapterTest {

    @Mock
    private JpaEmployeeRepository jpaRepository;

    @InjectMocks
    private EmployeeRepositoryAdapter employeeRepositoryAdapter;

    private Employee employee;
    private EmployeeEntity employeeEntity;
    private Department department;
    private Role role;
    private DepartmentEntity departmentEntity;
    private RoleEntity roleEntity;
    private UUID employeeId;
    private UUID departmentId;
    private UUID roleId;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        departmentId = UUID.randomUUID();
        roleId = UUID.randomUUID();

        department = new Department(departmentId, "IT");
        role = new Role(roleId, "Developer");

        employee = new Employee(
                employeeId,
                "John",
                "Doe",
                "john.doe@example.com",
                department,
                role,
                LocalDate.of(2023, 1, 15),
                15
        );

        departmentEntity = new DepartmentEntity(departmentId, "IT");
        roleEntity = new RoleEntity(roleId, "Developer");

        employeeEntity = new EmployeeEntity(
                employeeId,
                "John",
                "Doe",
                "john.doe@example.com",
                departmentEntity,
                roleEntity,
                LocalDate.of(2023, 1, 15),
                15
        );
    }

    @Test
    void save_ShouldReturnSavedEmployee() {
        // Given
        when(jpaRepository.save(any(EmployeeEntity.class))).thenReturn(employeeEntity);

        // When
        Employee result = employeeRepositoryAdapter.save(employee);

        // Then
        assertNotNull(result);
        assertEquals(employee.getId(), result.getId());
        assertEquals(employee.getFirstName(), result.getFirstName());
        assertEquals(employee.getLastName(), result.getLastName());
        assertEquals(employee.getEmail(), result.getEmail());
        assertEquals(employee.getDepartment().getId(), result.getDepartment().getId());
        assertEquals(employee.getRole().getId(), result.getRole().getId());
        assertEquals(employee.getHireDate(), result.getHireDate());
        assertEquals(employee.getVacationDays(), result.getVacationDays());

        verify(jpaRepository).save(any(EmployeeEntity.class));
    }

    @Test
    void findById_WhenEmployeeExists_ShouldReturnEmployee() {
        // Given
        when(jpaRepository.findById(employeeId)).thenReturn(Optional.of(employeeEntity));

        // When
        Optional<Employee> result = employeeRepositoryAdapter.findById(employeeId);

        // Then
        assertTrue(result.isPresent());
        Employee foundEmployee = result.get();
        assertEquals(employeeId, foundEmployee.getId());
        assertEquals("John", foundEmployee.getFirstName());
        assertEquals("Doe", foundEmployee.getLastName());
        assertEquals("john.doe@example.com", foundEmployee.getEmail());

        verify(jpaRepository).findById(employeeId);
    }

    @Test
    void findById_WhenEmployeeNotExists_ShouldReturnEmpty() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(jpaRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        Optional<Employee> result = employeeRepositoryAdapter.findById(nonExistentId);

        // Then
        assertFalse(result.isPresent());
        verify(jpaRepository).findById(nonExistentId);
    }

    @Test
    void findAll_ShouldReturnAllEmployees() {
        // Given
        EmployeeEntity secondEmployeeEntity = new EmployeeEntity(
                UUID.randomUUID(),
                "Jane",
                "Smith",
                "jane.smith@example.com",
                departmentEntity,
                roleEntity,
                LocalDate.of(2023, 2, 1),
                20
        );

        List<EmployeeEntity> employeeEntities = Arrays.asList(employeeEntity, secondEmployeeEntity);
        when(jpaRepository.findAll()).thenReturn(employeeEntities);

        // When
        List<Employee> result = employeeRepositoryAdapter.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John", result.get(0).getFirstName());
        assertEquals("Jane", result.get(1).getFirstName());

        verify(jpaRepository).findAll();
    }

    @Test
    void deleteById_ShouldCallJpaRepositoryDelete() {
        // When
        employeeRepositoryAdapter.deleteById(employeeId);

        // Then
        verify(jpaRepository).deleteById(employeeId);
    }

    @Test
    void findByDepartmentId_ShouldReturnEmployeesInDepartment() {
        // Given
        List<EmployeeEntity> employeeEntities = Arrays.asList(employeeEntity);
        when(jpaRepository.findByDepartment_Id(departmentId)).thenReturn(employeeEntities);

        // When
        List<Employee> result = employeeRepositoryAdapter.findByDepartmentId(departmentId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(departmentId, result.get(0).getDepartment().getId());

        verify(jpaRepository).findByDepartment_Id(departmentId);
    }

    @Test
    void findByRoleId_ShouldReturnEmployeesWithRole() {
        // Given
        List<EmployeeEntity> employeeEntities = Arrays.asList(employeeEntity);
        when(jpaRepository.findByRole_Id(roleId)).thenReturn(employeeEntities);

        // When
        List<Employee> result = employeeRepositoryAdapter.findByRoleId(roleId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(roleId, result.get(0).getRole().getId());

        verify(jpaRepository).findByRole_Id(roleId);
    }

    @Test
    void mapToEntity_ShouldCorrectlyMapEmployeeToEntity() {
        // Given
        when(jpaRepository.save(any(EmployeeEntity.class))).thenReturn(employeeEntity);

        // When
        Employee result = employeeRepositoryAdapter.save(employee);

        // Then
        verify(jpaRepository).save(argThat(entity ->
            entity.getId().equals(employee.getId()) &&
            entity.getFirstName().equals(employee.getFirstName()) &&
            entity.getLastName().equals(employee.getLastName()) &&
            entity.getEmail().equals(employee.getEmail()) &&
            entity.getDepartment().getId().equals(employee.getDepartment().getId()) &&
            entity.getRole().getId().equals(employee.getRole().getId()) &&
            entity.getHireDate().equals(employee.getHireDate()) &&
            entity.getVacationDays() == employee.getVacationDays()
        ));
    }

    @Test
    void mapToDomain_ShouldCorrectlyMapEntityToEmployee() {
        // Given
        when(jpaRepository.findById(employeeId)).thenReturn(Optional.of(employeeEntity));

        // When
        Optional<Employee> result = employeeRepositoryAdapter.findById(employeeId);

        // Then
        assertTrue(result.isPresent());
        Employee mappedEmployee = result.get();
        assertEquals(employeeEntity.getId(), mappedEmployee.getId());
        assertEquals(employeeEntity.getFirstName(), mappedEmployee.getFirstName());
        assertEquals(employeeEntity.getLastName(), mappedEmployee.getLastName());
        assertEquals(employeeEntity.getEmail(), mappedEmployee.getEmail());
        assertEquals(employeeEntity.getDepartment().getId(), mappedEmployee.getDepartment().getId());
        assertEquals(employeeEntity.getDepartment().getName(), mappedEmployee.getDepartment().getName());
        assertEquals(employeeEntity.getRole().getId(), mappedEmployee.getRole().getId());
        assertEquals(employeeEntity.getRole().getName(), mappedEmployee.getRole().getName());
        assertEquals(employeeEntity.getHireDate(), mappedEmployee.getHireDate());
        assertEquals(employeeEntity.getVacationDays(), mappedEmployee.getVacationDays());
    }
}
