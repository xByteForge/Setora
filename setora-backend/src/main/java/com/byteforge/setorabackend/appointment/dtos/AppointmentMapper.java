package com.byteforge.setorabackend.appointment.dtos;
import com.byteforge.setorabackend.appointment.Appointment;
import com.byteforge.setorabackend.appointment.AppointmentStatus;
import com.byteforge.setorabackend.auth.User;
import com.byteforge.setorabackend.common.DateTimeInterval;
import com.byteforge.setorabackend.employees.model.Employee;
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

        int clientId = client != null ? client.getId() : 0;
        String clientName = client != null ? client.getName() : appointment.getClientName();
        String clientPhone = client != null ? client.getPhoneNumber() : appointment.getClientPhone();
        String clientEmail = client != null ? client.getEmail() : appointment.getClientEmail();

        return new AppointmentResponseDto(
                appointment.getId(),

                business.getId(),

                service.getId(),
                service.getName(),
                Math.toIntExact(Duration.between(startsAt, endsAt).toMinutes()),

                clientId,
                clientName,
                clientPhone,
                clientEmail,

                employee.getId(),
                employee.getUser().getName(),

                start.toLocalDate(),
                start.toLocalTime(),
                end.toLocalTime(),

                appointment.getStatus(),

                appointment.getPriceAtAppointment(),

                appointment.getPaymentMethod(),

                appointment.getClientNote()
        );
    }

    public static Appointment toEntity(
            CreateAppointmentGhostClientRequestDto request,
            Employee employee,
            ServiceItem service,
            User client,
            ZoneId timezone
    ) {
        Appointment appointment = new Appointment();
        appointment.setClient(client);
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
        appointment.setStatus(AppointmentStatus.UNAVAILABLE);

        return appointment;
    }

}