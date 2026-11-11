package com.byteforge.setorabackend.employees;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, UUID> {
}
