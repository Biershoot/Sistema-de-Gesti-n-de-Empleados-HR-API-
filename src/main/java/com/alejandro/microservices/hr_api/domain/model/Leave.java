package com.alejandro.microservices.hr_api.domain.model;

import java.time.LocalDate;
import java.util.UUID;

public class Leave {
    private UUID id;
    private UUID employeeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveType type;

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
