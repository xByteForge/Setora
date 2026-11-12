package com.byteforge.setorabackend.schedule.dtos;

import com.byteforge.setorabackend.employees.model.UnavailableType;

import java.time.Instant;
import java.util.UUID;

public record UnavailableSlotDto(
        UUID id,
        Instant start,
        Instant end,
        UnavailableType type,
        String reason,
        int durationMinutes
) {}