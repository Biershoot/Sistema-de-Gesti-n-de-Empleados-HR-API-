package com.alejandro.microservices.hr_api.domain.model;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidad de dominio que representa un permiso o ausencia de un empleado.
 * Forma parte del sistema de gestión de vacaciones y ausencias del HR API.
 *
 * Esta entidad encapsula la información sobre permisos solicitados por empleados,
 * incluyendo el período de ausencia y el tipo de permiso.
 *
 * @author Sistema HR API
 * @version 1.0
 * @see LeaveType
 */
public class Leave {
    private UUID id;
    private UUID employeeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveType type;

    /**
     * Constructor para crear una nueva instancia de Leave.
     *
     * @param id Identificador único del permiso
     * @param employeeId ID del empleado que solicita el permiso
     * @param startDate Fecha de inicio del permiso
     * @param endDate Fecha de fin del permiso
     * @param type Tipo de permiso (VACATION, SICK, UNPAID)
     */
    public Leave(UUID id, UUID employeeId, LocalDate startDate, LocalDate endDate, LeaveType type) {
        this.id = id;
        this.employeeId = employeeId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
    }

    public UUID getId() { return id; }
    public UUID getEmployeeId() { return employeeId; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public LeaveType getType() { return type; }
}
