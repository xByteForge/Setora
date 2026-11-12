package com.byteforge.setorabackend.schedule.dtos;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record WorkingHoursDto(
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
) {}
