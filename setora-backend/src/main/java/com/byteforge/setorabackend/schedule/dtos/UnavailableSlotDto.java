package com.byteforge.setorabackend.schedule.dtos;

import com.byteforge.setorabackend.employees.model.UnavailableType;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;

public record UnavailableSlotDto(
        int id,
        Instant startTime,
        Instant endTime,
        UnavailableType type,
        String reason,
        DayOfWeek dayOfWeek,
        LocalDate specificDate,
        int durationMinutes) {}
