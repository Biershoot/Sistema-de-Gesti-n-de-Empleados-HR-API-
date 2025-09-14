package com.alejandro.microservices.hr_api.infrastructure.security;

import com.alejandro.microservices.hr_api.domain.model.Employee;
import com.alejandro.microservices.hr_api.domain.repository.EmployeeRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * Servicio personalizado para cargar detalles de usuario desde la base de datos.
 *
 * Implementa UserDetailsService de Spring Security para integrar
 * la autenticación con nuestro dominio de empleados.
 *
 * Funcionalidades:
 * - Carga empleados por email (username)
 * - Asigna roles basados en el rol del empleado
 * - Maneja autoridades de Spring Security
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    public CustomUserDetailsService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    /**
     * Carga un usuario por su email (username) desde la base de datos.
     *
     * @param username Email del empleado
     * @return UserDetails con información del usuario y sus roles
     * @throws UsernameNotFoundException Si el usuario no existe
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Employee employee = employeeRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        return User.builder()
                .username(employee.getEmail())
                .password(getPasswordForEmployee(employee)) // Temporal - necesitará campo password
                .authorities(getAuthorities(employee))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    /**
     * Obtiene las autoridades (roles) del empleado.
     *
     * @param employee Empleado del dominio
     * @return Lista de autoridades de Spring Security
     */
    private Collection<? extends GrantedAuthority> getAuthorities(Employee employee) {
        String roleName = employee.getRole().getName().toUpperCase();

        // Mapear roles del dominio a roles de Spring Security
        return switch (roleName) {
            case "ADMIN", "ADMINISTRADOR" -> List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_USER")
            );
            case "HR", "RECURSOS_HUMANOS" -> List.of(
                new SimpleGrantedAuthority("ROLE_HR"),
                new SimpleGrantedAuthority("ROLE_USER")
            );
            case "MANAGER", "GERENTE" -> List.of(
                new SimpleGrantedAuthority("ROLE_MANAGER"),
                new SimpleGrantedAuthority("ROLE_USER")
            );
            default -> List.of(new SimpleGrantedAuthority("ROLE_USER"));
        };
    }

    /**
     * Obtiene la contraseña del empleado desde la entidad.
     *
     * @param employee Empleado
     * @return Contraseña encriptada del empleado
     */
    private String getPasswordForEmployee(Employee employee) {
        return employee.getPassword();
    }
}
