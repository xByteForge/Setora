package com.byteforge.setorabackend.employees;

import com.byteforge.setorabackend.employees.AvailabilityDtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class AvailabilityController {
    private final AvailabilityService availabilityService;

    @GetMapping("/employees/{employeeId}/working-hours")
    public WorkingHours getWorkingHours(@PathVariable("employeeId") UUID employeeId) {
        return availabilityService.getWorkingHours(employeeId);
    }

    //use ?date=2026-11-20 or ?from=2026-11-16&to=2026-11-22
    @GetMapping("/employees/{employeeId}/unavailable-slots")
    public UnavailableSlots getUnavailableSlots(
            @PathVariable("employeeId") UUID employeeId,
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return availabilityService.getUnavailableSlots(employeeId, date, from, to);
    }

    @PostMapping("/blocks")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateSlotResponse createUnavailableSlot(@RequestBody UnavailableSlotRequest request) {
        return availabilityService.createUnavailableSlot(request);
    }

    @DeleteMapping("/blocks/{slotId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUnavailableSlot(@PathVariable("slotId") UUID slotId) {
        availabilityService.deleteUnavailableSlot(slotId);
    }
}