package com.alejandro.microservices.hr_api.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración principal de seguridad para la aplicación HR API.
 *
 * Funcionalidades implementadas:
 * - Autenticación basada en JWT (sin sesiones)
 * - Autorización por roles (ADMIN, USER)
 * - Endpoints públicos para autenticación
 * - Protección de endpoints administrativos
 *
 * Rutas configuradas:
 * - /auth/** : Público (login, registro)
 * - /api/reports/** : Solo ADMIN
 * - /api/** : Usuarios autenticados
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                         UserDetailsService userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Configuración principal de la cadena de filtros de seguridad.
     *
     * @param http Configurador de seguridad HTTP
     * @return Cadena de filtros configurada
     * @throws Exception Error en configuración
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Deshabilitar CSRF para APIs REST
            .csrf(csrf -> csrf.disable())

            // Configurar autorización de rutas
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll() // Para testing

                // Rutas administrativas
                .requestMatchers("/api/reports/**").hasRole("ADMIN")
                .requestMatchers("/api/employees/*/vacation").hasAnyRole("ADMIN", "HR")

                // Todas las demás rutas requieren autenticación
                .anyRequest().authenticated()
            )

            // Configurar manejo de sesiones (stateless para JWT)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Agregar filtro JWT antes del filtro de autenticación estándar
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Proveedor de autenticación que utiliza UserDetailsService y PasswordEncoder.
     *
     * @return Proveedor de autenticación configurado
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Gestor de autenticación principal.
     *
     * @param config Configuración de autenticación
     * @return Gestor de autenticación
     * @throws Exception Error en configuración
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Codificador de contraseñas utilizando BCrypt.
     *
     * @return Codificador de contraseñas seguro
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
