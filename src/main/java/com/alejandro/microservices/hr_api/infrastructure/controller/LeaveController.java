package com.alejandro.microservices.hr_api.infrastructure.controller;

import com.alejandro.microservices.hr_api.application.dto.LeaveRequestDTO;
import com.alejandro.microservices.hr_api.application.dto.LeaveResponseDTO;
import com.alejandro.microservices.hr_api.application.service.LeaveService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/leaves")
@CrossOrigin(origins = "*")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @PostMapping
    public ResponseEntity<LeaveResponseDTO> requestLeave(@Valid @RequestBody LeaveRequestDTO request) {
        LeaveResponseDTO leave = leaveService.requestLeave(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(leave);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveResponseDTO> getById(@PathVariable UUID id) {
        LeaveResponseDTO leave = leaveService.getLeaveById(id);
        return ResponseEntity.ok(leave);
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<LeaveResponseDTO>> getByEmployee(@PathVariable UUID employeeId) {
        List<LeaveResponseDTO> leaves = leaveService.getLeavesByEmployee(employeeId);
        return ResponseEntity.ok(leaves);
    }

    @GetMapping("/range")
    public ResponseEntity<List<LeaveResponseDTO>> getByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        List<LeaveResponseDTO> leaves = leaveService.getLeavesByDateRange(
                LocalDate.parse(startDate),
                LocalDate.parse(endDate)
        );
        return ResponseEntity.ok(leaves);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelLeave(@PathVariable UUID id) {
        leaveService.cancelLeave(id);
        return ResponseEntity.noContent().build();
    }
}
