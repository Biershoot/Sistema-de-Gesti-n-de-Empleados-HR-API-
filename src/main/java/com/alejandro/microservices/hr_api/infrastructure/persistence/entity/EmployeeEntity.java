package com.alejandro.microservices.hr_api.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employees")
public class EmployeeEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private DepartmentEntity department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;

    @Column(nullable = false)
    private LocalDate hireDate;

    @Column(nullable = false)
    private int vacationDays;

    // Constructor por defecto requerido por JPA
    public EmployeeEntity() {}

    public EmployeeEntity(UUID id, String firstName, String lastName, String email,
                          DepartmentEntity department, RoleEntity role,
                          LocalDate hireDate, int vacationDays) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.department = department;
        this.role = role;
        this.hireDate = hireDate;
        this.vacationDays = vacationDays;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public DepartmentEntity getDepartment() {
        return department;
    }

    public RoleEntity getRole() {
        return role;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public int getVacationDays() {
        return vacationDays;
    }

    // Setters (necesarios para JPA)
    public void setId(UUID id) {
        this.id = id;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDepartment(DepartmentEntity department) {
        this.department = department;
    }

    public void setRole(RoleEntity role) {
        this.role = role;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public void setVacationDays(int vacationDays) {
        this.vacationDays = vacationDays;
    }
}
