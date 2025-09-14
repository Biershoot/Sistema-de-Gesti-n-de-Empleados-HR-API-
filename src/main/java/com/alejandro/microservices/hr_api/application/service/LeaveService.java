package com.alejandro.microservices.hr_api.application.service;

import com.alejandro.microservices.hr_api.application.dto.LeaveRequestDTO;
import com.alejandro.microservices.hr_api.application.dto.LeaveResponseDTO;
import com.alejandro.microservices.hr_api.domain.model.Leave;
import com.alejandro.microservices.hr_api.domain.model.LeaveType;
import com.alejandro.microservices.hr_api.domain.repository.LeaveRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LeaveService {

    private final LeaveRepository leaveRepository;

    public LeaveService(LeaveRepository leaveRepository) {
        this.leaveRepository = leaveRepository;
    }

    public LeaveResponseDTO requestLeave(LeaveRequestDTO request) {
        // Validar que la fecha de inicio sea anterior a la fecha de fin
        if (request.startDate().isAfter(request.endDate())) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior a la fecha de fin");
        }

        // Validar que las fechas no sean en el pasado (excepto para licencias médicas)
        if (request.type() != LeaveType.SICK && request.startDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se pueden solicitar permisos con fechas pasadas");
        }

        Leave leave = new Leave(
                UUID.randomUUID(),
                request.employeeId(),
                request.startDate(),
                request.endDate(),
                request.type()
        );

        leaveRepository.save(leave);
        return mapToResponseDTO(leave);
    }

    public List<LeaveResponseDTO> getLeavesByEmployee(UUID employeeId) {
        return leaveRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<LeaveResponseDTO> getLeavesByDateRange(LocalDate start, LocalDate end) {
        return leaveRepository.findByDateRange(start, end).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public LeaveResponseDTO getLeaveById(UUID id) {
        return leaveRepository.findById(id)
                .map(this::mapToResponseDTO)
                .orElseThrow(() -> new IllegalArgumentException("Permiso no encontrado"));
    }

    public void cancelLeave(UUID id) {
        if (!leaveRepository.findById(id).isPresent()) {
            throw new IllegalArgumentException("Permiso no encontrado");
        }
        leaveRepository.deleteById(id);
    }

    private LeaveResponseDTO mapToResponseDTO(Leave leave) {
        return new LeaveResponseDTO(
                leave.getId(),
                leave.getEmployeeId(),
                leave.getStartDate(),
                leave.getEndDate(),
                leave.getType()
        );
    }
}
