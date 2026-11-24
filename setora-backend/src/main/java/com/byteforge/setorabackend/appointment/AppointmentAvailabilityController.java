package com.byteforge.setorabackend.appointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/businesses")
public class AppointmentAvailabilityController {
    private final AppointmentAvailabilityService appointmentAvailabilityService;

    /**
     * Retrieves all available appointment start times for a service on a given date.
     * Validates that the service belongs to the specified business and is active.
     * Optionally filters availability by a specific employee.
     * Considers employee working schedules, unavailable slots, and existing
     * non-cancelled appointments.
     * Obs! Returned results format is ["09:00:00", "09:30:00", "10:00:00"], eliminate seconds on frontend.
     *
     * @param businessId ID of the business
     * @param date date for which availability is requested
     * @param serviceId ID of the requested service
     * @param employeeId optional ID of a specific employee
     * @return sorted list of available appointment start times
     */
    @GetMapping("/{businessId}/availability")
    public ResponseEntity<@NonNull List<LocalTime>> getAllAvailableAppointmentTimesForToday(
            @PathVariable int businessId,
            @RequestParam LocalDate date,
            @RequestParam int serviceId,
            @RequestParam(required = false) Integer employeeId) {
        List<LocalTime> retList = appointmentAvailabilityService.getAllAvailableAppointmentTimesForToday(
                businessId, date, serviceId, employeeId);

        return ResponseEntity.ok().body(retList);
    }
}
