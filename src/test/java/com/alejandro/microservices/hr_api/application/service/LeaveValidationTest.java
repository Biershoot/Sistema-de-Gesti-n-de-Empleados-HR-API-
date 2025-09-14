package com.alejandro.microservices.hr_api.application.service;

import com.alejandro.microservices.hr_api.application.dto.LeaveRequestDTO;
import com.alejandro.microservices.hr_api.domain.model.Leave;
import com.alejandro.microservices.hr_api.domain.model.LeaveType;
import com.alejandro.microservices.hr_api.domain.repository.LeaveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para las validaciones de negocio del LeaveService.
 *
 * Estas pruebas verifican que las reglas de negocio se apliquen correctamente:
 * - Validación de fechas (inicio debe ser antes que fin)
 * - No permitir fechas pasadas para vacaciones (excepto licencias médicas)
 * - Prevenir solapamiento de permisos para el mismo empleado
 */
class LeaveValidationTest {

    @Mock
    private LeaveRepository leaveRepository;

    private LeaveService leaveService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        leaveService = new LeaveService(leaveRepository);
    }

    @Test
    void requestLeave_shouldThrowException_whenEndDateBeforeStartDate() {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        LocalDate startDate = LocalDate.of(2024, 6, 15);
        LocalDate endDate = LocalDate.of(2024, 6, 10); // Antes de la fecha de inicio

        LeaveRequestDTO request = new LeaveRequestDTO(
            employeeId, startDate, endDate, LeaveType.VACATION
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> leaveService.requestLeave(request)
        );

        assertEquals("La fecha de fin no puede ser anterior a la fecha de inicio", exception.getMessage());
        verify(leaveRepository, never()).save(any());
    }

    @Test
    void requestLeave_shouldThrowException_whenVacationDatesInPast() {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        LocalDate startDate = LocalDate.now().minusDays(5); // Fecha pasada
        LocalDate endDate = LocalDate.now().minusDays(1);

        LeaveRequestDTO request = new LeaveRequestDTO(
            employeeId, startDate, endDate, LeaveType.VACATION
        );

        when(leaveRepository.findOverlappingLeaves(employeeId, startDate, endDate, null))
            .thenReturn(List.of());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> leaveService.requestLeave(request)
        );

        assertEquals("No se pueden solicitar permisos con fechas pasadas", exception.getMessage());
        verify(leaveRepository, never()).save(any());
    }

    @Test
    void requestLeave_shouldAllowSickLeavesInPast() {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        LocalDate startDate = LocalDate.now().minusDays(3); // Fecha pasada
        LocalDate endDate = LocalDate.now().minusDays(1);

        LeaveRequestDTO request = new LeaveRequestDTO(
            employeeId, startDate, endDate, LeaveType.SICK // Licencia médica
        );

        when(leaveRepository.findOverlappingLeaves(employeeId, startDate, endDate, null))
            .thenReturn(List.of());

        Leave savedLeave = new Leave(UUID.randomUUID(), employeeId, startDate, endDate, LeaveType.SICK);
        when(leaveRepository.save(any())).thenReturn(savedLeave);

        // Act & Assert - No debería lanzar excepción
        assertDoesNotThrow(() -> leaveService.requestLeave(request));
        verify(leaveRepository).save(any());
    }

    @Test
    void requestLeave_shouldThrowException_whenDatesOverlapWithExistingLeave() {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        LocalDate startDate = LocalDate.now().plusDays(10);
        LocalDate endDate = LocalDate.now().plusDays(15);

        LeaveRequestDTO request = new LeaveRequestDTO(
            employeeId, startDate, endDate, LeaveType.VACATION
        );

        // Simular un permiso existente que se solapa
        Leave existingLeave = new Leave(
            UUID.randomUUID(),
            employeeId,
            LocalDate.now().plusDays(12), // Se solapa con el nuevo permiso
            LocalDate.now().plusDays(18),
            LeaveType.VACATION
        );

        when(leaveRepository.findOverlappingLeaves(employeeId, startDate, endDate, null))
            .thenReturn(List.of(existingLeave));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> leaveService.requestLeave(request)
        );

        assertEquals("El permiso se solapa con otros permisos existentes", exception.getMessage());
        verify(leaveRepository, never()).save(any());
    }

    @Test
    void requestLeave_shouldSucceed_whenNoOverlapAndValidDates() {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        LocalDate startDate = LocalDate.now().plusDays(10);
        LocalDate endDate = LocalDate.now().plusDays(15);

        LeaveRequestDTO request = new LeaveRequestDTO(
            employeeId, startDate, endDate, LeaveType.VACATION
        );

        when(leaveRepository.findOverlappingLeaves(employeeId, startDate, endDate, null))
            .thenReturn(List.of()); // Sin solapamientos

        Leave savedLeave = new Leave(UUID.randomUUID(), employeeId, startDate, endDate, LeaveType.VACATION);
        when(leaveRepository.save(any())).thenReturn(savedLeave);

        // Act & Assert - No debería lanzar excepción
        assertDoesNotThrow(() -> leaveService.requestLeave(request));
        verify(leaveRepository).save(any());
    }

    @Test
    void requestLeave_shouldThrowException_whenRequestIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> leaveService.requestLeave(null)
        );

        assertTrue(exception.getMessage().contains("datos"));
        verify(leaveRepository, never()).save(any());
    }

    @Test
    void requestLeave_shouldThrowException_whenDatesAreNull() {
        // Arrange
        LeaveRequestDTO request = new LeaveRequestDTO(
            UUID.randomUUID(), null, null, LeaveType.VACATION
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> leaveService.requestLeave(request)
        );

        assertEquals("Las fechas de inicio y fin son requeridas", exception.getMessage());
        verify(leaveRepository, never()).save(any());
    }

    @Test
    void requestLeave_shouldThrowException_whenLeaveTypeIsNull() {
        // Arrange
        LeaveRequestDTO request = new LeaveRequestDTO(
            UUID.randomUUID(),
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(3),
            null
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> leaveService.requestLeave(request)
        );

        assertEquals("El tipo de permiso es requerido", exception.getMessage());
        verify(leaveRepository, never()).save(any());
    }

    @Test
    void requestLeave_shouldHandleComplexOverlapScenarios() {
        // Arrange - Caso donde el nuevo permiso engloba uno existente
        UUID employeeId = UUID.randomUUID();
        LocalDate startDate = LocalDate.now().plusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(20);

        LeaveRequestDTO request = new LeaveRequestDTO(
            employeeId, startDate, endDate, LeaveType.VACATION
        );

        // Permiso existente dentro del rango del nuevo permiso
        Leave existingLeave = new Leave(
            UUID.randomUUID(),
            employeeId,
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(15),
            LeaveType.SICK
        );

        when(leaveRepository.findOverlappingLeaves(employeeId, startDate, endDate, null))
            .thenReturn(List.of(existingLeave));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> leaveService.requestLeave(request)
        );

        assertEquals("El permiso se solapa con otros permisos existentes", exception.getMessage());
    }
}
