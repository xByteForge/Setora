package com.byteforge.setorabackend.employees.repository;

import com.byteforge.setorabackend.employees.model.WorkSchedule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.DayOfWeek;
import java.util.Optional;

public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Integer> {
    List<WorkSchedule> findByEmployeeId(int employeeId);

    @Query("""
        SELECT ws FROM WorkSchedule ws
        JOIN FETCH ws.employee e
        JOIN FETCH e.user u
        LEFT JOIN FETCH e.serviceItems s
        WHERE e.business.id = :businessId
        AND e.active = true
    """)
    List<WorkSchedule> findByBusinessId(@Param("businessId") Integer businessId);

    Optional<WorkSchedule> findByEmployee_IdAndDayOfWeek(int employeeId, DayOfWeek dayOfWeek);
}
