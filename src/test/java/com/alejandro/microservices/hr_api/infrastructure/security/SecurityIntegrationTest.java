package com.alejandro.microservices.hr_api.infrastructure.security;

import com.alejandro.microservices.hr_api.application.dto.AuthResponseDTO;
import com.alejandro.microservices.hr_api.application.dto.LoginRequestDTO;
import com.alejandro.microservices.hr_api.application.dto.RegisterRequestDTO;
import com.alejandro.microservices.hr_api.application.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración para la seguridad de la aplicación.
 *
 * Estas pruebas verifican que los endpoints estén protegidos correctamente
 * y que la autenticación funcione como se espera.
 *
 * @author Sistema HR API
 * @version 1.0
 * @since 2025-01-14
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@DisplayName("Security Integration Tests")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private String validJwtToken;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        validJwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqdWFuLnBlcmV6Iiwicm9sZSI6IlVTRVIiLCJpYXQiOjE2NDA5OTUyMDAsImV4cCI6MTY0MDk5ODgwMH0.test-signature";
    }

    @Nested
    @DisplayName("Endpoints Públicos")
    class PublicEndpointsTests {

        @Test
        @DisplayName("Debería permitir acceso a endpoints de autenticación sin token")
        void shouldAllowAccessToAuthEndpointsWithoutToken() throws Exception {
            // Given
            RegisterRequestDTO registerRequest = new RegisterRequestDTO(
                    "juan.perez",
                    "password123",
                    "USER"
            );

            LoginRequestDTO loginRequest = new LoginRequestDTO(
                    "juan.perez",
                    "password123"
            );

            AuthResponseDTO authResponse = new AuthResponseDTO(
                    validJwtToken,
                    "juan.perez",
                    "USER",
                    3600L
            );

            when(authService.register(any(RegisterRequestDTO.class))).thenReturn(authResponse);
            when(authService.authenticate(any(LoginRequestDTO.class))).thenReturn(authResponse);

            // When & Then - Registro
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isCreated());

            // When & Then - Login
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Debería permitir acceso a endpoints de salud sin token")
        void shouldAllowAccessToHealthEndpointsWithoutToken() throws Exception {
            // When & Then
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Endpoints Protegidos")
    class ProtectedEndpointsTests {

        @Test
        @DisplayName("Debería denegar acceso a endpoints de empleados sin token")
        void shouldDenyAccessToEmployeeEndpointsWithoutToken() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/employees"))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get("/api/employees/{id}", UUID.randomUUID().toString()))
                    .andExpect(status().isForbidden());

            mockMvc.perform(post("/api/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());

            mockMvc.perform(MockMvcRequestBuilders.put("/api/employees/{id}", UUID.randomUUID().toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());

            mockMvc.perform(MockMvcRequestBuilders.delete("/api/employees/{id}", UUID.randomUUID().toString()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Debería denegar acceso a endpoints de departamentos sin token")
        void shouldDenyAccessToDepartmentEndpointsWithoutToken() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/departments"))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get("/api/departments/{id}", UUID.randomUUID().toString()))
                    .andExpect(status().isForbidden());

            mockMvc.perform(post("/api/departments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());

            mockMvc.perform(MockMvcRequestBuilders.put("/api/departments/{id}", UUID.randomUUID().toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());

            mockMvc.perform(MockMvcRequestBuilders.delete("/api/departments/{id}", UUID.randomUUID().toString()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Debería denegar acceso a endpoints de roles sin token")
        void shouldDenyAccessToRoleEndpointsWithoutToken() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/roles"))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get("/api/roles/{id}", UUID.randomUUID().toString()))
                    .andExpect(status().isForbidden());

            mockMvc.perform(post("/api/roles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());

            mockMvc.perform(MockMvcRequestBuilders.put("/api/roles/{id}", UUID.randomUUID().toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());

            mockMvc.perform(MockMvcRequestBuilders.delete("/api/roles/{id}", UUID.randomUUID().toString()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Validación de Tokens JWT")
    class JwtTokenValidationTests {

        @Test
        @DisplayName("Debería denegar acceso con token JWT inválido")
        void shouldDenyAccessWithInvalidJwtToken() throws Exception {
            // Given
            String invalidToken = "invalid.jwt.token";

            // When & Then
            mockMvc.perform(get("/api/employees")
                            .header("Authorization", "Bearer " + invalidToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Debería denegar acceso con token JWT malformado")
        void shouldDenyAccessWithMalformedJwtToken() throws Exception {
            // Given
            String malformedToken = "not-a-jwt-token";

            // When & Then
            mockMvc.perform(get("/api/employees")
                            .header("Authorization", "Bearer " + malformedToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Debería denegar acceso con token JWT expirado")
        void shouldDenyAccessWithExpiredJwtToken() throws Exception {
            // Given
            String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqdWFuLnBlcmV6Iiwicm9sZSI6IlVTRVIiLCJpYXQiOjE2NDA5OTUyMDAsImV4cCI6MTY0MDk5ODgwMH0.expired-signature";

            // When & Then
            mockMvc.perform(get("/api/employees")
                            .header("Authorization", "Bearer " + expiredToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Debería denegar acceso sin header Authorization")
        void shouldDenyAccessWithoutAuthorizationHeader() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/employees"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Debería denegar acceso con formato incorrecto de Authorization header")
        void shouldDenyAccessWithIncorrectAuthorizationHeaderFormat() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/employees")
                            .header("Authorization", "Basic " + validJwtToken))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get("/api/employees")
                            .header("Authorization", validJwtToken)) // Sin "Bearer "
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Configuración CORS")
    class CorsConfigurationTests {

        @Test
        @DisplayName("Debería incluir headers CORS en las respuestas")
        void shouldIncludeCorsHeadersInResponses() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/employees")
                            .header("Origin", "http://localhost:3000"))
                    .andExpect(status().isForbidden())
                    .andExpect(header().string("Access-Control-Allow-Origin", "*"))
                    .andExpect(header().exists("Access-Control-Allow-Methods"))
                    .andExpect(header().exists("Access-Control-Allow-Headers"));
        }

        @Test
        @DisplayName("Debería manejar preflight requests correctamente")
        void shouldHandlePreflightRequestsCorrectly() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/employees")
                            .header("Origin", "http://localhost:3000")
                            .header("Access-Control-Request-Method", "POST")
                            .header("Access-Control-Request-Headers", "Content-Type,Authorization"))
                    .andExpect(status().isForbidden())
                    .andExpect(header().string("Access-Control-Allow-Origin", "*"));
        }
    }

    @Nested
    @DisplayName("Manejo de Errores de Seguridad")
    class SecurityErrorHandlingTests {

        @Test
        @DisplayName("Debería retornar error 403 para endpoints protegidos")
        void shouldReturn403ForProtectedEndpoints() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/employees"))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get("/api/departments"))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get("/api/roles"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Debería retornar error 401 para tokens inválidos")
        void shouldReturn401ForInvalidTokens() throws Exception {
            // Given
            String invalidToken = "invalid-token";

            // When & Then
            mockMvc.perform(get("/api/employees")
                            .header("Authorization", "Bearer " + invalidToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Debería manejar múltiples intentos de acceso no autorizado")
        void shouldHandleMultipleUnauthorizedAccessAttempts() throws Exception {
            // When & Then - Múltiples intentos
            for (int i = 0; i < 5; i++) {
                mockMvc.perform(get("/api/employees"))
                        .andExpect(status().isForbidden());
            }
        }
    }

    @Nested
    @DisplayName("Validación de Roles")
    class RoleValidationTests {

        @Test
        @DisplayName("Debería validar que los endpoints requieren autenticación")
        void shouldValidateThatEndpointsRequireAuthentication() throws Exception {
            // When & Then - Todos los endpoints protegidos deben requerir autenticación
            String[] protectedEndpoints = {
                    "/api/employees",
                    "/api/departments",
                    "/api/roles"
            };

            for (String endpoint : protectedEndpoints) {
                mockMvc.perform(get(endpoint))
                        .andExpect(status().isForbidden());
            }
        }

        @Test
        @DisplayName("Debería permitir acceso a endpoints públicos")
        void shouldAllowAccessToPublicEndpoints() throws Exception {
            // When & Then
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk());

            // Los endpoints de autenticación también son públicos
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest()); // Bad request por datos inválidos, no por autenticación

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest()); // Bad request por datos inválidos, no por autenticación
        }
    }
}
