package com.byteforge.setorabackend.appointment;

import com.byteforge.setorabackend.appointment.dtos.AppointmentsTodaySummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    final private AppointmentService appointmentService;

    //this should be called fromm the angular service, which is called from appointments.component.ts
    @GetMapping("summary")
    public ResponseEntity<AppointmentsTodaySummaryDto> getDailyAppointmentSummary(
            @RequestParam UUID businessId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ){
        return ResponseEntity.ok(appointmentService.getDailySummary(businessId, date));
    }



}
