package com.byteforge.setorabackend.schedule.dtos;

import com.byteforge.setorabackend.appointment.AppointmentStatus;
import com.byteforge.setorabackend.appointment.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;

public record ScheduledAppointmentDto(
        int appointmentId,
        Integer clientId,
        String clientName,
        String clientPhone,
        String clientEmail,
        String clientNote,
        Instant start,
        Instant end,
        Instant createdAt,
        int durationMinutes,
        String location,
        AppointmentStatus status,
        BigDecimal priceAtAppointment,
        PaymentMethod paymentMethod,
        int serviceId,
        String serviceName
) {}