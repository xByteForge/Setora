package com.byteforge.setorabackend.schedule.dtos;

import java.util.List;

public record EmployeeScheduleDto(
        int employeeId,
        String employeeName,
        String initials,
        String employmentRole,
        String location,
        List<WorkingHoursDto> workingHours,
        List<ScheduledAppointmentDto> appointments,
        List<UnavailableSlotDto> unavailableSlots) {}
