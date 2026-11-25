package com.byteforge.setorabackend.appointment.dtos;

import com.byteforge.setorabackend.appointment.Appointment;
import com.byteforge.setorabackend.employees.model.Employee;
import com.byteforge.setorabackend.serviceItem.ServiceItem;
import com.byteforge.setorabackend.user.User;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;

public record CalendarAppointmentDto(
        int id,
        int clientId,
        String clientName,
        int businessId,
        int employeeId,
        String employeeName,
        int serviceId,
        String serviceName,
        Instant startsAt,
        Instant endsAt,
        String status,
        BigDecimal priceAtAppointment,
        String clientNote) {
    public static CalendarAppointmentDto from(Appointment appointment) {
        User client = appointment.getClient();
        Employee employee = appointment.getEmployee();
        User employeeUser = employee.getUser();
        ServiceItem service = appointment.getServiceItem();

        return new CalendarAppointmentDto(
                appointment.getId(),
                client.getId(),
                client.getFirstName() + " " + client.getLastName(),
                service.getBusiness().getId(),
                employee.getId(),
                employeeUser.getFirstName() + " " + employeeUser.getLastName(),
                service.getId(),
                service.getName(),
                appointment.getInterval().getStart(),
                appointment.getInterval().getEnd(),
                appointment.getStatus().name().toLowerCase(Locale.ROOT),
                appointment.getPriceAtAppointment(),
                appointment.getClientNote());
    }
}
