package com.alejandro.microservices.hr_api.infrastructure.persistence.adapter;

import com.alejandro.microservices.hr_api.domain.model.Leave;
import com.alejandro.microservices.hr_api.domain.repository.LeaveRepository;
import com.alejandro.microservices.hr_api.infrastructure.persistence.entity.LeaveEntity;
import com.alejandro.microservices.hr_api.infrastructure.persistence.repository.JpaLeaveRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class LeaveRepositoryAdapter implements LeaveRepository {

    private final JpaLeaveRepository jpaRepository;

    public LeaveRepositoryAdapter(JpaLeaveRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Leave save(Leave leave) {
        LeaveEntity entity = mapToEntity(leave);
        LeaveEntity saved = jpaRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public List<Leave> findByEmployeeId(UUID employeeId) {
        return jpaRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Leave> findByDateRange(LocalDate start, LocalDate end) {
        return jpaRepository.findByDateRange(start, end).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Leave> findById(UUID id) {
        return jpaRepository.findById(id).map(this::mapToDomain);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    private LeaveEntity mapToEntity(Leave leave) {
        return new LeaveEntity(
                leave.getId(),
                leave.getEmployeeId(),
                leave.getStartDate(),
                leave.getEndDate(),
                leave.getType()
        );
    }

    private Leave mapToDomain(LeaveEntity entity) {
        return new Leave(
                entity.getId(),
                entity.getEmployeeId(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getType()
        );
    }
}
