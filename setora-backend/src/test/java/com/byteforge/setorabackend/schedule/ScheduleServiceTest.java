package com.byteforge.setorabackend.schedule;

import com.byteforge.setorabackend.appointment.Appointment;
import com.byteforge.setorabackend.appointment.AppointmentService;
import com.byteforge.setorabackend.business.Business;
import com.byteforge.setorabackend.business.BusinessService;
import com.byteforge.setorabackend.employees.model.UnavailableSlot;
import com.byteforge.setorabackend.employees.repository.UnavailableSlotRepository;
import com.byteforge.setorabackend.schedule.dtos.EmployeeScheduleDto;
import com.byteforge.setorabackend.employees.model.Employee;
import com.byteforge.setorabackend.employees.model.WorkSchedule;
import com.byteforge.setorabackend.schedule.mapper.ScheduleMapper;
import com.byteforge.setorabackend.employees.repository.WorkScheduleRepository;
import com.byteforge.setorabackend.schedule.service.ScheduleService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private WorkScheduleRepository workScheduleRepository;

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private UnavailableSlotRepository unavailableSlotRepository;

    @Mock
    private BusinessService businessService;

    @Mock
    private ScheduleMapper mapper;

    @InjectMocks
    private ScheduleService scheduleService;

    @Test
    void getBusinessSchedules_ReturnsEmployeeEvenWithZeroAppointmentsOrSlots() {
        UUID businessId = UUID.randomUUID();
        Instant start = Instant.parse("2026-11-12T00:00:00Z");
        Instant end = Instant.parse("2026-11-13T00:00:00Z");
        ZoneId zoneId = ZoneId.of("Europe/Bucharest");
        LocalDate startDate = LocalDate.ofInstant(start, zoneId);
        LocalDate endDate = LocalDate.ofInstant(end, zoneId);

        Business business = new Business();
        business.setId(businessId);
        business.setTimezone(zoneId);

        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());

        WorkSchedule schedule = new WorkSchedule();
        schedule.setEmployee(employee);

        when(businessService.findById(businessId)).thenReturn(business);
        when(workScheduleRepository.findByBusinessId(businessId))
                .thenReturn(List.of(schedule));
        when(appointmentService.findByBusinessIdAndDateRange(businessId, start, end))
                .thenReturn(List.of());
        when(unavailableSlotRepository.findByBusinessIdAndDateRangeOrDayNotNull(businessId, startDate, endDate))
                .thenReturn(List.of());

        EmployeeScheduleDto mockDto = new EmployeeScheduleDto(
                employee.getId(), "John Doe", "JD", "Specialist",
                "Location", List.of(), List.of(), List.of()
        );

        when(mapper.toEmployeeScheduleDto(
                eq(employee), anyList(), anyList(), anyList(), eq(startDate), eq(endDate), eq(zoneId)
        )).thenReturn(mockDto);

        List<EmployeeScheduleDto> result = scheduleService.getBusinessSchedules(businessId, start, end);

        assertNotNull(result);
        assertEquals(1, result.size(), "Should return exactly one employee schedule group");
        assertEquals(employee.getId(), result.getFirst().employeeId());

        verify(mapper).toEmployeeScheduleDto(
                eq(employee), eq(List.of(schedule)), eq(List.of()), eq(List.of()), eq(startDate), eq(endDate), eq(zoneId)
        );
    }

    @Test
    void getBusinessSchedules_GroupsMultipleEmployeesAndAppointmentsCorrectly() {
        UUID businessId = UUID.randomUUID();
        Instant start = Instant.parse("2026-11-12T00:00:00Z");
        Instant end = Instant.parse("2026-11-13T00:00:00Z");
        ZoneId zoneId = ZoneId.of("Europe/Bucharest");
        LocalDate startDate = LocalDate.ofInstant(start, zoneId);
        LocalDate endDate = LocalDate.ofInstant(end, zoneId);

        Business business = new Business();
        business.setId(businessId);
        business.setTimezone(zoneId);

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

        UnavailableSlot slotEmp1 = new UnavailableSlot();
        slotEmp1.setId(UUID.randomUUID());
        slotEmp1.setEmployee(emp1);

        Employee emp2 = new Employee();
        emp2.setId(UUID.randomUUID());

        WorkSchedule ws1Emp2 = new WorkSchedule();
        ws1Emp2.setId(UUID.randomUUID());
        ws1Emp2.setEmployee(emp2);

        when(businessService.findById(businessId)).thenReturn(business);
        when(workScheduleRepository.findByBusinessId(businessId))
                .thenReturn(List.of(ws1Emp1, ws2Emp1, ws1Emp2));
        when(appointmentService.findByBusinessIdAndDateRange(businessId, start, end))
                .thenReturn(List.of(appt1Emp1, appt2Emp1));
        when(unavailableSlotRepository.findByBusinessIdAndDateRangeOrDayNotNull(businessId, startDate, endDate))
                .thenReturn(List.of(slotEmp1));

        EmployeeScheduleDto dto1 = new EmployeeScheduleDto(
                emp1.getId(), "Emp 1", "E1", "Role", "Location", List.of(), List.of(), List.of()
        );
        EmployeeScheduleDto dto2 = new EmployeeScheduleDto(
                emp2.getId(), "Emp 2", "E2", "Role", "Location", List.of(), List.of(), List.of()
        );

        when(mapper.toEmployeeScheduleDto(
                eq(emp1), anyList(), anyList(), anyList(), eq(startDate), eq(endDate), eq(zoneId)
        )).thenReturn(dto1);

        when(mapper.toEmployeeScheduleDto(
                eq(emp2), anyList(), anyList(), anyList(), eq(startDate), eq(endDate), eq(zoneId)
        )).thenReturn(dto2);

        List<EmployeeScheduleDto> result = scheduleService.getBusinessSchedules(businessId, start, end);

        assertNotNull(result);
        assertEquals(2, result.size(), "Should return schedules for exactly 2 distinct employees");
        assertTrue(result.contains(dto1));
        assertTrue(result.contains(dto2));

        verify(mapper).toEmployeeScheduleDto(
                eq(emp1),
                eq(List.of(ws1Emp1, ws2Emp1)),
                eq(List.of(appt1Emp1, appt2Emp1)),
                eq(List.of(slotEmp1)),
                eq(startDate),
                eq(endDate),
                eq(zoneId)
        );

        verify(mapper).toEmployeeScheduleDto(
                eq(emp2),
                eq(List.of(ws1Emp2)),
                eq(List.of()),
                eq(List.of()),
                eq(startDate),
                eq(endDate),
                eq(zoneId)
        );
    }

    @Test
    void getBusinessSchedules_ThrowsEntityNotFoundException_WhenBusinessDoesNotExist() {
        UUID businessId = UUID.randomUUID();
        Instant start = Instant.parse("2026-11-12T00:00:00Z");
        Instant end = Instant.parse("2026-11-13T00:00:00Z");

        when(businessService.findById(businessId))
                .thenThrow(new EntityNotFoundException("Business not found"));

        assertThrows(
                EntityNotFoundException.class,
                () -> scheduleService.getBusinessSchedules(businessId, start, end)
        );

        verifyNoInteractions(workScheduleRepository, appointmentService, unavailableSlotRepository, mapper);
    }
}