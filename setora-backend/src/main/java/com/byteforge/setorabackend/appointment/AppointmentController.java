package com.byteforge.setorabackend.appointment;

import com.byteforge.setorabackend.appointment.dtos.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    final private AppointmentService appointmentService;

    //this should be called from the angular service, which is called from appointments.component.ts
    @GetMapping("/summary")
    public ResponseEntity<AppointmentsTodaySummaryDto> getDailyAppointmentSummary(
            @RequestParam int businessId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return ResponseEntity.ok(
                appointmentService.getDailySummary(businessId, date)
        );
    }

    @GetMapping("/calendar")
    public ResponseEntity<List<CalendarAppointmentDto>> getCalendarAppointments(
            @RequestParam int businessId,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(required = false)
            int employeeId,

            @RequestParam(required = false)
            String status,

            @RequestParam(required = false)
            Integer from,

            @RequestParam(required = false)
            Integer to
    ) {
        return ResponseEntity.ok(
                appointmentService.getCalendarAppointments(
                        businessId,
                        startDate,
                        endDate,
                        employeeId,
                        status,
                        from,
                        to
                )
        );
    }

    @PostMapping("/ghost")
    public ResponseEntity<AppointmentResponseDto> createGuestAppointment(
            @Valid @RequestBody CreateAppointmentGhostClientRequestDto request
    ) {
        var response = appointmentService.createGuestAppointment(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDto> getAppointmentById(
            @PathVariable int id
    ) {
        return ResponseEntity.ok(
                appointmentService.getAppointmentById(id)
        );
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponseDto> cancelAppointment(
            @PathVariable int id
    ) {
        return ResponseEntity.ok(
                appointmentService.cancelAppointment(id)
        );
    }


    @PatchMapping("/{id}")
    public ResponseEntity<AppointmentResponseDto> updateAppointment(
            @PathVariable int id,
            @Valid @RequestBody UpdateAppointmentRequestDto request
    ) {
        return ResponseEntity.ok(
                appointmentService.updateAppointment(id, request)
        );
    }


}
