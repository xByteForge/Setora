package com.byteforge.setorabackend.appointment.dtos;

import com.byteforge.setorabackend.appointment.PaymentMethod;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record CreateAppointmentGhostClientRequestDto (
    @NotNull UUID businessId,//TODO remove later on when we have sessions
    @NotNull UUID serviceId,
    @NotNull UUID employeeId,

    @NotBlank String clientName,
    @NotBlank String clientPhone,
    @Email
    @NotBlank String clientEmail,

    @NotNull
    LocalDate date,
    @NotNull
    LocalTime startTime,

    @NotNull
    PaymentMethod paymentMethod,

    @Size(max = 1000) String clientNote
){}