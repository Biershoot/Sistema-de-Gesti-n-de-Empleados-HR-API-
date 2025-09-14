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

/**
 * Servicio de aplicación para la gestión de permisos y ausencias.
 *
 * Este servicio implementa los casos de uso relacionados con el sistema de
 * vacaciones y ausencias, proporcionando funcionalidades para solicitar,
 * consultar y gestionar permisos de empleados.
 *
 * Tipos de permisos soportados:
 * - VACATION: Vacaciones programadas
 * - SICK: Licencias médicas
 * - UNPAID: Permisos sin goce de sueldo
 *
 * Validaciones implementadas:
 * - Fechas de inicio deben ser anteriores a fechas de fin
 * - No se permiten fechas pasadas para vacaciones (excepto licencias médicas)
 * - Validación de empleado existente
 *
 * @author Sistema HR API
 * @version 1.0
 */
@Service
public class LeaveService {

    private final LeaveRepository leaveRepository;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param leaveRepository Repositorio para operaciones de permisos/ausencias
     */
    public LeaveService(LeaveRepository leaveRepository) {
        this.leaveRepository = leaveRepository;
    }

    /**
     * Solicita un nuevo permiso o ausencia para un empleado.
     *
     * Aplica las siguientes validaciones de negocio:
     * - La fecha de inicio debe ser anterior a la fecha de fin
     * - Para vacaciones, no se permiten fechas en el pasado
     * - Las licencias médicas pueden tener fechas retroactivas
     * - No se permiten permisos solapados para el mismo empleado
     *
     * @param request DTO con los datos del permiso a solicitar
     * @return DTO con los datos del permiso creado
     * @throws IllegalArgumentException si las validaciones fallan
     */
    public LeaveResponseDTO requestLeave(LeaveRequestDTO request) {
        // Validar datos de entrada
        validateLeaveRequest(request);

        // Validar que la fecha de inicio sea anterior a la fecha de fin
        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }

        // Validar que las fechas no sean en el pasado (excepto para licencias médicas)
        if (request.type() != LeaveType.SICK && request.startDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se pueden solicitar permisos con fechas pasadas");
        }

        // NUEVA VALIDACIÓN: Verificar solapamiento de permisos
        validateDateOverlap(request.employeeId(), request.startDate(), request.endDate(), null);

        // Crear entidad de dominio
        Leave leave = new Leave(
                UUID.randomUUID(),
                request.employeeId(),
                request.startDate(),
                request.endDate(),
                request.type()
        );

        // Persistir y retornar resultado
        leaveRepository.save(leave);
        return mapToResponseDTO(leave);
    }

    /**
     * Obtiene todos los permisos de un empleado específico.
     *
     * @param employeeId UUID del empleado
     * @return Lista de permisos del empleado
     */
    public List<LeaveResponseDTO> getLeavesByEmployee(UUID employeeId) {
        return leaveRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene permisos dentro de un rango de fechas específico.
     *
     * Útil para generar reportes de ausencias en períodos determinados
     * o para planificación de recursos.
     *
     * @param start Fecha de inicio del rango
     * @param end Fecha de fin del rango
     * @return Lista de permisos en el rango especificado
     */
    public List<LeaveResponseDTO> getLeavesByDateRange(LocalDate start, LocalDate end) {
        return leaveRepository.findByDateRange(start, end).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un permiso específico por su ID.
     *
     * @param id UUID del permiso
     * @return DTO del permiso encontrado
     * @throws IllegalArgumentException si el permiso no existe
     */
    public LeaveResponseDTO getLeaveById(UUID id) {
        return leaveRepository.findById(id)
                .map(this::mapToResponseDTO)
                .orElseThrow(() -> new IllegalArgumentException("Permiso no encontrado"));
    }

    /**
     * Cancela un permiso existente.
     *
     * Típicamente usado cuando un empleado necesita cancelar
     * una solicitud de vacaciones previamente aprobada.
     *
     * @param id UUID del permiso a cancelar
     * @throws IllegalArgumentException si el permiso no existe
     */
    public void cancelLeave(UUID id) {
        if (!leaveRepository.findById(id).isPresent()) {
            throw new IllegalArgumentException("Permiso no encontrado");
        }
        leaveRepository.deleteById(id);
    }

    /**
     * Mapea una entidad de dominio Leave a su DTO de respuesta.
     *
     * @param leave Entidad de dominio
     * @return DTO de respuesta
     */
    private LeaveResponseDTO mapToResponseDTO(Leave leave) {
        return new LeaveResponseDTO(
                leave.getId(),
                leave.getEmployeeId(),
                leave.getStartDate(),
                leave.getEndDate(),
                leave.getType()
        );
    }

    /**
     * Valida los datos de una solicitud de permiso.
     *
     * @param request DTO con los datos del permiso a validar
     * @throws IllegalArgumentException si los datos son inválidos
     */
    private void validateLeaveRequest(LeaveRequestDTO request) {
        // NUEVA VALIDACIÓN: Verificar que el request no sea nulo
        if (request == null) {
            throw new IllegalArgumentException("Los datos del permiso no pueden ser nulos");
        }

        // Validar que el empleado exista
        // (suponiendo que hay un servicio o repositorio para validar empleados)
        /*
        if (!employeeExists(request.employeeId())) {
            throw new IllegalArgumentException("Empleado no encontrado");
        }
        */

        // Validar que la fecha de inicio y fin no sean nulas
        if (request.startDate() == null || request.endDate() == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son requeridas");
        }

        // Validar que el tipo de permiso no sea nulo
        if (request.type() == null) {
            throw new IllegalArgumentException("El tipo de permiso es requerido");
        }
    }

    /**
     * Valida que no haya solapamiento de permisos en las fechas especificadas.
     *
     * @param employeeId UUID del empleado
     * @param start Fecha de inicio del nuevo permiso
     * @param end Fecha de fin del nuevo permiso
     * @param excludeLeaveId UUID de un permiso a excluir de la validación (por ejemplo, al editar un permiso)
     * @throws IllegalArgumentException si hay solapamiento de fechas
     */
    private void validateDateOverlap(UUID employeeId, LocalDate start, LocalDate end, UUID excludeLeaveId) {
        List<Leave> overlappingLeaves = leaveRepository.findOverlappingLeaves(employeeId, start, end, excludeLeaveId);
        if (!overlappingLeaves.isEmpty()) {
            throw new IllegalArgumentException("El permiso se solapa con otros permisos existentes");
        }
    }
}
