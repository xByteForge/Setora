package com.byteforge.setorabackend.schedule.service;

import com.byteforge.setorabackend.appointment.Appointment;
import com.byteforge.setorabackend.appointment.AppointmentService;
import com.byteforge.setorabackend.business.Business;
import com.byteforge.setorabackend.business.BusinessService;
import com.byteforge.setorabackend.employees.model.UnavailableSlot;
import com.byteforge.setorabackend.employees.repository.UnavailableSlotRepository;
import com.byteforge.setorabackend.schedule.dtos.EmployeeScheduleDto;
import com.byteforge.setorabackend.schedule.mapper.ScheduleMapper;
import com.byteforge.setorabackend.employees.model.Employee;
import com.byteforge.setorabackend.employees.model.WorkSchedule;
import com.byteforge.setorabackend.employees.repository.WorkScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final WorkScheduleRepository workScheduleRepository;
    private final UnavailableSlotRepository unavailableSlotRepository;
    private final AppointmentService appointmentService;
    private final BusinessService businessService;

    private final ScheduleMapper scheduleMapper;

    @Transactional(readOnly = true)
    public List<EmployeeScheduleDto> getBusinessSchedules(UUID businessId, Instant start, Instant end) {
        Business business = businessService.findById(businessId);

        ZoneId zoneId = business.getTimezone();
        LocalDate startDate = LocalDate.ofInstant(start, zoneId);
        LocalDate endDate = LocalDate.ofInstant(end, zoneId);

        List<WorkSchedule> workSchedules = workScheduleRepository.findByBusinessId(businessId);
        List<Appointment> appointments = appointmentService.findByBusinessIdAndDateRange(businessId, start, end);
        List<UnavailableSlot> unavailableSlots = unavailableSlotRepository.findByBusinessIdAndDateRangeOrDayNotNull(businessId, startDate, endDate);

        Map<UUID, List<WorkSchedule>> schedulesByEmp = workSchedules.stream()
                .collect(Collectors.groupingBy(ws -> ws.getEmployee().getId()));

        Map<UUID, List<Appointment>> appointmentsByEmp = appointments.stream()
                .collect(Collectors.groupingBy(app -> app.getEmployee().getId()));

        Map<UUID, List<UnavailableSlot>> slotsByEmp = unavailableSlots.stream()
                .collect(Collectors.groupingBy(slot -> slot.getEmployee().getId()));

        return schedulesByEmp.entrySet().stream().map(entry -> {
            UUID empId = entry.getKey();
            Employee employee = entry.getValue().getFirst().getEmployee();

            return scheduleMapper.toEmployeeScheduleDto(
                    employee,
                    entry.getValue(),
                    appointmentsByEmp.getOrDefault(empId, List.of()),
                    slotsByEmp.getOrDefault(empId, List.of()),
                    startDate,
                    endDate,
                    zoneId
            );
        }).toList();
    }
}
