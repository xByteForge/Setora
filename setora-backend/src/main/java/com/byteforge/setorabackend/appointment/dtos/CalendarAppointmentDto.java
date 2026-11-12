
package com.byteforge.setorabackend.appointment.dtos;

import com.byteforge.setorabackend.appointment.Appointment;
import com.byteforge.setorabackend.auth.User;
import com.byteforge.setorabackend.employees.Employee;
import com.byteforge.setorabackend.serviceItem.ServiceItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

public record CalendarAppointmentDto(
        UUID id,
        UUID clientId,
        String clientName,
        UUID businessId,
        UUID employeeId,
        String employeeName,
        UUID serviceId,
        String serviceName,
        Instant startsAt,
        Instant endsAt,
        String status,
        BigDecimal priceAtAppointment,
        String clientNote
) {
    public static CalendarAppointmentDto from(Appointment appointment) {
        User client = appointment.getClient();
        Employee employee = appointment.getEmployee();
        User employeeUser = employee.getUser();
        ServiceItem service = appointment.getServiceItem();

        return new CalendarAppointmentDto(
                appointment.getId(),
                client.getId(),
                client.getFirstname() + " " + client.getSurname(),
                service.getBusiness().getId(),
                employee.getId(),
                employeeUser.getFirstname() + " " + employeeUser.getSurname(),
                service.getId(),
                service.getName(),
                appointment.getInterval().getStart(),
                appointment.getInterval().getEnd(),
                appointment.getStatus().name().toLowerCase(Locale.ROOT),
                appointment.getPriceAtAppointment(),
                appointment.getClientNote()
        );
    }
}
