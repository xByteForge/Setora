package com.byteforge.setorabackend.schedule.mapper;

import com.byteforge.setorabackend.appointment.Appointment;
import com.byteforge.setorabackend.employees.model.UnavailableSlot;
import com.byteforge.setorabackend.schedule.dtos.EmployeeScheduleDto;
import com.byteforge.setorabackend.schedule.dtos.ScheduledAppointmentDto;
import com.byteforge.setorabackend.schedule.dtos.UnavailableSlotDto;
import com.byteforge.setorabackend.schedule.dtos.WorkingHoursDto;
import com.byteforge.setorabackend.employees.model.Employee;
import com.byteforge.setorabackend.employees.model.WorkSchedule;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Component
public class ScheduleMapper {

    public EmployeeScheduleDto toEmployeeScheduleDto(
            Employee employee,
            List<WorkSchedule> workSchedules,
            List<Appointment> appointments,
            List<UnavailableSlot> unavailableSlots,
            LocalDate startDate,
            LocalDate endDate,
            ZoneId zoneId) {

        List<WorkingHoursDto> workingHours = workSchedules.stream()
                .map(this::toWorkingHoursDto)
                .toList();

        List<ScheduledAppointmentDto> scheduledAppointments = appointments.stream()
                .map(this::toScheduledAppointmentDto)
                .toList();

        List<UnavailableSlotDto> projectedSlots = projectUnavailableSlots(
                unavailableSlots, startDate, endDate, zoneId);

        String name = employee.getUser().getFullName();
        String role = employee.getServiceItems().isEmpty() ?
                "Specialist" : employee.getServiceItems().iterator().next().getName();

        return new EmployeeScheduleDto(
                employee.getId(),
                name,
                employee.getUser().getNameInitials(),
                role,
                employee.getBusiness().getAddress(),
                workingHours,
                scheduledAppointments,
                projectedSlots
        );
    }

    private WorkingHoursDto toWorkingHoursDto(WorkSchedule workSchedule) {
        return new WorkingHoursDto(
                workSchedule.getDayOfWeek(),
                workSchedule.getInterval().getStart(),
                workSchedule.getInterval().getEnd()
        );
    }

    public ScheduledAppointmentDto toScheduledAppointmentDto(Appointment appointment) {
        return new ScheduledAppointmentDto(
                appointment.getId(),
                appointment.getClient().getId(),
                appointment.getClient().getFullName(),
                "0700123456",
                appointment.getClient().getEmail(),
                appointment.getServiceItem().getId(),
                appointment.getServiceItem().getName(),
                appointment.getInterval().getStart(),
                appointment.getInterval().getEnd(),
                appointment.getStatus().name().toLowerCase(),
                appointment.getServiceItem().getDurationMinutes(),
                appointment.getPriceAtAppointment(),
                "RON",
                appointment.getPaymentMethod(),
                appointment.isPaid(),
                appointment.getClientNote()
        );
    }

    private List<UnavailableSlotDto> projectUnavailableSlots(
            List<UnavailableSlot> slots, LocalDate startDate, LocalDate endDate, ZoneId zoneId) {

        List<UnavailableSlotDto> projectedBlocks = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            for (UnavailableSlot slot : slots) {
                if (slot.occursOn(currentDate)) {
                    Instant startInstant = currentDate.atTime(slot.getInterval().getStart()).atZone(zoneId).toInstant();
                    Instant endInstant = currentDate.atTime(slot.getInterval().getEnd()).atZone(zoneId).toInstant();

                    int durationMinutes = (int) java.time.Duration.between(startInstant, endInstant).toMinutes();

                    projectedBlocks.add(new UnavailableSlotDto(
                            slot.getId(),
                            startInstant,
                            endInstant,
                            slot.getType(),
                            slot.getReason(),
                            durationMinutes
                    ));
                }
            }
            currentDate = currentDate.plusDays(1);
        }

        return projectedBlocks;
    }
}