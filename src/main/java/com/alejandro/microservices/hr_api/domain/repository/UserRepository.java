package com.alejandro.microservices.hr_api.domain.repository;

import com.alejandro.microservices.hr_api.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para la entidad User.
 *
 * Proporciona operaciones de acceso a datos para los usuarios del sistema,
 * incluyendo búsquedas por username y verificaciones de existencia.
 *
 * @author Sistema HR API
 * @version 1.0
 * @since 2025-01-14
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Busca un usuario por su nombre de usuario.
     *
     * @param username Nombre de usuario a buscar
     * @return Optional que contiene el usuario si existe
     */
    Optional<User> findByUsername(String username);

    /**
     * Busca un usuario por su nombre de usuario y que esté habilitado.
     *
     * @param username Nombre de usuario a buscar
     * @return Optional que contiene el usuario si existe y está habilitado
     */
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.enabled = true")
    Optional<User> findByUsernameAndEnabled(@Param("username") String username);

    /**
     * Verifica si existe un usuario con el nombre de usuario dado.
     *
     * @param username Nombre de usuario a verificar
     * @return true si existe un usuario con ese username
     */
    boolean existsByUsername(String username);

    /**
     * Busca usuarios por role.
     *
     * @param role Role a buscar
     * @return Lista de usuarios con el role especificado
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.enabled = true")
    java.util.List<User> findByRoleAndEnabled(@Param("role") String role);

    /**
     * Cuenta usuarios habilitados por role.
     *
     * @param role Role a contar
     * @return Número de usuarios habilitados con el role especificado
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.enabled = true")
    long countByRoleAndEnabled(@Param("role") String role);
}
