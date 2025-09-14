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

    /**
     * Busca permisos que se solapen con el rango de fechas especificado para un empleado.
     *
     * @param employeeId ID del empleado
     * @param startDate Fecha de inicio del rango
     * @param endDate Fecha de fin del rango
     * @param excludeLeaveId ID del permiso a excluir de la búsqueda (útil para actualizaciones)
     * @return Lista de permisos que se solapan
     */
    List<Leave> findOverlappingLeaves(UUID employeeId, LocalDate startDate, LocalDate endDate, UUID excludeLeaveId);
}
