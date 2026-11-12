package com.byteforge.setorabackend.employees.repository;

import com.byteforge.setorabackend.employees.model.WorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, UUID> {
    List<WorkSchedule> findByEmployeeId(UUID employeeId);
}
