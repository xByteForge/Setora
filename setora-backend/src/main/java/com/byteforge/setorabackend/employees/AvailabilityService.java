package com.byteforge.setorabackend.employees;

import com.byteforge.setorabackend.appointment.Appointment;
import com.byteforge.setorabackend.appointment.AppointmentRepository;
import com.byteforge.setorabackend.appointment.AppointmentStatus;
import com.byteforge.setorabackend.common.TimeInterval;
import com.byteforge.setorabackend.employees.AvailabilityDtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AvailabilityService {
    //longest range that can be requested at once
    private static final long MAX_RANGE_DAYS = 366;

    private final EmployeeRepository employeeRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final UnavailableSlotRepository unavailableSlotRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional(readOnly = true)
    public WorkingHours getWorkingHours(UUID employeeId) {
        Employee employee = findEmployee(employeeId);
        List<WorkingDay> days = workScheduleRepository.findByEmployeeId(employeeId).stream()
                .sorted(Comparator.comparing(WorkSchedule::getDayOfWeek))
                .map(schedule -> new WorkingDay(
                        schedule.getDayOfWeek(),
                        schedule.getInterval().getStart(),
                        schedule.getInterval().getEnd()))
                .toList();
        return new WorkingHours(employeeId, zoneOf(employee), days);
    }

    //pass either a single date or a from/to range
    @Transactional(readOnly = true)
    public UnavailableSlots getUnavailableSlots(UUID employeeId, LocalDate date, LocalDate from, LocalDate to) {
        Employee employee = findEmployee(employeeId);

        if (date != null && (from != null || to != null)) {
            throw badRequest("Use either date or from/to, not both");
        }
        LocalDate start = date != null ? date : from;
        LocalDate end = date != null ? date : to;
        if (start == null || end == null) {
            throw badRequest("Provide date, or both from and to");
        }
        if (start.isAfter(end)) {
            throw badRequest("from must not be after to");
        }
        if (ChronoUnit.DAYS.between(start, end) > MAX_RANGE_DAYS) {
            throw badRequest("The range cannot be longer than " + MAX_RANGE_DAYS + " days");
        }

        List<UnavailableSlot> slots = unavailableSlotRepository.findForRange(employeeId, start, end);
        List<UnavailableOccurrence> occurrences = start.datesUntil(end.plusDays(1))
                .flatMap(day -> slots.stream()
                        .filter(slot -> slot.occursOn(day))
                        .map(slot -> toOccurrence(slot, day)))
                .sorted(Comparator.comparing(UnavailableOccurrence::date)
                        .thenComparing(UnavailableOccurrence::start))
                .toList();
        return new UnavailableSlots(employeeId, zoneOf(employee), start, end, occurrences);
    }

    @Transactional
    public CreateSlotResponse createUnavailableSlot(UnavailableSlotRequest request) {
        if (request.employeeId() == null || request.type() == null) {
            throw badRequest("employeeId and type are required");
        }

        boolean oneOff = request.date() != null;
        boolean recurring = request.dayOfWeek() != null;
        if (oneOff == recurring) {
            throw badRequest("Provide either date (one-off) or dayOfWeek (recurring), not both");
        }
        TimeInterval interval = toInterval(request.start(), request.end());

        Employee employee = findEmployee(request.employeeId());
        ZoneId zone = zoneOf(employee);
        LocalDate today = LocalDate.now(zoneOf(employee));
        if (oneOff && request.date().isBefore(today)) {
            throw badRequest("The date cannot be in the past");
        }

        UnavailableSlot slot = new UnavailableSlot();
        slot.setEmployee(employee);
        slot.setType(request.type());
        slot.setReason(blankToNull(request.reason()));
        slot.setSpecificDate(request.date());
        slot.setDayOfWeek(request.dayOfWeek());
        slot.setInterval(interval);

        for (UnavailableSlot existing : unavailableSlotRepository.findByEmployeeId(request.employeeId())) {
            if (conflicts(slot, existing, today)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "The interval overlaps an existing unavailable interval");
            }
        }
        UnavailableSlot savedSlot = unavailableSlotRepository.save(slot);
        List<Appointment> activeAppointments = appointmentRepository
                .findByEmployeeIdAndStatusNot(request.employeeId(), AppointmentStatus.CANCELLED);
        List<Appointment> affectedAppointments = activeAppointments.stream()
                .filter(app -> isAppointmentAffected(app, savedSlot, zone, today))
                .toList();
        List<UUID> cancelledIds = new java.util.ArrayList<>();
        for (Appointment app : affectedAppointments) {
            app.setStatus(AppointmentStatus.CANCELLED);
            // Dacă dorești să lași un mesaj: app.setClientNote("Anulat automat: Angajat indisponibil");
            cancelledIds.add(app.getId());
        }

        if (!affectedAppointments.isEmpty()) {
            appointmentRepository.saveAll(affectedAppointments);
        }
        return new CreateSlotResponse(toResponse(savedSlot), cancelledIds);
    }

    @Transactional
    public void deleteUnavailableSlot(UUID slotId) {
        UnavailableSlot slot = unavailableSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unavailable slot not found"));
        unavailableSlotRepository.delete(slot);
    }

    //two slots conflict when they can fall on the same day and their times overlap
    //past one-off slots are ignored when compared with a recurring one
    private boolean conflicts(UnavailableSlot candidate, UnavailableSlot existing, LocalDate today) {
        if (!candidate.getInterval().overlaps(existing.getInterval())) {
            return false;
        }
        if (candidate.isRecurring() && existing.isRecurring()) {
            return candidate.getDayOfWeek() == existing.getDayOfWeek();
        }
        if (!candidate.isRecurring() && !existing.isRecurring()) {
            return candidate.getSpecificDate().equals(existing.getSpecificDate());
        }
        UnavailableSlot recurring = candidate.isRecurring() ? candidate : existing;
        UnavailableSlot oneOff = candidate.isRecurring() ? existing : candidate;
        return !oneOff.getSpecificDate().isBefore(today)
                && oneOff.getSpecificDate().getDayOfWeek() == recurring.getDayOfWeek();
    }

    private TimeInterval toInterval(LocalTime start, LocalTime end) {
        try {
            return new TimeInterval(start, end);
        } catch (IllegalArgumentException e) {
            throw badRequest("start and end are required and start must be before end");
        }
    }

    private Employee findEmployee(UUID employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
    }

    //the business timezone can be missing, UTC is used until it is set
    private ZoneId zoneOf(Employee employee) {
        ZoneId zone = employee.getBusiness().getTimezone();
        return zone != null ? zone : ZoneOffset.UTC;
    }

    private UnavailableOccurrence toOccurrence(UnavailableSlot slot, LocalDate day) {
        return new UnavailableOccurrence(
                slot.getId(),
                slot.getType(),
                slot.getReason(),
                slot.isRecurring(),
                day,
                slot.getInterval().getStart(),
                slot.getInterval().getEnd());
    }

    private UnavailableSlotResponse toResponse(UnavailableSlot slot) {
        return new UnavailableSlotResponse(
                slot.getId(),
                slot.getEmployee().getId(),
                slot.getType(),
                slot.getReason(),
                slot.isRecurring(),
                slot.getSpecificDate(),
                slot.getDayOfWeek(),
                slot.getInterval().getStart(),
                slot.getInterval().getEnd());
    }

    private String blankToNull(String text) {
        return text == null || text.isBlank() ? null : text.trim();
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private boolean isAppointmentAffected(Appointment app, UnavailableSlot slot, ZoneId zone, LocalDate today) {
        java.time.ZonedDateTime appZonedStart = app.getInterval().getStart().atZone(zone);
        java.time.ZonedDateTime appZonedEnd = app.getInterval().getEnd().atZone(zone);

        LocalDate appDate = appZonedStart.toLocalDate();
        LocalTime appStartTime = appZonedStart.toLocalTime();
        LocalTime appEndTime = appZonedEnd.toLocalTime();

        if (appDate.isBefore(today)) {
            return false;
        }

        boolean isSameDay = slot.isRecurring()
                ? appDate.getDayOfWeek() == slot.getDayOfWeek()
                : appDate.equals(slot.getSpecificDate());

        if (!isSameDay) {
            return false;
        }

        return appStartTime.isBefore(slot.getInterval().getEnd()) &&
                slot.getInterval().getStart().isBefore(appEndTime);
    }
}