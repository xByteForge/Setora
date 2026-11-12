package com.byteforge.setorabackend.schedule.mapper;

import com.byteforge.setorabackend.appointment.Appointment;
import com.byteforge.setorabackend.schedule.dtos.EmployeeScheduleDto;
import com.byteforge.setorabackend.schedule.dtos.ScheduledAppointmentDto;
import com.byteforge.setorabackend.schedule.dtos.WorkingHoursDto;
import com.byteforge.setorabackend.employees.Employee;
import com.byteforge.setorabackend.employees.WorkSchedule;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ScheduleMapper {

    public EmployeeScheduleDto toEmployeeScheduleDto(
            Employee employee,
            List<WorkSchedule> workSchedules,
            List<Appointment> appointments) {

        List<WorkingHoursDto> workingHours = workSchedules.stream()
                .map(this::toWorkingHoursDto)
                .toList();

        List<ScheduledAppointmentDto> scheduledAppointments = appointments.stream()
                .map(this::toScheduledAppointmentDto)
                .toList();

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
                scheduledAppointments
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
}