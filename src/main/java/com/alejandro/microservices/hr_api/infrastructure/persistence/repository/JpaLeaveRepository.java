package com.alejandro.microservices.hr_api.infrastructure.persistence.repository;

import com.alejandro.microservices.hr_api.infrastructure.persistence.entity.LeaveEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface JpaLeaveRepository extends JpaRepository<LeaveEntity, UUID> {
    List<LeaveEntity> findByEmployeeId(UUID employeeId);

    @Query("SELECT l FROM LeaveEntity l WHERE l.startDate BETWEEN :start AND :end OR l.endDate BETWEEN :start AND :end")
    List<LeaveEntity> findByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    /**
     * Busca permisos que se solapen con el rango de fechas para un empleado específico.
     * Un solapamiento ocurre cuando:
     * - La fecha de inicio del nuevo permiso está entre las fechas de un permiso existente
     * - La fecha de fin del nuevo permiso está entre las fechas de un permiso existente
     * - El nuevo permiso engloba completamente un permiso existente
     */
    @Query("SELECT l FROM LeaveEntity l WHERE l.employeeId = :employeeId AND " +
           "((l.startDate <= :startDate AND l.endDate >= :startDate) OR " +
           "(l.startDate <= :endDate AND l.endDate >= :endDate) OR " +
           "(l.startDate >= :startDate AND l.endDate <= :endDate))")
    List<LeaveEntity> findOverlappingLeaves(@Param("employeeId") UUID employeeId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    /**
     * Busca permisos que se solapen excluyendo un permiso específico (útil para updates).
     */
    @Query("SELECT l FROM LeaveEntity l WHERE l.employeeId = :employeeId AND l.id != :excludeId AND " +
           "((l.startDate <= :startDate AND l.endDate >= :startDate) OR " +
           "(l.startDate <= :endDate AND l.endDate >= :endDate) OR " +
           "(l.startDate >= :startDate AND l.endDate <= :endDate))")
    List<LeaveEntity> findOverlappingLeavesExcluding(@Param("employeeId") UUID employeeId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate,
                                                     @Param("excludeId") UUID excludeId);
}
