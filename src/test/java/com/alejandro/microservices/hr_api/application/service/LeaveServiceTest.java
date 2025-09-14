package com.alejandro.microservices.hr_api.application.service;

import com.alejandro.microservices.hr_api.application.dto.LeaveRequestDTO;
import com.alejandro.microservices.hr_api.application.dto.LeaveResponseDTO;
import com.alejandro.microservices.hr_api.domain.model.Leave;
import com.alejandro.microservices.hr_api.domain.model.LeaveType;
import com.alejandro.microservices.hr_api.domain.repository.LeaveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock
    private LeaveRepository leaveRepository;

    @InjectMocks
    private LeaveService leaveService;

    private UUID employeeId;
    private LeaveRequestDTO validRequest;
    private Leave mockLeave;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        validRequest = new LeaveRequestDTO(
                employeeId,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5),
                LeaveType.VACATION
        );

        mockLeave = new Leave(
                UUID.randomUUID(),
                employeeId,
                validRequest.startDate(),
                validRequest.endDate(),
                validRequest.type()
        );
    }

    @Test
    void shouldRequestLeaveSuccessfully() {
        // Arrange
        when(leaveRepository.save(any(Leave.class))).thenReturn(mockLeave);

        // Act
        LeaveResponseDTO result = leaveService.requestLeave(validRequest);

        // Assert
        assertNotNull(result);
        assertEquals(employeeId, result.employeeId());
        assertEquals(LeaveType.VACATION, result.type());
        verify(leaveRepository).save(any(Leave.class));
    }

    @Test
    void shouldThrowExceptionWhenStartDateAfterEndDate() {
        // Arrange
        LeaveRequestDTO invalidRequest = new LeaveRequestDTO(
                employeeId,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(1), // End before start
                LeaveType.VACATION
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> leaveService.requestLeave(invalidRequest)
        );
        assertEquals("La fecha de inicio debe ser anterior a la fecha de fin", exception.getMessage());
        verify(leaveRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenRequestingPastDateVacation() {
        // Arrange
        LeaveRequestDTO pastDateRequest = new LeaveRequestDTO(
                employeeId,
                LocalDate.now().minusDays(1), // Past date
                LocalDate.now().plusDays(1),
                LeaveType.VACATION
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> leaveService.requestLeave(pastDateRequest)
        );
        assertEquals("No se pueden solicitar permisos con fechas pasadas", exception.getMessage());
    }

    @Test
    void shouldAllowPastDateForSickLeave() {
        // Arrange
        LeaveRequestDTO sickLeaveRequest = new LeaveRequestDTO(
                employeeId,
                LocalDate.now().minusDays(1), // Past date but sick leave
                LocalDate.now().plusDays(1),
                LeaveType.SICK
        );
        when(leaveRepository.save(any(Leave.class))).thenReturn(mockLeave);

        // Act
        LeaveResponseDTO result = leaveService.requestLeave(sickLeaveRequest);

        // Assert
        assertNotNull(result);
        verify(leaveRepository).save(any(Leave.class));
    }

    @Test
    void shouldGetLeavesByEmployee() {
        // Arrange
        List<Leave> leaves = Arrays.asList(mockLeave);
        when(leaveRepository.findByEmployeeId(employeeId)).thenReturn(leaves);

        // Act
        List<LeaveResponseDTO> result = leaveService.getLeavesByEmployee(employeeId);

        // Assert
        assertEquals(1, result.size());
        assertEquals(employeeId, result.get(0).employeeId());
        verify(leaveRepository).findByEmployeeId(employeeId);
    }

    @Test
    void shouldGetLeaveById() {
        // Arrange
        UUID leaveId = UUID.randomUUID();
        when(leaveRepository.findById(leaveId)).thenReturn(Optional.of(mockLeave));

        // Act
        LeaveResponseDTO result = leaveService.getLeaveById(leaveId);

        // Assert
        assertNotNull(result);
        assertEquals(employeeId, result.employeeId());
        verify(leaveRepository).findById(leaveId);
    }

    @Test
    void shouldThrowExceptionWhenLeaveNotFound() {
        // Arrange
        UUID leaveId = UUID.randomUUID();
        when(leaveRepository.findById(leaveId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> leaveService.getLeaveById(leaveId)
        );
        assertEquals("Permiso no encontrado", exception.getMessage());
    }

    @Test
    void shouldCancelLeaveSuccessfully() {
        // Arrange
        UUID leaveId = UUID.randomUUID();
        when(leaveRepository.findById(leaveId)).thenReturn(Optional.of(mockLeave));

        // Act
        leaveService.cancelLeave(leaveId);

        // Assert
        verify(leaveRepository).deleteById(leaveId);
    }

    @Test
    void shouldThrowExceptionWhenCancellingNonExistentLeave() {
        // Arrange
        UUID leaveId = UUID.randomUUID();
        when(leaveRepository.findById(leaveId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> leaveService.cancelLeave(leaveId)
        );
        assertEquals("Permiso no encontrado", exception.getMessage());
        verify(leaveRepository, never()).deleteById(any());
    }
}
