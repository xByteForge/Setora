package com.byteforge.setorabackend.employees;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Integer> {
    @Query("""
        SELECT ws FROM WorkSchedule ws
        JOIN FETCH ws.employee e
        JOIN FETCH e.user u
        LEFT JOIN FETCH e.serviceItems s
        WHERE e.business.id = :businessId
        AND e.active = true
    """)
    List<WorkSchedule> findByBusinessId(@Param("businessId") Integer businessId);
}
