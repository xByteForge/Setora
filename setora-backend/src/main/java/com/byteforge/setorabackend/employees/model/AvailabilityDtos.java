package com.byteforge.setorabackend.employees.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

// request and response bodies of the availability endpoints
// all times are local times in the timezone of the business, the end is exclusive
public final class AvailabilityDtos {
    private AvailabilityDtos() {}

    public record WorkingDay(DayOfWeek dayOfWeek, LocalTime start, LocalTime end) {}

    public record WorkingHours(int employeeId, ZoneId timezone, List<WorkingDay> days) {}

    // send either date (one-off slot) or dayOfWeek (recurring weekly slot)
    public record UnavailableSlotRequest(
            UnavailableType type, String reason, LocalDate date, DayOfWeek dayOfWeek, LocalTime start, LocalTime end) {}

    // the slot as it is stored
    public record UnavailableSlotResponse(
            int id,
            int employeeId,
            UnavailableType type,
            String reason,
            boolean recurring,
            LocalDate date,
            DayOfWeek dayOfWeek,
            LocalTime start,
            LocalTime end) {}

    // the slot on one concrete date, recurring slots are expanded for every date in the range
    public record UnavailableOccurrence(
            int slotId,
            UnavailableType type,
            String reason,
            boolean recurring,
            LocalDate date,
            LocalTime start,
            LocalTime end) {}

    public record UnavailableSlots(
            int employeeId, ZoneId timezone, LocalDate from, LocalDate to, List<UnavailableOccurrence> slots) {}

    public record CreateSlotResponse(UnavailableSlotResponse slot, List<Integer> cancelledAppointmentIds) {}
}
