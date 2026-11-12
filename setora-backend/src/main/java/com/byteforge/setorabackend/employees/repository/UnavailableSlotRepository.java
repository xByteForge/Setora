package com.byteforge.setorabackend.employees.repository;

import com.byteforge.setorabackend.employees.model.UnavailableSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface UnavailableSlotRepository extends JpaRepository<UnavailableSlot, UUID> {
    List<UnavailableSlot> findByEmployeeId(UUID employeeId);

    //all the recurring slots plus the one-off slots inside the date range
    @Query("""
            select s from UnavailableSlot s
            where s.employee.id = :employeeId
              and (s.dayOfWeek is not null or s.specificDate between :startDate and :endDate)
            """)
    List<UnavailableSlot> findForRange(@Param("employeeId") UUID employeeId,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);
}