package com.alejandro.microservices.hr_api.application.service;

import com.alejandro.microservices.hr_api.application.dto.AuthResponseDTO;
import com.alejandro.microservices.hr_api.application.dto.LoginRequestDTO;
import com.alejandro.microservices.hr_api.domain.model.Department;
import com.alejandro.microservices.hr_api.domain.model.Employee;
import com.alejandro.microservices.hr_api.domain.model.Role;
import com.alejandro.microservices.hr_api.domain.repository.EmployeeRepository;
import com.alejandro.microservices.hr_api.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para AuthService.
 *
 * Valida la autenticación de usuarios y generación de respuestas JWT.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private Authentication authentication;

    private AuthService authService;
    private Employee testEmployee;
    private Department testDepartment;
    private Role testRole;

    @BeforeEach
    void setUp() {
        authService = new AuthService(authenticationManager, jwtService, employeeRepository);

        // Configurar datos de prueba
        testDepartment = new Department(UUID.randomUUID(), "IT");
        testRole = new Role(UUID.randomUUID(), "ADMIN");
        testEmployee = new Employee(
            UUID.randomUUID(),
            "John",
            "Doe",
            "john.doe@example.com",
            "$2a$10$hashedPassword",
            testDepartment,
            testRole,
            LocalDate.now(),
            15
        );
    }

    @Test
    void authenticate_ShouldReturnAuthResponseDTO_WhenCredentialsAreValid() {
        // Given
        LoginRequestDTO loginRequest = new LoginRequestDTO("john.doe@example.com", "password123");
        List<SimpleGrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_ADMIN"),
            new SimpleGrantedAuthority("ROLE_USER")
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(employeeRepository.findByEmail(loginRequest.email()))
            .thenReturn(Optional.of(testEmployee));
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(jwtService.generateToken(any(), eq(testEmployee.getEmail())))
            .thenReturn("mocked.jwt.token");

        // When
        AuthResponseDTO response = authService.authenticate(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals("mocked.jwt.token", response.token());
        assertEquals("john.doe@example.com", response.email());
        assertEquals("John", response.firstName());
        assertEquals("Doe", response.lastName());
        assertEquals(2, response.roles().size());
        assertTrue(response.roles().contains("ROLE_ADMIN"));
        assertTrue(response.roles().contains("ROLE_USER"));
        assertEquals(3600, response.expiresIn());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(employeeRepository).findByEmail(loginRequest.email());
        verify(jwtService).generateToken(any(), eq(testEmployee.getEmail()));
    }

    @Test
    void authenticate_ShouldThrowException_WhenEmployeeNotFound() {
        // Given
        LoginRequestDTO loginRequest = new LoginRequestDTO("notfound@example.com", "password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(employeeRepository.findByEmail(loginRequest.email()))
            .thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> authService.authenticate(loginRequest));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(employeeRepository).findByEmail(loginRequest.email());
        verifyNoInteractions(jwtService);
    }

    @Test
    void validateToken_ShouldReturnAuthResponseDTO_WhenTokenIsValid() {
        // Given
        String validToken = "valid.jwt.token";
        List<String> roles = List.of("ROLE_ADMIN", "ROLE_USER");

        when(jwtService.extractUsername(validToken)).thenReturn(testEmployee.getEmail());
        when(employeeRepository.findByEmail(testEmployee.getEmail()))
            .thenReturn(Optional.of(testEmployee));
        when(jwtService.isTokenValid(validToken, testEmployee.getEmail())).thenReturn(true);
        when(jwtService.extractClaim(eq(validToken), any())).thenReturn(roles);

        // When
        AuthResponseDTO response = authService.validateToken(validToken);

        // Then
        assertNotNull(response);
        assertEquals(validToken, response.token());
        assertEquals(testEmployee.getEmail(), response.email());
        assertEquals(testEmployee.getFirstName(), response.firstName());
        assertEquals(testEmployee.getLastName(), response.lastName());
        assertEquals(roles, response.roles());
        assertEquals(0, response.expiresIn());

        verify(jwtService).extractUsername(validToken);
        verify(employeeRepository).findByEmail(testEmployee.getEmail());
        verify(jwtService).isTokenValid(validToken, testEmployee.getEmail());
    }

    @Test
    void validateToken_ShouldThrowException_WhenTokenIsInvalid() {
        // Given
        String invalidToken = "invalid.jwt.token";

        when(jwtService.extractUsername(invalidToken)).thenReturn(testEmployee.getEmail());
        when(employeeRepository.findByEmail(testEmployee.getEmail()))
            .thenReturn(Optional.of(testEmployee));
        when(jwtService.isTokenValid(invalidToken, testEmployee.getEmail())).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> authService.validateToken(invalidToken));

        verify(jwtService).extractUsername(invalidToken);
        verify(employeeRepository).findByEmail(testEmployee.getEmail());
        verify(jwtService).isTokenValid(invalidToken, testEmployee.getEmail());
    }

    @Test
    void validateToken_ShouldThrowException_WhenEmployeeNotFound() {
        // Given
        String validToken = "valid.jwt.token";

        when(jwtService.extractUsername(validToken)).thenReturn("notfound@example.com");
        when(employeeRepository.findByEmail("notfound@example.com"))
            .thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> authService.validateToken(validToken));

        verify(jwtService).extractUsername(validToken);
        verify(employeeRepository).findByEmail("notfound@example.com");
        verifyNoMoreInteractions(jwtService);
    }
}
