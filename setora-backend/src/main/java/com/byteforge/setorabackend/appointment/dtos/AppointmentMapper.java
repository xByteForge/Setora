package com.byteforge.setorabackend.appointment.dtos;
import com.byteforge.setorabackend.appointment.Appointment;
import com.byteforge.setorabackend.appointment.AppointmentStatus;
import com.byteforge.setorabackend.common.DateTimeInterval;
import com.byteforge.setorabackend.employees.Employee;
import com.byteforge.setorabackend.serviceItem.ServiceItem;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.Duration;

public class AppointmentMapper {

    public static AppointmentResponseDto toResponseDto(Appointment appointment) {
        var service = appointment.getServiceItem();
        var business = service.getBusiness();
        var client = appointment.getClient();
        var employee = appointment.getEmployee();

        ZoneId timezone = business.getTimezone();

        Instant startsAt = appointment.getInterval().getStart();
        Instant endsAt = appointment.getInterval().getEnd();

        ZonedDateTime start = startsAt.atZone(timezone);
        ZonedDateTime end = endsAt.atZone(timezone);

        return new AppointmentResponseDto(
                appointment.getId(),

                business.getId(),

                service.getId(),
                service.getName(),
                Math.toIntExact(Duration.between(startsAt, endsAt).toMinutes()),

                client.getId(),
                client.getName(),
                client.getPhoneNumber(),
                client.getEmail(),

                employee.getId(),
                employee.getUser().getName(),

                start.toLocalDate(),
                start.toLocalTime(),
                end.toLocalTime(),

                appointment.getStatus(),

                appointment.getPriceAtAppointment(),
                "RON", //TODO what to do about currency?

                appointment.getPaymentMethod(),
                appointment.isPaid(),

                appointment.getClientNote()
        );
    }

    public static Appointment toEntity(
            CreateAppointmentGhostClientRequestDto request,
            Employee employee,
            ServiceItem service,
            ZoneId timezone
    ) {
        Appointment appointment = new Appointment();

        appointment.setEmployee(employee);
        appointment.setServiceItem(service);

        ZonedDateTime start = request.date()
                .atTime(request.startTime())
                .atZone(timezone);

        ZonedDateTime end = start.plusMinutes(service.getDurationMinutes());

        appointment.setInterval(new DateTimeInterval(
                start.toInstant(),
                end.toInstant()
        ));

        appointment.setPaymentMethod(request.paymentMethod());
        appointment.setClientNote(request.clientNote());

        appointment.setPriceAtAppointment(service.getPrice());
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setPaid(false);

        return appointment;
    }

}