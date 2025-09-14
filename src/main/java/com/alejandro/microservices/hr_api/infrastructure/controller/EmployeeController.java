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

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> create(@Valid @RequestBody EmployeeRequestDTO employeeRequest) {
        EmployeeResponseDTO createdEmployee = employeeService.createEmployee(employeeRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee);
    }

    @GetMapping
    public ResponseEntity<List<EmployeeResponseDTO>> getAll() {
        List<EmployeeResponseDTO> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getById(@PathVariable UUID id) {
        EmployeeResponseDTO employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> update(@PathVariable UUID id,
                                                    @Valid @RequestBody EmployeeRequestDTO employeeRequest) {
        EmployeeResponseDTO updatedEmployee = employeeService.updateEmployee(id, employeeRequest);
        return ResponseEntity.ok(updatedEmployee);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<EmployeeResponseDTO>> getByDepartment(@PathVariable UUID departmentId) {
        List<EmployeeResponseDTO> employees = employeeService.getEmployeesByDepartment(departmentId);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/role/{roleId}")
    public ResponseEntity<List<EmployeeResponseDTO>> getByRole(@PathVariable UUID roleId) {
        List<EmployeeResponseDTO> employees = employeeService.getEmployeesByRole(roleId);
        return ResponseEntity.ok(employees);
    }

    @PutMapping("/{id}/vacation")
    public ResponseEntity<EmployeeResponseDTO> takeVacation(@PathVariable UUID id,
                                                          @RequestParam int days) {
        EmployeeResponseDTO employee = employeeService.takeVacation(id, days);
        return ResponseEntity.ok(employee);
    }

    @PutMapping("/{id}/vacation/add")
    public ResponseEntity<EmployeeResponseDTO> addVacationDays(@PathVariable UUID id,
                                                             @RequestParam int days) {
        EmployeeResponseDTO employee = employeeService.addVacationDays(id, days);
        return ResponseEntity.ok(employee);
    }
}
