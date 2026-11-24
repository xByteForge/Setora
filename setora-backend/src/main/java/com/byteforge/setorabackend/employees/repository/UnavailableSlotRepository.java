package com.byteforge.setorabackend.employees.repository;

import com.byteforge.setorabackend.employees.model.UnavailableSlot;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UnavailableSlotRepository extends JpaRepository<UnavailableSlot, Integer> {
    List<UnavailableSlot> findByEmployeeId(int employeeId);

    // all the recurring slots plus the one-off slots inside the date range
    @Query("""
            select s from UnavailableSlot s
            where s.employee.id = :employeeId
              and (s.dayOfWeek is not null or s.specificDate between :startDate and :endDate)
            """)
    List<UnavailableSlot> findForRange(
            @Param("employeeId") int employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("""
        SELECT u FROM UnavailableSlot u
        JOIN FETCH u.employee e
        WHERE e.business.id = :businessId
          AND (u.dayOfWeek IS NOT NULL
               OR (u.specificDate >= :startDate AND u.specificDate <= :endDate))
   \s""")
    List<UnavailableSlot> findByBusinessIdAndDateRangeOrDayNotNull(
            @Param("businessId") Integer businessId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
