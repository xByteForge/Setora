
package com.byteforge.setorabackend.appointment;

import com.byteforge.setorabackend.appointment.dtos.AppointmentMapper;
import com.byteforge.setorabackend.appointment.dtos.AppointmentResponseDto;
import com.byteforge.setorabackend.appointment.dtos.AppointmentsTodaySummaryDto;
import com.byteforge.setorabackend.appointment.dtos.CalendarAppointmentDto;
import com.byteforge.setorabackend.appointment.dtos.CreateAppointmentGhostClientRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    final private AppointmentService appointmentService;

    //this should be called from the angular service, which is called from appointments.component.ts
    @GetMapping("/summary")
    public ResponseEntity<AppointmentsTodaySummaryDto> getDailyAppointmentSummary(
            @RequestParam UUID businessId,
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
            @RequestParam UUID businessId,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(required = false)
            UUID employeeId,

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
        var appointment = appointmentService.createGuestAppointment(request);
        var response = AppointmentMapper.toResponseDto(appointment);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDto> getAppointmentById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                appointmentService.getAppointmentById(id)
        );
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponseDto> cancelAppointment(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                appointmentService.cancelAppointment(id)
        );
    }

}
