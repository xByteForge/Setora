package com.byteforge.setorabackend.appointment;

import com.byteforge.setorabackend.appointment.dtos.AppointmentsTodaySummaryProjection;
import com.byteforge.setorabackend.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository
        extends JpaRepository<Appointment, UUID>,
        JpaSpecificationExecutor<Appointment> {
    List<Appointment> findByEmployeeIdAndStatusNot(UUID employeeId, AppointmentStatus status);

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
            @Param("businessId") UUID businessId,
            @Param("start") Instant start,
            @Param("end") Instant end,
            @Param("confirmed") AppointmentStatus confirmed,
            @Param("pending") AppointmentStatus pending,
            @Param("cancelled") AppointmentStatus cancelled
    );

    @Query("""
        SELECT DISTINCT a.client FROM Appointment a
        WHERE a.serviceItem.business.id = :businessId
          AND (
            :name IS NULL OR :name = '' OR
            LOWER(a.client.firstname) LIKE LOWER(CONCAT('%', :name, '%')) OR
            LOWER(a.client.surname) LIKE LOWER(CONCAT('%', :name, '%')) OR
            LOWER(CONCAT(a.client.firstname, ' ', a.client.surname)) LIKE LOWER(CONCAT('%', :name, '%')) OR
            LOWER(CONCAT(a.client.surname, ' ', a.client.firstname)) LIKE LOWER(CONCAT('%', :name, '%'))
          )
        ORDER BY a.client.firstname ASC, a.client.surname ASC
    """)
    List<User> searchClientsByBusiness(
            @Param("businessId") UUID businessId,
            @Param("name") String name
    );
}
