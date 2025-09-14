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
}
