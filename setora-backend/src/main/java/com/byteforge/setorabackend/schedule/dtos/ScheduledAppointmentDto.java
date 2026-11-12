package com.byteforge.setorabackend.schedule.dtos;

import com.byteforge.setorabackend.appointment.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ScheduledAppointmentDto(
        UUID appointmentId,
        UUID clientId,
        String clientName,
        String clientPhone,
        String clientEmail,
        UUID serviceId,
        String serviceName,
        Instant startsAt,
        Instant endsAt,
        String status,
        Integer durationMinutes,
        BigDecimal price,
        String currency,
        PaymentMethod paymentMethod,
        Boolean isPaid,
        String clientNote
) {}