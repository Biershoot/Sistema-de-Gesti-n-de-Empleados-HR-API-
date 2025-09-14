package com.alejandro.microservices.hr_api.domain.model;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Entidad User para el sistema de autenticación JWT.
 *
 * Esta entidad representa a los usuarios del sistema que pueden autenticarse
 * y acceder a las funcionalidades de la API HR según sus roles asignados.
 *
 * Características:
 * - UUID como identificador único
 * - Username único para identificación
 * - Password hasheado con BCrypt
 * - Role basado en cadenas (ROLE_ADMIN, ROLE_USER, etc.)
 *
 * @author Sistema HR API
 * @version 1.0
 * @since 2025-01-14
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String role;

    @Column(nullable = false)
    private boolean enabled = true;

    /**
     * Constructor por defecto requerido por JPA.
     */
    public User() {}

    /**
     * Constructor para crear un nuevo usuario.
     *
     * @param username Nombre de usuario único
     * @param password Contraseña (será hasheada antes de persistir)
     * @param role Rol del usuario (ej: ROLE_ADMIN, ROLE_USER)
     */
    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.enabled = true;
    }

    /**
     * Constructor completo con estado habilitado.
     *
     * @param username Nombre de usuario único
     * @param password Contraseña hasheada
     * @param role Rol del usuario
     * @param enabled Estado del usuario (habilitado/deshabilitado)
     */
    public User(String username, String password, String role, boolean enabled) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.enabled = enabled;
    }

    // Getters y Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", enabled=" + enabled +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
