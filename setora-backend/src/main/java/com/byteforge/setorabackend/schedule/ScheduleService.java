package com.byteforge.setorabackend.schedule;

import com.byteforge.setorabackend.appointment.Appointment;
import com.byteforge.setorabackend.appointment.AppointmentRepository;
import com.byteforge.setorabackend.schedule.dtos.EmployeeScheduleDto;
import com.byteforge.setorabackend.schedule.mapper.ScheduleMapper;
import com.byteforge.setorabackend.business.BusinessRepository;
import com.byteforge.setorabackend.employees.Employee;
import com.byteforge.setorabackend.employees.WorkSchedule;
import com.byteforge.setorabackend.employees.WorkScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final AppointmentRepository appointmentRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final BusinessRepository businessRepository;

    private final ScheduleMapper scheduleMapper;

    @Transactional(readOnly = true)
    public List<EmployeeScheduleDto> getBusinessSchedules(UUID businessId, Instant start, Instant end) {
        if(!businessRepository.existsById(businessId)) {
            throw new EntityNotFoundException("Business not found");
        }

        List<WorkSchedule> allSchedules =
                workScheduleRepository.findByBusinessId(businessId);
        List<Appointment> appointments =
                appointmentRepository.findByBusinessIdAndDateRange(businessId, start, end);

        Map<Employee, List<WorkSchedule>> schedulesByEmployee = allSchedules.stream()
                .collect(Collectors.groupingBy(WorkSchedule::getEmployee));

        Map<Employee, List<Appointment>> appointmentsByEmployee = appointments.stream()
                .collect(Collectors.groupingBy(Appointment::getEmployee));

        return schedulesByEmployee.entrySet().stream()
                .map(entry -> scheduleMapper.toEmployeeScheduleDto(
                        entry.getKey(),
                        entry.getValue(),
                        appointmentsByEmployee.getOrDefault(entry.getKey(), List.of())
                ))
                .toList();
    }
}
