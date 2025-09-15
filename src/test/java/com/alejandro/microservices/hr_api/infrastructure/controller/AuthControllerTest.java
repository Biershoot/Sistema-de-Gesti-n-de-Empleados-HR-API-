package com.alejandro.microservices.hr_api.infrastructure.controller;

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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas unitarias para el AuthController.
 *
 * Estas pruebas verifican el comportamiento de los endpoints de autenticación,
 * incluyendo registro, login, validaciones de entrada y respuestas HTTP.
 *
 * @author Sistema HR API
 * @version 1.0
 * @since 2025-01-14
 */
@WebMvcTest(AuthController.class)
@DisplayName("AuthController - Pruebas Unitarias")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterRequestDTO registerRequestDTO;
    private LoginRequestDTO loginRequestDTO;
    private AuthResponseDTO authResponseDTO;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        registerRequestDTO = new RegisterRequestDTO(
                "juan.perez",
                "password123",
                "USER"
        );

        loginRequestDTO = new LoginRequestDTO(
                "juan.perez",
                "password123"
        );

        authResponseDTO = new AuthResponseDTO(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                "juan.perez",
                "USER",
                3600L
        );
    }

    @Nested
    @DisplayName("Registro de Usuarios")
    class UserRegistrationTests {

        @Test
        @DisplayName("Debería registrar un usuario exitosamente con datos válidos")
        void shouldRegisterUserSuccessfullyWithValidData() throws Exception {
            // Given
            when(authService.register(any(RegisterRequestDTO.class)))
                    .thenReturn(authResponseDTO);

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.token").value("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."))
                    .andExpect(jsonPath("$.userId").value(userId.toString()))
                    .andExpect(jsonPath("$.username").value("juan.perez"))
                    .andExpect(jsonPath("$.email").value("juan.perez@empresa.com"))
                    .andExpect(jsonPath("$.role").value("USER"));

            verify(authService).register(any(RegisterRequestDTO.class));
        }

        @Test
        @DisplayName("Debería retornar error 400 cuando los datos de registro son inválidos")
        void shouldReturnBadRequestWhenRegistrationDataIsInvalid() throws Exception {
            // Given
            RegisterRequestDTO invalidRequest = new RegisterRequestDTO(
                    "", // Username vacío
                    "123", // Password muy corto
                    "" // Rol vacío
            );

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).register(any());
        }

        @Test
        @DisplayName("Debería retornar error 400 cuando el JSON es inválido")
        void shouldReturnBadRequestWhenJsonIsInvalid() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("invalid json"))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).register(any());
        }

        @Test
        @DisplayName("Debería manejar excepciones del servicio de registro")
        void shouldHandleRegistrationServiceExceptions() throws Exception {
            // Given
            when(authService.register(any(RegisterRequestDTO.class)))
                    .thenThrow(new RuntimeException("Error interno del servidor"));

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequestDTO)))
                    .andExpect(status().isInternalServerError());

            verify(authService).register(any(RegisterRequestDTO.class));
        }

        @Test
        @DisplayName("Debería manejar cuando el usuario ya existe")
        void shouldHandleWhenUserAlreadyExists() throws Exception {
            // Given
            when(authService.register(any(RegisterRequestDTO.class)))
                    .thenThrow(new IllegalArgumentException("El usuario ya existe"));

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("El usuario ya existe"));

            verify(authService).register(any(RegisterRequestDTO.class));
        }
    }

    @Nested
    @DisplayName("Inicio de Sesión")
    class UserLoginTests {

        @Test
        @DisplayName("Debería iniciar sesión exitosamente con credenciales válidas")
        void shouldLoginSuccessfullyWithValidCredentials() throws Exception {
            // Given
            when(authService.authenticate(any(LoginRequestDTO.class)))
                    .thenReturn(authResponseDTO);

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."))
                    .andExpect(jsonPath("$.userId").value(userId.toString()))
                    .andExpect(jsonPath("$.username").value("juan.perez"))
                    .andExpect(jsonPath("$.email").value("juan.perez@empresa.com"))
                    .andExpect(jsonPath("$.role").value("USER"));

            verify(authService).authenticate(any(LoginRequestDTO.class));
        }

        @Test
        @DisplayName("Debería retornar error 400 cuando las credenciales son inválidas")
        void shouldReturnBadRequestWhenCredentialsAreInvalid() throws Exception {
            // Given
            LoginRequestDTO invalidRequest = new LoginRequestDTO(
                    "", // Username vacío
                    "" // Password vacío
            );

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).authenticate(any());
        }

        @Test
        @DisplayName("Debería retornar error 401 cuando las credenciales son incorrectas")
        void shouldReturnUnauthorizedWhenCredentialsAreIncorrect() throws Exception {
            // Given
            when(authService.authenticate(any(LoginRequestDTO.class)))
                    .thenThrow(new IllegalArgumentException("Credenciales incorrectas"));

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Credenciales incorrectas"));

            verify(authService).authenticate(any(LoginRequestDTO.class));
        }

        @Test
        @DisplayName("Debería retornar error 400 cuando el JSON es inválido")
        void shouldReturnBadRequestWhenJsonIsInvalid() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("invalid json"))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).authenticate(any());
        }
    }

    @Nested
    @DisplayName("Validaciones de Contenido")
    class ContentValidationTests {

        @Test
        @DisplayName("Debería validar que el Content-Type sea JSON")
        void shouldValidateContentTypeIsJson() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.TEXT_PLAIN)
                            .content("invalid content"))
                    .andExpect(status().isUnsupportedMediaType());

            verify(authService, never()).register(any());
        }

        @Test
        @DisplayName("Debería aceptar solo métodos HTTP permitidos")
        void shouldAcceptOnlyAllowedHttpMethods() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/auth/invalid-endpoint")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequestDTO)))
                    .andExpect(status().isNotFound());

            verify(authService, never()).authenticate(any());
        }
    }

    @Nested
    @DisplayName("Casos Edge y Validaciones Específicas")
    class EdgeCasesAndSpecificValidationsTests {

        @Test
        @DisplayName("Debería manejar campos nulos en el registro")
        void shouldHandleNullFieldsInRegistration() throws Exception {
            // Given
            RegisterRequestDTO requestWithNulls = new RegisterRequestDTO(
                    null, null, null
            );

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithNulls)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).register(any());
        }

        @Test
        @DisplayName("Debería manejar campos nulos en el login")
        void shouldHandleNullFieldsInLogin() throws Exception {
            // Given
            LoginRequestDTO requestWithNulls = new LoginRequestDTO(null, null);

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithNulls)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).authenticate(any());
        }

        @Test
        @DisplayName("Debería manejar strings vacíos en el registro")
        void shouldHandleEmptyStringsInRegistration() throws Exception {
            // Given
            RegisterRequestDTO requestWithEmptyStrings = new RegisterRequestDTO(
                    "   ", "   ", "   "
            );

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithEmptyStrings)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).register(any());
        }

        @Test
        @DisplayName("Debería manejar strings vacíos en el login")
        void shouldHandleEmptyStringsInLogin() throws Exception {
            // Given
            LoginRequestDTO requestWithEmptyStrings = new LoginRequestDTO("   ", "   ");

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithEmptyStrings)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).authenticate(any());
        }

        @Test
        @DisplayName("Debería manejar caracteres especiales en el username")
        void shouldHandleSpecialCharactersInUsername() throws Exception {
            // Given
            RegisterRequestDTO requestWithSpecialChars = new RegisterRequestDTO(
                    "user@#$%",
                    "password123",
                    "USER"
            );

            when(authService.register(any(RegisterRequestDTO.class)))
                    .thenReturn(authResponseDTO);

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithSpecialChars)))
                    .andExpect(status().isCreated());

            verify(authService).register(any(RegisterRequestDTO.class));
        }

        @Test
        @DisplayName("Debería manejar emails con diferentes formatos")
        void shouldHandleDifferentEmailFormats() throws Exception {
            // Given
            RegisterRequestDTO requestWithValidEmail = new RegisterRequestDTO(
                    "usuario",
                    "password123",
                    "USER"
            );

            when(authService.register(any(RegisterRequestDTO.class)))
                    .thenReturn(authResponseDTO);

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithValidEmail)))
                    .andExpect(status().isCreated());

            verify(authService).register(any(RegisterRequestDTO.class));
        }
    }

    @Nested
    @DisplayName("Manejo de Errores del Servicio")
    class ServiceErrorHandlingTests {

        @Test
        @DisplayName("Debería manejar errores de validación del servicio")
        void shouldHandleServiceValidationErrors() throws Exception {
            // Given
            when(authService.register(any(RegisterRequestDTO.class)))
                    .thenThrow(new IllegalArgumentException("El email ya está en uso"));

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("El email ya está en uso"));

            verify(authService).register(any(RegisterRequestDTO.class));
        }

        @Test
        @DisplayName("Debería manejar errores internos del servidor")
        void shouldHandleInternalServerErrors() throws Exception {
            // Given
            when(authService.authenticate(any(LoginRequestDTO.class)))
                    .thenThrow(new RuntimeException("Error interno del servidor"));

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequestDTO)))
                    .andExpect(status().isInternalServerError());

            verify(authService).authenticate(any(LoginRequestDTO.class));
        }

        @Test
        @DisplayName("Debería manejar excepciones de seguridad")
        void shouldHandleSecurityExceptions() throws Exception {
            // Given
            when(authService.authenticate(any(LoginRequestDTO.class)))
                    .thenThrow(new SecurityException("Cuenta bloqueada por intentos fallidos"));

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequestDTO)))
                    .andExpect(status().isForbidden());

            verify(authService).authenticate(any(LoginRequestDTO.class));
        }
    }
}
