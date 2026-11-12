
package com.byteforge.setorabackend.appointment;

import com.byteforge.setorabackend.appointment.dtos.AppointmentsTodaySummaryDto;
import com.byteforge.setorabackend.appointment.dtos.CalendarAppointmentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

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
}
