package com.alejandro.microservices.hr_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de la aplicación HR API.
 *
 * Sistema de Gestión de Recursos Humanos desarrollado con Spring Boot
 * siguiendo los principios de Arquitectura Hexagonal (Ports and Adapters).
 *
 * Funcionalidades principales:
 * - Gestión completa de empleados (CRUD)
 * - Administración de departamentos y roles
 * - Sistema de vacaciones y ausencias
 * - Reportes analíticos por departamento
 * - API REST con validaciones y manejo de errores
 *
 * Arquitectura implementada:
 * - Domain: Entidades de negocio y repositorios (puertos)
 * - Application: Servicios de casos de uso y DTOs
 * - Infrastructure: Adaptadores (REST controllers, JPA repositories)
 *
 * Tecnologías utilizadas:
 * - Spring Boot 3.x
 * - Spring Data JPA
 * - MySQL Database
 * - Jakarta Validation
 * - Maven
 *
 * @author Sistema HR API
 * @version 1.0
 * @since 2024
 */
@SpringBootApplication
public class HrApiApplication {

  /**
   * Método principal que inicia la aplicación Spring Boot.
   *
   * @param args Argumentos de línea de comandos
   */
  public static void main(String[] args) {
    SpringApplication.run(HrApiApplication.class, args);
  }
}
