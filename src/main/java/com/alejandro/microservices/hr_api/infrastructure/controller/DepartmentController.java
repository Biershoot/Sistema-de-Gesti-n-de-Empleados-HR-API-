package com.alejandro.microservices.hr_api.infrastructure.controller;

import com.alejandro.microservices.hr_api.application.dto.DepartmentDTO;
import com.alejandro.microservices.hr_api.application.service.DepartmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para la gestión de departamentos.
 *
 * Este controlador maneja todas las operaciones relacionadas con la gestión
 * de departamentos en el sistema de recursos humanos, incluyendo creación,
 * consulta, actualización y eliminación de departamentos.
 *
 * Endpoints disponibles:
 * - POST /api/departments - Crear nuevo departamento
 * - GET /api/departments - Listar todos los departamentos
 * - GET /api/departments/{id} - Obtener departamento por ID
 * - DELETE /api/departments/{id} - Eliminar departamento
 *
 * @author Sistema HR API
 * @version 1.0
 * @since 2025-01-14
 */
// @Tag(name = "Departments", description = "Operaciones relacionadas con la gestión de departamentos")
@RestController
@RequestMapping("/api/departments")
@CrossOrigin(origins = "*")
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * Constructor con inyección de dependencias del servicio de departamentos.
     *
     * @param departmentService Servicio que contiene la lógica de negocio para departamentos
     */
    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    public ResponseEntity<DepartmentDTO> create(@Valid @RequestBody CreateDepartmentRequest request) {
        DepartmentDTO department = departmentService.createDepartment(request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(department);
    }

    @GetMapping
    public ResponseEntity<List<DepartmentDTO>> getAll() {
        List<DepartmentDTO> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDTO> getById(@PathVariable UUID id) {
        DepartmentDTO department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(department);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    // Record para la solicitud de creación de departamento
    public record CreateDepartmentRequest(
            @jakarta.validation.constraints.NotBlank(message = "El nombre del departamento no puede estar vacío")
            String name
    ) {}
}
