package com.byteforge.setorabackend.appointment.dtos;

import com.byteforge.setorabackend.appointment.AppointmentStatus;
import com.byteforge.setorabackend.appointment.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record CreateAppointmentRequestDto(
        Integer businessId,
        @NotNull(message = "serviceId is required") Integer serviceId,
        @NotNull(message = "employeeId is required") Integer employeeId,
        Integer clientId,
        String clientName,
        String clientEmail,
        String clientPhone,
        @NotNull(message = "date is required") LocalDate date,
        @NotNull(message = "startTime is required") LocalTime startTime,
        PaymentMethod paymentMethod,
        AppointmentStatus status,
        String clientNote) {}
