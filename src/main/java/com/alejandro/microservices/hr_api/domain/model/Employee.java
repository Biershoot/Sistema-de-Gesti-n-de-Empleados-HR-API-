package com.alejandro.microservices.hr_api.domain.model;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidad de dominio que representa un empleado en el sistema de recursos humanos.
 * Esta clase implementa la lógica de negocio relacionada con los empleados,
 * incluyendo la gestión de días de vacaciones.
 *
 * Siguiendo los principios de Domain Driven Design (DDD), esta entidad
 * encapsula tanto los datos como el comportamiento del empleado.
 *
 * @author Sistema HR API
 * @version 1.0
 */
public class Employee {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String password; // Campo agregado para autenticación
    private Department department;
    private Role role;
    private LocalDate hireDate;
    private int vacationDays;

    /**
     * Constructor para crear una nueva instancia de Employee.
     *
     * @param id Identificador único del empleado
     * @param firstName Nombre del empleado
     * @param lastName Apellido del empleado
     * @param email Correo electrónico del empleado (debe ser único)
     * @param password Contraseña encriptada del empleado
     * @param department Departamento al que pertenece el empleado
     * @param role Rol o posición del empleado
     * @param hireDate Fecha de contratación
     * @param vacationDays Días de vacaciones disponibles
     */
    public Employee(UUID id, String firstName, String lastName, String email, String password,
                    Department department, Role role, LocalDate hireDate, int vacationDays) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
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

    public String getPassword() {
        return password;
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

    // Setters necesarios para testing y actualizaciones
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

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public void setVacationDays(int vacationDays) {
        this.vacationDays = vacationDays;
    }

    /**
     * Verifica si el empleado está activo.
     * Un empleado está activo si tiene un departamento y role asignados.
     *
     * @return true si el empleado está activo
     */
    public boolean isActive() {
        return this.department != null && this.role != null;
    }

    /**
     * Establece el estado activo del empleado.
     * Para simplificar las pruebas, agregamos este método.
     *
     * @param active estado activo
     */
    public void setActive(boolean active) {
        // En una implementación real, esto podría manejar la lógica de negocio
        // Por ahora, para las pruebas, no hacemos nada específico
    }

    /**
     * Método de negocio para tomar días de vacaciones.
     * Aplica las reglas de negocio para la gestión de vacaciones.
     *
     * @param days Número de días de vacaciones a tomar
     * @throws IllegalArgumentException si los días son <= 0 o exceden los días disponibles
     */
    public void takeVacation(int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("Los días deben ser positivos");
        }
        if (days > vacationDays) {
            throw new IllegalArgumentException("No tiene suficientes días de vacaciones");
        }
        this.vacationDays -= days;
    }

    /**
     * Agrega días de vacaciones al empleado.
     * Típicamente usado para el acumulado anual o ajustes administrativos.
     *
     * @param days Número de días a agregar (debe ser positivo)
     */
    public void addVacationDays(int days) {
        if (days > 0) {
            this.vacationDays += days;
        }
    }
}
