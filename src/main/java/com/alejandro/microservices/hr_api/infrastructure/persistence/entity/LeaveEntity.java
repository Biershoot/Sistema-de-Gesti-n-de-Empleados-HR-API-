package com.alejandro.microservices.hr_api.infrastructure.persistence.entity;

import com.alejandro.microservices.hr_api.domain.model.LeaveType;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "leaves")
public class LeaveEntity {

    @Id
    private UUID id;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveType type;

    public LeaveEntity() {}

    public LeaveEntity(UUID id, UUID employeeId, LocalDate startDate, LocalDate endDate, LeaveType type) {
        this.id = id;
        this.employeeId = employeeId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getEmployeeId() { return employeeId; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public LeaveType getType() { return type; }

    // Setters
    public void setId(UUID id) { this.id = id; }
    public void setEmployeeId(UUID employeeId) { this.employeeId = employeeId; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public void setType(LeaveType type) { this.type = type; }
}
