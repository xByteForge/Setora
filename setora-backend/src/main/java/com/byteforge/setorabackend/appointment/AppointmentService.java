package com.byteforge.setorabackend.appointment;

import com.byteforge.setorabackend.appointment.dtos.*;
import com.byteforge.setorabackend.business.model.Business;
import com.byteforge.setorabackend.business.service.BusinessService;
import com.byteforge.setorabackend.common.DateTimeInterval;
import com.byteforge.setorabackend.employees.model.Employee;
import com.byteforge.setorabackend.employees.service.EmployeeService;
import com.byteforge.setorabackend.serviceItem.ServiceItem;
import com.byteforge.setorabackend.serviceItem.ServiceItemService;
import com.byteforge.setorabackend.user.User;
import com.byteforge.setorabackend.user.UserService;
import jakarta.validation.Valid;
import java.time.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final BusinessService businessService;
    private final ServiceItemService serviceItemService;
    private final EmployeeService employeeService;
    private final UserService userService;

    public AppointmentsTodaySummaryDto getDailySummary(int businessId, LocalDate date) {
        Business business = businessService.findById(businessId);

        ZoneId zone = business.getTimezone();

        Instant start = date.atStartOfDay(zone).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(zone).toInstant();

        AppointmentsTodaySummaryProjection summary = appointmentRepository.getDailySummary(
                businessId,
                start,
                end,
                AppointmentStatus.CONFIRMED,
                AppointmentStatus.UNAVAILABLE,
                AppointmentStatus.CANCELLED);

        return new AppointmentsTodaySummaryDto(
                Math.toIntExact(summary.getTotal()),
                Math.toIntExact(summary.getConfirmed()),
                Math.toIntExact(summary.getPending()),
                Math.toIntExact(summary.getCancelled()));
    }

    @Transactional(readOnly = true)
    public AppointmentResponseDto getAppointmentById(int id) {
        Appointment appointment = appointmentRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        return AppointmentMapper.toResponseDto(appointment);
    }

    @Transactional
    public AppointmentResponseDto cancelAppointment(int id) {
        Appointment appointment = appointmentRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Appointment is already cancelled");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        return AppointmentMapper.toResponseDto(savedAppointment);
    }

    @Transactional
    public AppointmentResponseDto updateAppointment(int id, UpdateAppointmentRequestDto request) {
        Appointment appointment = appointmentRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED
                || appointment.getStatus() == AppointmentStatus.UNAVAILABLE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This appointment cannot be modified");
        }

        if (request == null
                || (request.surname() == null
                        && request.firstname() == null
                        && request.email() == null
                        && request.phoneNumber() == null
                        && request.clientNote() == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one field must be provided");
        }

        User client = appointment.getClient();

        boolean updatingClient = request.surname() != null
                || request.firstname() != null
                || request.email() != null
                || request.phoneNumber() != null;

        if (updatingClient) {
            userService.updateClientDetails(
                    client, request.surname(), request.firstname(), request.email(), request.phoneNumber());
        }

        if (request.clientNote() != null) {
            appointment.setClientNote(request.clientNote());
        }

        Appointment savedAppointment = appointmentRepository.save(appointment);

        return AppointmentMapper.toResponseDto(savedAppointment);
    }

    @Transactional
    public AppointmentResponseDto createAppointment(@Valid CreateAppointmentRequestDto request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }

        if (request.serviceId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "serviceId is required");
        }

        if (request.employeeId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "employeeId is required");
        }

        if (request.date() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date is required");
        }

        if (request.startTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startTime is required");
        }

        ServiceItem service = serviceItemService.findById(request.serviceId());
        Employee employee = employeeService.findById(request.employeeId());

        Business business = service.getBusiness();

        if (employee.getBusiness().getId() != business.getId()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Employee does not belong to the business offering this service");
        }

        if (request.businessId() != null && request.businessId() != business.getId()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Service does not belong to the specified business");
        }

        User client = null;
        String clientName = null;
        String clientEmail = null;
        String clientPhone = null;

        if (request.clientId() != null) {
            client = userService.findActiveUserById(request.clientId());
        } else {
            if (request.clientName() == null || request.clientName().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "clientName is required when clientId is not provided");
            }

            if ((request.clientEmail() == null || request.clientEmail().isBlank())
                    && (request.clientPhone() == null || request.clientPhone().isBlank())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "At least one of clientEmail or clientPhone must be provided when clientId is not provided");
            }

            clientName = request.clientName().trim();

            clientEmail =
                    request.clientEmail() != null && !request.clientEmail().isBlank()
                            ? request.clientEmail().trim().toLowerCase(Locale.ROOT)
                            : null;

            clientPhone =
                    request.clientPhone() != null && !request.clientPhone().isBlank()
                            ? request.clientPhone().trim()
                            : null;
        }

        ZoneId timezone = business.getTimezone() != null ? business.getTimezone() : ZoneId.of("Europe/Bucharest");

        ZonedDateTime start = request.date().atTime(request.startTime()).atZone(timezone);

        ZonedDateTime end = start.plusMinutes(service.getDurationMinutes());

        Appointment appointment = new Appointment();
        appointment.setClient(client);
        appointment.setClientName(clientName);
        appointment.setClientEmail(clientEmail);
        appointment.setClientPhone(clientPhone);

        appointment.setEmployee(employee);
        appointment.setServiceItem(service);

        appointment.setInterval(new DateTimeInterval(start.toInstant(), end.toInstant()));

        PaymentMethod paymentMethod = request.paymentMethod() != null
                ? request.paymentMethod()
                : (service.getPaymentMethod() != null ? service.getPaymentMethod() : PaymentMethod.PHYSICAL);

        appointment.setPaymentMethod(paymentMethod);
        appointment.setPriceAtAppointment(service.getPrice());

        AppointmentStatus status = request.status() != null ? request.status() : AppointmentStatus.CONFIRMED;

        appointment.setStatus(status);
        appointment.setClientNote(request.clientNote());

        try {
            Appointment saved = appointmentRepository.save(appointment);
            return AppointmentMapper.toResponseDto(saved);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Appointment data violates database constraints: " + e.getMessage(), e);
        }
    }

    @Transactional
    public AppointmentResponseDto createGuestAppointment(@Valid CreateAppointmentGhostClientRequestDto request) {
        ServiceItem service = serviceItemService.findById(request.serviceId());
        Employee employee = employeeService.findById(request.employeeId());

        Business business = service.getBusiness();

        // Validate business, employee and service compatibility.
        // TODO: Check time slots to prevent double bookings.
        Appointment appointment = AppointmentMapper.toEntity(request, employee, service, null, business.getTimezone());

        appointment.setClientName(request.clientName());
        appointment.setClientEmail(request.clientEmail());
        appointment.setClientPhone(request.clientPhone());

        appointment.setClientNote(request.clientNote());

        return AppointmentMapper.toResponseDto(appointmentRepository.save(appointment));
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponseDto> getAppointments(
            Integer businessId,
            Integer clientId,
            Integer serviceId,
            String statusValue,
            LocalDate startDate,
            LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date range");
        }

        if ((startDate == null) != (endDate == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Both startDate and endDate must be provided");
        }

        AppointmentStatus status = null;

        if (statusValue != null) {
            try {
                status = AppointmentStatus.valueOf(statusValue.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid appointment status");
            }
        }

        Instant rangeStart = null;
        Instant rangeEnd = null;

        if (startDate != null) {
            ZoneId zone;

            if (businessId != null) {
                zone = businessService.findById(businessId).getTimezone();
            } else {
                zone = ZoneId.of("UTC");
            }

            rangeStart = startDate.atStartOfDay(zone).toInstant();
            rangeEnd = endDate.plusDays(1).atStartOfDay(zone).toInstant();
        }

        Specification<Appointment> specification = AppointmentSpecifications.filterAppointments(
                businessId, clientId, serviceId, status, rangeStart, rangeEnd);

        return appointmentRepository.findAll(specification, Sort.by(Sort.Direction.ASC, "interval.start")).stream()
                .map(AppointmentMapper::toResponseDto)
                .toList();
    }

    public List<Appointment> findByBusinessIdAndDateRange(Integer businessId, Instant start, Instant end) {
        return appointmentRepository.findByBusinessIdAndDateRange(businessId, start, end);
    }

    public List<Appointment> findByEmployeeIdInInterval(int employeeId, Instant start, Instant end) {
        if (start == null || end == null || !start.isBefore(end)) {
            throw new IllegalArgumentException("Invalid date interval");
        }
        return appointmentRepository.findByEmployee_IdOnDate(employeeId, start, end);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findByEmployeeIdOnDate(int employeeId, LocalDate date) {
        Employee employee = employeeService.findById(employeeId);
        ZoneId zone = employee.getBusiness().getTimezone();

        Instant start = date.atStartOfDay(zone).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(zone).toInstant();

        return appointmentRepository.findByEmployee_IdOnDate(employeeId, start, end);
    }
}
