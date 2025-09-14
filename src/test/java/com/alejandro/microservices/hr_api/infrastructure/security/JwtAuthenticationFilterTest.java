package com.alejandro.microservices.hr_api.infrastructure.security;

import com.alejandro.microservices.hr_api.domain.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para JwtAuthenticationFilter.
 *
 * Verifica el correcto funcionamiento del filtro de autenticación JWT,
 * incluyendo extracción de tokens, validación y configuración del contexto de seguridad.
 *
 * @author Sistema HR API
 * @version 1.0
 * @since 2025-01-14
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private User testUser;
    private UserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService, userDetailsService);

        // Configurar usuario de prueba
        testUser = new User("testuser", "hashedPassword", "ROLE_USER");
        testUser.setEnabled(true);

        testUserDetails = new CustomUserDetailsService.CustomUserPrincipal(testUser);

        // Configurar SecurityContext mock
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void doFilterInternal_ShouldSkipAuthentication_WhenNoAuthorizationHeader() throws ServletException, IOException {
        // ARRANGE
        when(request.getHeader("Authorization")).thenReturn(null);

        // ACT
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // ASSERT
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void doFilterInternal_ShouldSkipAuthentication_WhenAuthorizationHeaderDoesNotStartWithBearer() throws ServletException, IOException {
        // ARRANGE
        when(request.getHeader("Authorization")).thenReturn("Basic sometoken");

        // ACT
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // ASSERT
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void doFilterInternal_ShouldSkipAuthentication_WhenUserAlreadyAuthenticated() throws ServletException, IOException {
        // ARRANGE
        String token = "Bearer valid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn(token);
        when(securityContext.getAuthentication()).thenReturn(mock(UsernamePasswordAuthenticationToken.class));

        // ACT
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // ASSERT
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void doFilterInternal_ShouldAuthenticateUser_WhenValidTokenProvided() throws ServletException, IOException {
        // ARRANGE
        String token = "Bearer valid.jwt.token";
        String username = "testuser";

        when(request.getHeader("Authorization")).thenReturn(token);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(jwtService.extractUsername("valid.jwt.token")).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(testUserDetails);
        when(jwtService.isTokenValid("valid.jwt.token", username)).thenReturn(true);

        // ACT
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // ASSERT
        verify(jwtService).extractUsername("valid.jwt.token");
        verify(userDetailsService).loadUserByUsername(username);
        verify(jwtService).isTokenValid("valid.jwt.token", username);
        verify(securityContext).setAuthentication(any(UsernamePasswordAuthenticationToken.class));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldNotAuthenticate_WhenTokenIsInvalid() throws ServletException, IOException {
        // ARRANGE
        String token = "Bearer invalid.jwt.token";
        String username = "testuser";

        when(request.getHeader("Authorization")).thenReturn(token);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(jwtService.extractUsername("invalid.jwt.token")).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(testUserDetails);
        when(jwtService.isTokenValid("invalid.jwt.token", username)).thenReturn(false);

        // ACT
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // ASSERT
        verify(jwtService).extractUsername("invalid.jwt.token");
        verify(userDetailsService).loadUserByUsername(username);
        verify(jwtService).isTokenValid("invalid.jwt.token", username);
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldHandleException_WhenJwtServiceThrowsException() throws ServletException, IOException {
        // ARRANGE
        String token = "Bearer malformed.jwt.token";

        when(request.getHeader("Authorization")).thenReturn(token);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(jwtService.extractUsername("malformed.jwt.token")).thenThrow(new RuntimeException("Token malformed"));

        // ACT & ASSERT
        assertDoesNotThrow(() -> {
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        }, "Filter no debe propagar excepciones de JWT");

        verify(filterChain).doFilter(request, response);
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    void doFilterInternal_ShouldHandleException_WhenUserDetailsServiceThrowsException() throws ServletException, IOException {
        // ARRANGE
        String token = "Bearer valid.jwt.token";
        String username = "nonexistentuser";

        when(request.getHeader("Authorization")).thenReturn(token);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(jwtService.extractUsername("valid.jwt.token")).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenThrow(new RuntimeException("User not found"));

        // ACT & ASSERT
        assertDoesNotThrow(() -> {
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        }, "Filter no debe propagar excepciones de UserDetailsService");

        verify(filterChain).doFilter(request, response);
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    void doFilterInternal_ShouldExtractTokenCorrectly_WhenAuthorizationHeaderHasExtraSpaces() throws ServletException, IOException {
        // ARRANGE
        String token = "Bearer  valid.jwt.token  "; // Espacios extra
        String username = "testuser";

        when(request.getHeader("Authorization")).thenReturn(token);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(jwtService.extractUsername("valid.jwt.token")).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(testUserDetails);
        when(jwtService.isTokenValid("valid.jwt.token", username)).thenReturn(true);

        // ACT
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // ASSERT
        verify(jwtService).extractUsername("valid.jwt.token");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldSetCorrectAuthenticationDetails() throws ServletException, IOException {
        // ARRANGE
        String token = "Bearer valid.jwt.token";
        String username = "testuser";

        when(request.getHeader("Authorization")).thenReturn(token);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(jwtService.extractUsername("valid.jwt.token")).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(testUserDetails);
        when(jwtService.isTokenValid("valid.jwt.token", username)).thenReturn(true);

        // ACT
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // ASSERT
        verify(securityContext).setAuthentication(argThat(auth -> {
            if (!(auth instanceof UsernamePasswordAuthenticationToken)) return false;

            UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken) auth;
            return testUserDetails.equals(authToken.getPrincipal()) &&
                   authToken.getCredentials() == null &&
                   authToken.getAuthorities().equals(testUserDetails.getAuthorities());
        }));
    }

    @Test
    void doFilterInternal_ShouldHandleCaseSensitiveTokens() throws ServletException, IOException {
        // ARRANGE
        String token = "bearer valid.jwt.token"; // Lowercase 'bearer'

        when(request.getHeader("Authorization")).thenReturn(token);

        // ACT
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // ASSERT
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void doFilterInternal_ShouldHandleEmptyToken() throws ServletException, IOException {
        // ARRANGE
        String token = "Bearer ";

        when(request.getHeader("Authorization")).thenReturn(token);
        when(securityContext.getAuthentication()).thenReturn(null);

        // ACT
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // ASSERT
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void doFilterInternal_ShouldHandleNullUsername() throws ServletException, IOException {
        // ARRANGE
        String token = "Bearer valid.jwt.token";

        when(request.getHeader("Authorization")).thenReturn(token);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(jwtService.extractUsername("valid.jwt.token")).thenReturn(null);

        // ACT
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // ASSERT
        verify(jwtService).extractUsername("valid.jwt.token");
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userDetailsService);
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    void doFilterInternal_ShouldHandleEmptyUsername() throws ServletException, IOException {
        // ARRANGE
        String token = "Bearer valid.jwt.token";

        when(request.getHeader("Authorization")).thenReturn(token);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(jwtService.extractUsername("valid.jwt.token")).thenReturn("");

        // ACT
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // ASSERT
        verify(jwtService).extractUsername("valid.jwt.token");
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userDetailsService);
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    void doFilterInternal_ShouldNotOverrideExistingAuthentication() throws ServletException, IOException {
        // ARRANGE
        String token = "Bearer valid.jwt.token";
        UsernamePasswordAuthenticationToken existingAuth = new UsernamePasswordAuthenticationToken(
            "existinguser", null, testUserDetails.getAuthorities()
        );

        when(request.getHeader("Authorization")).thenReturn(token);
        when(securityContext.getAuthentication()).thenReturn(existingAuth);

        // ACT
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // ASSERT
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(userDetailsService);
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    void doFilterInternal_ShouldContinueFilterChain_InAllScenarios() throws ServletException, IOException {
        // ARRANGE - Test multiple scenarios
        String[] tokens = {
            null,
            "Basic token",
            "Bearer valid.token",
            "Bearer invalid.token",
            "Bearer "
        };

        for (String token : tokens) {
            reset(request, response, filterChain, jwtService, userDetailsService, securityContext);
            SecurityContextHolder.setContext(securityContext);

            when(request.getHeader("Authorization")).thenReturn(token);
            when(securityContext.getAuthentication()).thenReturn(null);

            if (token != null && token.startsWith("Bearer ") && token.length() > 7) {
                String jwtToken = token.substring(7);
                when(jwtService.extractUsername(jwtToken)).thenReturn("user");
                when(userDetailsService.loadUserByUsername("user")).thenReturn(testUserDetails);
                when(jwtService.isTokenValid(jwtToken, "user")).thenReturn(token.contains("valid"));
            }

            // ACT
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // ASSERT
            verify(filterChain).doFilter(request, response);
        }
    }
}
