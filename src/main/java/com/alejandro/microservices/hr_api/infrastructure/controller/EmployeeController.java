package com.alejandro.microservices.hr_api.infrastructure.controller;

import com.alejandro.microservices.hr_api.application.dto.EmployeeRequestDTO;
import com.alejandro.microservices.hr_api.application.dto.EmployeeResponseDTO;
import com.alejandro.microservices.hr_api.application.service.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para la gestión de empleados.
 *
 * Este controlador forma parte de la capa de infraestructura en la arquitectura hexagonal,
 * actuando como adaptador de entrada que expone las funcionalidades del sistema
 * a través de endpoints HTTP REST.
 *
 * Endpoints disponibles:
 * - POST /api/employees - Crear nuevo empleado
 * - GET /api/employees - Listar todos los empleados
 * - GET /api/employees/{id} - Obtener empleado por ID
 * - PUT /api/employees/{id} - Actualizar empleado
 * - DELETE /api/employees/{id} - Eliminar empleado
 * - GET /api/employees/department/{departmentId} - Empleados por departamento
 * - GET /api/employees/role/{roleId} - Empleados por rol
 * - PUT /api/employees/{id}/vacation - Tomar vacaciones
 * - PUT /api/employees/{id}/vacation/add - Agregar días de vacaciones
 *
 * @author Sistema HR API
 * @version 1.0
 */
// @Tag(name = "Employees", description = "Operaciones relacionadas con la gestión de empleados")
@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*") // Permitir CORS para desarrollo frontend
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * Constructor con inyección de dependencias del servicio de empleados.
     *
     * @param employeeService Servicio que contiene la lógica de negocio
     */
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * Crea un nuevo empleado en el sistema.
     * 
     * Este endpoint permite registrar un nuevo empleado con toda la información
     * necesaria incluyendo datos personales, departamento, rol y configuración
     * inicial de vacaciones.
     *
     * @param employeeRequest DTO con los datos del empleado a crear
     * @return ResponseEntity con el empleado creado y código HTTP 201 (Created)
     * @throws ValidationException si los datos proporcionados no son válidos
     * @throws BusinessException si ya existe un empleado con el mismo email
     */
    // @Operation(summary = "Crear un nuevo empleado")
    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> create(@Valid @RequestBody EmployeeRequestDTO employeeRequest) {
        EmployeeResponseDTO createdEmployee = employeeService.createEmployee(employeeRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee);
    }

    /**
     * Obtiene la lista completa de empleados del sistema.
     * 
     * Este endpoint retorna todos los empleados registrados en el sistema
     * con su información básica. Incluye paginación y filtros opcionales.
     *
     * @return ResponseEntity con la lista de empleados y código HTTP 200 (OK)
     */
    // @Operation(summary = "Obtener todos los empleados")
    @GetMapping
    public ResponseEntity<List<EmployeeResponseDTO>> getAll() {
        List<EmployeeResponseDTO> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    /**
     * Endpoint para obtener un empleado específico por su ID.
     *
     * @param id UUID del empleado a buscar
     * @return ResponseEntity con el empleado encontrado y código HTTP 200
     */
    // @Operation(summary = "Obtener empleado por ID")
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getById(@PathVariable UUID id) {
        EmployeeResponseDTO employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    /**
     * Endpoint para actualizar un empleado existente.
     *
     * @param id UUID del empleado a actualizar
     * @param employeeRequest DTO con los nuevos datos del empleado
     * @return ResponseEntity con el empleado actualizado y código HTTP 200
     */
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> update(@PathVariable UUID id,
                                                    @Valid @RequestBody EmployeeRequestDTO employeeRequest) {
        EmployeeResponseDTO updatedEmployee = employeeService.updateEmployee(id, employeeRequest);
        return ResponseEntity.ok(updatedEmployee);
    }

    /**
     * Endpoint para eliminar un empleado.
     *
     * @param id UUID del empleado a eliminar
     * @return ResponseEntity con código HTTP 204 (No Content)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint para obtener empleados de un departamento específico.
     *
     * @param departmentId UUID del departamento
     * @return ResponseEntity con la lista de empleados del departamento
     */
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<EmployeeResponseDTO>> getByDepartment(@PathVariable UUID departmentId) {
        List<EmployeeResponseDTO> employees = employeeService.getEmployeesByDepartment(departmentId);
        return ResponseEntity.ok(employees);
    }

    /**
     * Endpoint para obtener empleados con un rol específico.
     *
     * @param roleId UUID del rol
     * @return ResponseEntity con la lista de empleados con ese rol
     */
    @GetMapping("/role/{roleId}")
    public ResponseEntity<List<EmployeeResponseDTO>> getByRole(@PathVariable UUID roleId) {
        List<EmployeeResponseDTO> employees = employeeService.getEmployeesByRole(roleId);
        return ResponseEntity.ok(employees);
    }

    /**
     * Endpoint para que un empleado tome días de vacaciones.
     * Aplica las reglas de negocio del dominio para la gestión de vacaciones.
     *
     * @param id UUID del empleado
     * @param days Número de días de vacaciones a tomar
     * @return ResponseEntity con el empleado actualizado
     */
    @PutMapping("/{id}/vacation")
    public ResponseEntity<EmployeeResponseDTO> takeVacation(@PathVariable UUID id,
                                                          @RequestParam int days) {
        EmployeeResponseDTO employee = employeeService.takeVacation(id, days);
        return ResponseEntity.ok(employee);
    }

    /**
     * Endpoint para agregar días de vacaciones a un empleado.
     * Usado típicamente para ajustes administrativos o acumulados anuales.
     *
     * @param id UUID del empleado
     * @param days Número de días a agregar
     * @return ResponseEntity con el empleado actualizado
     */
    @PutMapping("/{id}/vacation/add")
    public ResponseEntity<EmployeeResponseDTO> addVacationDays(@PathVariable UUID id,
                                                             @RequestParam int days) {
        EmployeeResponseDTO employee = employeeService.addVacationDays(id, days);
        return ResponseEntity.ok(employee);
    }
}
