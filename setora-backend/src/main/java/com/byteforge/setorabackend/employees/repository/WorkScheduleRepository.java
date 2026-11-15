package com.byteforge.setorabackend.employees.repository;

import com.byteforge.setorabackend.employees.model.WorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Integer> {
    List<WorkSchedule> findByEmployeeId(int employeeId);
}
