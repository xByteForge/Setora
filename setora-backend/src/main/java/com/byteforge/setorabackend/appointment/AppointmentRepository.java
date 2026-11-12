package com.byteforge.setorabackend.appointment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
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
            @Param("businessId") UUID businessId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );
}
