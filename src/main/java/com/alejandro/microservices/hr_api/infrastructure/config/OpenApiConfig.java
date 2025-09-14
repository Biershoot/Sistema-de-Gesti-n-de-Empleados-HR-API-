package com.alejandro.microservices.hr_api.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI/Swagger para la documentación de la API.
 * 
 * Proporciona documentación interactiva de todos los endpoints de la API HR,
 * incluyendo información de contacto, licencia y servidores disponibles.
 * 
 * @author Alejandro Arango Calderón
 * @version 1.0
 * @since 2025-01-14
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HR Management API")
                        .version("1.0.0")
                        .description("API REST para la gestión de recursos humanos desarrollada con Spring Boot siguiendo la Arquitectura Hexagonal. " +
                                "Permite gestionar empleados, departamentos, roles, vacaciones y reportes con operaciones CRUD completas.")
                        .contact(new Contact()
                                .name("Alejandro Arango Calderón")
                                .email("alejandro@example.com")
                                .url("https://github.com/Biershoot/Sistema_Gestion_Empleados_HR_API"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de desarrollo local"),
                        new Server()
                                .url("https://hr-api.example.com")
                                .description("Servidor de producción")
                ));
    }
}
