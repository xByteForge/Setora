package com.byteforge.setorabackend.appointment.dtos;

public record AppointmentsTodaySummaryDto(
        int number,
        int confirmed,
        int pending,
        int cancelled
) {
}