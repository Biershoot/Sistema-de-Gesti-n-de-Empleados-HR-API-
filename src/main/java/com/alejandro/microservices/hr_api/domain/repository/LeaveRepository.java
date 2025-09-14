package com.alejandro.microservices.hr_api.domain.repository;

import com.alejandro.microservices.hr_api.domain.model.Leave;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeaveRepository {
    Leave save(Leave leave);
    List<Leave> findByEmployeeId(UUID employeeId);
    List<Leave> findByDateRange(LocalDate start, LocalDate end);
    Optional<Leave> findById(UUID id);
    void deleteById(UUID id);
}
