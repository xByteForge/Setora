package com.byteforge.setorabackend.schedule;

import com.byteforge.setorabackend.appointment.Appointment;
import com.byteforge.setorabackend.appointment.AppointmentRepository;
import com.byteforge.setorabackend.business.BusinessRepository;
import com.byteforge.setorabackend.schedule.dtos.EmployeeScheduleDto;
import com.byteforge.setorabackend.employees.Employee;
import com.byteforge.setorabackend.employees.WorkSchedule;
import com.byteforge.setorabackend.schedule.mapper.ScheduleMapper;
import com.byteforge.setorabackend.employees.WorkScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private WorkScheduleRepository workScheduleRepository;

    @Mock
    private ScheduleMapper mapper;

    @InjectMocks
    private ScheduleService scheduleService;

    @Test
    void getBusinessSchedules_ReturnsEmployeeEvenWithZeroAppointments() {
        UUID businessId = UUID.randomUUID();
        Instant start = Instant.parse("2026-11-12T00:00:00Z");
        Instant end = Instant.parse("2026-11-13T00:00:00Z");

        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());

        WorkSchedule schedule = new WorkSchedule();
        schedule.setEmployee(employee);

        when(businessRepository.existsById(businessId)).thenReturn(true);
        when(workScheduleRepository.findByBusinessId(businessId))
                .thenReturn(List.of(schedule));
        when(appointmentRepository.findByBusinessIdAndDateRange(businessId, start, end))
                .thenReturn(List.of());

        EmployeeScheduleDto mockDto = new EmployeeScheduleDto(
                employee.getId(), "John Doe", "JD", "Specialist",
                "Location", List.of(), List.of()
        );
        when(mapper.toEmployeeScheduleDto(eq(employee), anyList(), anyList()))
                .thenReturn(mockDto);

        List<EmployeeScheduleDto> result = scheduleService.getBusinessSchedules(businessId, start, end);

        assertNotNull(result);
        assertEquals(1, result.size(), "Should return exactly one employee schedule group");
        assertEquals(employee.getId(), result.getFirst().employeeId());

        verify(mapper).toEmployeeScheduleDto(eq(employee), eq(List.of(schedule)), eq(List.of()));
    }

    @Test
    void getBusinessSchedules_GroupsMultipleEmployeesAndAppointmentsCorrectly() {
        UUID businessId = UUID.randomUUID();
        Instant start = Instant.parse("2026-11-12T00:00:00Z");
        Instant end = Instant.parse("2026-11-13T00:00:00Z");

        Employee emp1 = new Employee();
        emp1.setId(UUID.randomUUID());

        WorkSchedule ws1Emp1 = new WorkSchedule();
        ws1Emp1.setId(UUID.randomUUID());
        ws1Emp1.setEmployee(emp1);

        WorkSchedule ws2Emp1 = new WorkSchedule();
        ws2Emp1.setId(UUID.randomUUID());
        ws2Emp1.setEmployee(emp1);

        Appointment appt1Emp1 = new Appointment();
        appt1Emp1.setId(UUID.randomUUID());
        appt1Emp1.setEmployee(emp1);

        Appointment appt2Emp1 = new Appointment();
        appt2Emp1.setId(UUID.randomUUID());
        appt2Emp1.setEmployee(emp1);

        Employee emp2 = new Employee();
        emp2.setId(UUID.randomUUID());

        WorkSchedule ws1Emp2 = new WorkSchedule();
        ws1Emp2.setId(UUID.randomUUID());
        ws1Emp2.setEmployee(emp2);

        when(businessRepository.existsById(businessId)).thenReturn(true);
        when(workScheduleRepository.findByBusinessId(businessId))
                .thenReturn(List.of(ws1Emp1, ws2Emp1, ws1Emp2));
        when(appointmentRepository.findByBusinessIdAndDateRange(businessId, start, end))
                .thenReturn(List.of(appt1Emp1, appt2Emp1));

        EmployeeScheduleDto dto1 = new EmployeeScheduleDto(
                emp1.getId(), "Emp 1", "E1", "Role", "Location", List.of(), List.of()
        );
        EmployeeScheduleDto dto2 = new EmployeeScheduleDto(
                emp2.getId(), "Emp 2", "E2", "Role", "Location", List.of(), List.of()
        );

        when(mapper.toEmployeeScheduleDto(eq(emp1), anyList(), anyList())).thenReturn(dto1);
        when(mapper.toEmployeeScheduleDto(eq(emp2), anyList(), anyList())).thenReturn(dto2);

        List<EmployeeScheduleDto> result = scheduleService.getBusinessSchedules(businessId, start, end);

        assertNotNull(result);
        assertEquals(2, result.size(), "Should return schedules for exactly 2 distinct employees");
        assertTrue(result.contains(dto1));
        assertTrue(result.contains(dto2));

        verify(mapper).toEmployeeScheduleDto(
                eq(emp1),
                eq(List.of(ws1Emp1, ws2Emp1)),
                eq(List.of(appt1Emp1, appt2Emp1))
        );

        verify(mapper).toEmployeeScheduleDto(
                eq(emp2),
                eq(List.of(ws1Emp2)),
                eq(List.of())
        );
    }

    @Test
    void getBusinessSchedules_ThrowsEntityNotFoundException_WhenBusinessDoesNotExist() {
        UUID businessId = UUID.randomUUID();
        Instant start = Instant.parse("2026-11-12T00:00:00Z");
        Instant end = Instant.parse("2026-11-13T00:00:00Z");

        when(businessRepository.existsById(businessId)).thenReturn(false);

        assertThrows(
                EntityNotFoundException.class,
                () -> scheduleService.getBusinessSchedules(businessId, start, end)
        );

        verifyNoInteractions(workScheduleRepository, appointmentRepository, mapper);
    }
}