package com.alejandro.microservices.hr_api.domain.model;

import java.time.LocalDate;
import java.util.UUID;

public class Employee {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private Department department;
    private Role role;
    private LocalDate hireDate;
    private int vacationDays;

    public Employee(UUID id, String firstName, String lastName, String email,
                    Department department, Role role, LocalDate hireDate, int vacationDays) {
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

    public Department getDepartment() {
        return department;
    }

    public Role getRole() {
        return role;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public int getVacationDays() {
        return vacationDays;
    }

    // Métodos de negocio
    public void takeVacation(int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("Los días deben ser positivos");
        }
        if (days > vacationDays) {
            throw new IllegalArgumentException("No tiene suficientes días de vacaciones");
        }
        this.vacationDays -= days;
    }

    public void addVacationDays(int days) {
        if (days > 0) {
            this.vacationDays += days;
        }
    }
}
