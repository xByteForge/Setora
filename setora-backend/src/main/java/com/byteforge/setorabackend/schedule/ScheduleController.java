package com.byteforge.setorabackend.schedule;

import com.byteforge.setorabackend.schedule.dtos.BusinessScheduleRequest;
import com.byteforge.setorabackend.schedule.dtos.EmployeeScheduleDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping("businesses/{businessId}/schedules")
    public ResponseEntity<List<EmployeeScheduleDto>> getEmployeesSchedules(
            @PathVariable UUID businessId,
            @Valid BusinessScheduleRequest request
    ) {
        List<EmployeeScheduleDto> response =
                scheduleService.getBusinessSchedules(businessId, request.startDate(), request.endDate());
        return ResponseEntity.ok(response);
    }
}
