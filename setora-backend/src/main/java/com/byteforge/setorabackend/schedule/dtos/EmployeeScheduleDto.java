package com.byteforge.setorabackend.schedule.dtos;

import java.util.List;
import java.util.UUID;

public record EmployeeScheduleDto(
        UUID employeeId,
        String employeeName,
        String initials,
        String role,
        String location,
        List<WorkingHoursDto> workingHours,
        List<ScheduledAppointmentDto> appointments,
        List<UnavailableSlotDto> unavailableSlots
) {}
