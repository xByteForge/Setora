package com.byteforge.setorabackend.schedule.mapper;

import com.byteforge.setorabackend.appointment.Appointment;
import com.byteforge.setorabackend.auth.User;
import com.byteforge.setorabackend.employees.model.UnavailableSlot;
import com.byteforge.setorabackend.schedule.dtos.EmployeeScheduleDto;
import com.byteforge.setorabackend.schedule.dtos.ScheduledAppointmentDto;
import com.byteforge.setorabackend.schedule.dtos.UnavailableSlotDto;
import com.byteforge.setorabackend.schedule.dtos.WorkingHoursDto;
import com.byteforge.setorabackend.employees.model.Employee;
import com.byteforge.setorabackend.employees.model.WorkSchedule;
import org.springframework.stereotype.Component;

import java.time.Duration;
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

        String name = employee.getUser().getName();
        String role = employee.getEmploymentRole() == null ?
                "Specialist" : employee.getEmploymentRole();

        return new EmployeeScheduleDto(
                employee.getId(),
                name,
                employee.getUser().getNameInitials(),
                role,
                employee.getBusiness().getLocation().getLocationName(),
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
        User client = appointment.getClient();
        Integer clientId = null;
        String clientName = appointment.getClientName();
        String clientPhone = appointment.getClientPhone();
        String clientEmail = appointment.getClientEmail();
        if (client != null) {
            clientId = client.getId();
            clientName = client.getName();
            clientPhone = client.getPhoneNumber();
            clientEmail = client.getEmail();
        }

        int durationMinutes = (int) Duration.between(
                appointment.getInterval().getStart(),
                appointment.getInterval().getEnd()
        ).toMinutes();

        return new ScheduledAppointmentDto(
                appointment.getId(),
                clientId,
                clientName,
                clientPhone,
                clientEmail,
                appointment.getClientNote(),
                appointment.getInterval().getStart(),
                appointment.getInterval().getEnd(),
                appointment.getCreatedAt(),
                durationMinutes,
                appointment.getEmployee().getBusiness().getLocation().getLocationName(),
                appointment.getStatus(),
                appointment.getPriceAtAppointment(),
                appointment.getPaymentMethod(),
                appointment.getServiceItem().getId(),
                appointment.getServiceItem().getName()
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

                    int durationMinutes = (int) Duration.between(startInstant, endInstant).toMinutes();

                    projectedBlocks.add(new UnavailableSlotDto(
                            slot.getId(),
                            startInstant,
                            endInstant,
                            slot.getType(),
                            slot.getReason(),
                            slot.getDayOfWeek(),
                            slot.getSpecificDate(),
                            durationMinutes
                    ));
                }
            }
            currentDate = currentDate.plusDays(1);
        }

        return projectedBlocks;
    }
}