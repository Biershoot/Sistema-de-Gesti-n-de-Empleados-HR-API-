package com.alejandro.microservices.hr_api.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de autenticación JWT.
 *
 * Este filtro intercepta todas las peticiones HTTP para:
 * - Extraer y validar tokens JWT del header Authorization
 * - Autenticar automáticamente usuarios con tokens válidos
 * - Establecer el contexto de seguridad de Spring Security
 *
 * Se ejecuta una vez por petición y permite el acceso sin token
 * a endpoints públicos configurados en SecurityConfig.
 *
 * @author Sistema HR API
 * @version 1.0
 * @since 2025-01-14
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Extraer el header Authorization
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // Verificar si el header existe y tiene el formato correcto
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraer el token JWT (remover "Bearer ")
        jwt = authHeader.substring(7).trim();

        // Verificar que el token no esté vacío
        if (jwt.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extraer username del token
            username = jwtService.extractUsername(jwt);

            // Si el username existe y no hay autenticación previa
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Cargar detalles del usuario
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // Validar el token
                if (jwtService.isTokenValid(jwt, userDetails.getUsername())) {

                    // Crear token de autenticación para Spring Security
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    // Establecer detalles adicionales de la petición
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // Establecer la autenticación en el contexto de seguridad
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Log del error sin interrumpir el filtro
            logger.error("Error procesando token JWT: " + e.getMessage());
        }

        // Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }
}
