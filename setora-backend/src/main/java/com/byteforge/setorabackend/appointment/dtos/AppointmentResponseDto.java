package com.byteforge.setorabackend.appointment.dtos;

import com.byteforge.setorabackend.appointment.AppointmentStatus;
import com.byteforge.setorabackend.appointment.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentResponseDto(
        int id,
        int businessId,

        int serviceId,
        String serviceName,
        int durationMinutes,

        Integer clientId,
        String clientName,
        String clientPhone,
        String clientEmail,

        int employeeId,
        String employeeName,

        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,

        AppointmentStatus status,

        BigDecimal price,

        PaymentMethod paymentMethod,

        String clientNote
) {}
