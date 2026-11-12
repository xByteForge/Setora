package com.byteforge.setorabackend.appointment.dtos;

import com.byteforge.setorabackend.appointment.AppointmentStatus;
import com.byteforge.setorabackend.appointment.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AppointmentResponseDto(
        UUID id,
        UUID businessId,

        UUID serviceId,
        String serviceName,
        int durationMinutes,

        UUID clientId,
        String clientName,
        String clientPhone,
        String clientEmail,

        UUID employeeId,
        String employeeName,

        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,

        AppointmentStatus status,

        BigDecimal price,
        String currency,

        PaymentMethod paymentMethod,
        boolean isPaid,

        String clientNote
) {}