package com.alejandro.microservices.hr_api.domain.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class LeaveTest {

    @Test
    void shouldCreateLeaveSuccessfully() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        LocalDate startDate = LocalDate.of(2024, 12, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 5);
        LeaveType type = LeaveType.VACATION;

        // Act
        Leave leave = new Leave(id, employeeId, startDate, endDate, type);

        // Assert
        assertEquals(id, leave.getId());
        assertEquals(employeeId, leave.getEmployeeId());
        assertEquals(startDate, leave.getStartDate());
        assertEquals(endDate, leave.getEndDate());
        assertEquals(type, leave.getType());
    }

    @Test
    void shouldCreateSickLeave() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(3);
        LeaveType type = LeaveType.SICK;

        // Act
        Leave leave = new Leave(id, employeeId, startDate, endDate, type);

        // Assert
        assertEquals(LeaveType.SICK, leave.getType());
    }

    @Test
    void shouldCreateUnpaidLeave() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(10);
        LeaveType type = LeaveType.UNPAID;

        // Act
        Leave leave = new Leave(id, employeeId, startDate, endDate, type);

        // Assert
        assertEquals(LeaveType.UNPAID, leave.getType());
    }
}
