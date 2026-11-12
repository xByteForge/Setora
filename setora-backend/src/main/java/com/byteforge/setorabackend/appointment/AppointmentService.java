package com.byteforge.setorabackend.appointment;

import com.byteforge.setorabackend.appointment.dtos.*;
import com.byteforge.setorabackend.business.Business;
import com.byteforge.setorabackend.business.BusinessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    final private AppointmentRepository appointmentRepository;
    final private BusinessService businessService;

    public AppointmentsTodaySummaryDto getDailySummary(
            UUID businessId,
            LocalDate date
    ) {
        Business business = businessService.findById(businessId);

        ZoneId zone = business.getTimezone();

        Instant start = date.atStartOfDay(zone).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(zone).toInstant();

        AppointmentsTodaySummaryProjection summary =
                appointmentRepository.getDailySummary(
                        businessId,
                        start,
                        end,
                        AppointmentStatus.CONFIRMED,
                        AppointmentStatus.PENDING,
                        AppointmentStatus.CANCELLED
                );

        return new AppointmentsTodaySummaryDto(
                Math.toIntExact(summary.getTotal()),
                Math.toIntExact(summary.getConfirmed()),
                Math.toIntExact(summary.getPending()),
                Math.toIntExact(summary.getCancelled())
        );
    }

    @Transactional(readOnly = true)
    public AppointmentResponseDto getAppointmentById(UUID id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Appointment not found"
                ));

        return AppointmentMapper.toResponseDto(appointment);
    }


    @Transactional
    public AppointmentResponseDto cancelAppointment(UUID id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Appointment not found"
                ));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Appointment is already cancelled"
            );
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        Appointment savedAppointment = appointmentRepository.save(appointment);
        return AppointmentMapper.toResponseDto(savedAppointment);
    }


    public Appointment createGuestAppointment(@Valid CreateAppointmentGhostClientRequestDto request) {
        return null;
    }
}
