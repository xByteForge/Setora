package com.byteforge.setorabackend.appointment.dtos;

import com.byteforge.setorabackend.appointment.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreateAppointmentExistingClientRequestDto(
        @NotNull int businessId,
        @NotNull int serviceId,
        @NotNull int employeeId,
        @NotNull LocalDate date,
        @NotNull LocalTime startTime,
        @NotNull PaymentMethod paymentMethod,
        String clientNote
) {}
//not needed now, as there is no existing front end