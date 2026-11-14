package com.byteforge.setorabackend.appointment.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateAppointmentRequestDto(
        String surname,
        String firstname,

        @Email
        String email,
        String phoneNumber,

        @Size(max = 1000)
        String clientNote
) {
}
