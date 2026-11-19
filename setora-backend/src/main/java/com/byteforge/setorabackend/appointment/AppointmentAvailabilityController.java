package com.byteforge.setorabackend.appointment;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/businesses")
public class AppointmentAvailabilityController {
    private final AppointmentAvailabilityService appointmentAvailabilityService;

    @GetMapping("/{businessId}/availability")
    public ResponseEntity<@NonNull List<LocalTime>> getAllAvailableServiceItemsForToday(
            @PathVariable int businessId,
            @RequestParam LocalDate date,
            @RequestParam int serviceId,
            @RequestParam(required = false) Integer employeeId)
    {
        List<LocalTime> retList = appointmentAvailabilityService.getAllAvailableAppointmentTimesForToday(businessId, date, serviceId, employeeId);

        return ResponseEntity.ok().body(retList);
    }
}
