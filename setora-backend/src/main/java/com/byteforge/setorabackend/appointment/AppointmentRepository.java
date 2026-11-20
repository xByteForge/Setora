package com.byteforge.setorabackend.appointment;

import com.byteforge.setorabackend.appointment.dtos.AppointmentsTodaySummaryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.time.Instant;

public interface AppointmentRepository
        extends JpaRepository<Appointment, Integer>,
        JpaSpecificationExecutor<Appointment> {
    List<Appointment> findByEmployeeIdAndStatusNot(int employeeId, AppointmentStatus status);

    @Query("""
        SELECT
            COUNT(a) AS total,
            COALESCE(SUM(CASE WHEN a.status = :confirmed THEN 1 ELSE 0 END), 0) AS confirmed,
            COALESCE(SUM(CASE WHEN a.status = :pending THEN 1 ELSE 0 END), 0) AS pending,
            COALESCE(SUM(CASE WHEN a.status = :cancelled THEN 1 ELSE 0 END), 0) AS cancelled
        FROM Appointment a
        WHERE a.serviceItem.business.id = :businessId
          AND a.interval.start >= :start
          AND a.interval.end < :end
    """)
    AppointmentsTodaySummaryProjection getDailySummary(
            @Param("businessId") int businessId,
            @Param("start") Instant start,
            @Param("end") Instant end,
            @Param("confirmed") AppointmentStatus confirmed,
            @Param("pending") AppointmentStatus pending,
            @Param("cancelled") AppointmentStatus cancelled
    );

    @Query("""
        SELECT a FROM Appointment a
        JOIN FETCH a.employee e
        JOIN FETCH a.client c
        JOIN FETCH a.serviceItem s
        WHERE s.business.id = :businessId
        AND a.interval.start >= :startDate
        AND a.interval.end <= :endDate
        ORDER BY a.interval.start ASC
    """)
    List<Appointment> findByBusinessIdAndDateRange(
            @Param("businessId") Integer businessId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );



    @Query("""
    SELECT a
    FROM Appointment a
    WHERE a.employee.id = :employeeId
      AND a.interval.start >= :start
      AND a.interval.end < :end
""")
    List<Appointment> findByEmployee_IdOnDate(
            @Param("employeeId") int employeeId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );
}
