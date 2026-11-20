package com.byteforge.setorabackend.appointment;

import com.byteforge.setorabackend.appointment.dtos.*;
import com.byteforge.setorabackend.auth.User;
import com.byteforge.setorabackend.auth.UserService;
import com.byteforge.setorabackend.business.Business;
import com.byteforge.setorabackend.business.BusinessService;
import com.byteforge.setorabackend.employees.model.Employee;
import com.byteforge.setorabackend.employees.service.EmployeeService;
import com.byteforge.setorabackend.serviceItem.ServiceItem;
import com.byteforge.setorabackend.serviceItem.ServiceItemService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
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
    public AppointmentResponseDto createGuestAppointment(@Valid CreateAppointmentGhostClientRequestDto request) {
        ServiceItem service = serviceItemService.findById(request.serviceId());
        Employee employee = employeeService.findById(request.employeeId());

        Business business = service.getBusiness();

        User client = userService.findByEmail("ghost@gmail.com");

        // Validate business, employee and service compatibility.
        // TODO: Check time slots to prevent double bookings.
        Appointment appointment =
                AppointmentMapper.toEntity(request, employee, service, client, business.getTimezone());

        String note = "";
        note += "Appointment for: " + request.clientName() + "\n";
        note += "Email: " + request.clientEmail() + "\n";
        note += "Phone number: " + request.clientPhone() + "\n";
        note += "\n --- Details ---\n";
        note += request.clientName() + "\n";

        appointment.setClientNote(note);

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
}
