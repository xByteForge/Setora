package com.byteforge.setorabackend.employees;

import com.byteforge.setorabackend.appointment.Appointment;
import com.byteforge.setorabackend.appointment.AppointmentRepository;
import com.byteforge.setorabackend.appointment.AppointmentStatus;
import com.byteforge.setorabackend.business.Business;
import com.byteforge.setorabackend.common.DateTimeInterval;
import com.byteforge.setorabackend.common.TimeInterval;
import com.byteforge.setorabackend.employees.model.AvailabilityDtos.*;
import com.byteforge.setorabackend.employees.model.Employee;
import com.byteforge.setorabackend.employees.model.UnavailableSlot;
import com.byteforge.setorabackend.employees.model.UnavailableType;
import com.byteforge.setorabackend.employees.model.WorkSchedule;
import com.byteforge.setorabackend.employees.repository.EmployeeRepository;
import com.byteforge.setorabackend.employees.repository.UnavailableSlotRepository;
import com.byteforge.setorabackend.employees.repository.WorkScheduleRepository;
import com.byteforge.setorabackend.employees.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private WorkScheduleRepository workScheduleRepository;
    @Mock
    private UnavailableSlotRepository unavailableSlotRepository;
    @Mock
    private AppointmentRepository appointmentRepository; // <--- Adăugat mock-ul nou

    @InjectMocks
    private EmployeeService service;

    private final UUID employeeId = UUID.randomUUID();
    // business timezone is not set in these tests, so the service uses UTC
    private final LocalDate nextWeek = LocalDate.now(ZoneOffset.UTC).plusDays(7);

    @Test
    void createsOneOffSlot() {
        stubEmployee();
        when(unavailableSlotRepository.findByEmployeeId(employeeId)).thenReturn(List.of());
        when(unavailableSlotRepository.save(any(UnavailableSlot.class))).thenAnswer(call -> call.getArgument(0));

        // Când se caută programări, returnăm o listă goală (nu există suprapuneri)
        when(appointmentRepository.findByEmployeeIdAndStatusNot(eq(employeeId), eq(AppointmentStatus.CANCELLED)))
                .thenReturn(List.of());

        // Actualizat pentru a folosi noul DTO (CreateSlotResponse)
        CreateSlotResponse response = service.createUnavailableSlot(employeeId, oneOffRequest(nextWeek, 12, 13));

        // Actualizat pentru a verifica valorile din interiorul .slot()
        assertEquals(employeeId, response.slot().employeeId());
        assertEquals(nextWeek, response.slot().date());
        assertEquals(LocalTime.of(12, 0), response.slot().start());
        assertEquals(LocalTime.of(13, 0), response.slot().end());
        assertFalse(response.slot().recurring());
        assertTrue(response.cancelledAppointmentIds().isEmpty()); // Lista de anulări e goală
    }

    @Test
    void cancelsOverlappingAppointments() {
        stubEmployee();
        when(unavailableSlotRepository.findByEmployeeId(employeeId)).thenReturn(List.of());
        when(unavailableSlotRepository.save(any(UnavailableSlot.class))).thenAnswer(call -> call.getArgument(0));

        // Creăm un mock pentru o programare care se va suprapune cu slotul (12:00 - 13:00)
        UUID appId = UUID.randomUUID();
        Appointment overlappingApp = mock(Appointment.class);
        when(overlappingApp.getId()).thenReturn(appId);

        // Simulăm un interval orar de la 12:30 la 13:30 (suprapunere de 30 de minute)
        DateTimeInterval mockInterval = mock(DateTimeInterval.class);
        when(mockInterval.getStart()).thenReturn(nextWeek.atTime(12, 30).toInstant(ZoneOffset.UTC));
        when(mockInterval.getEnd()).thenReturn(nextWeek.atTime(13, 30).toInstant(ZoneOffset.UTC));
        when(overlappingApp.getInterval()).thenReturn(mockInterval);

        when(appointmentRepository.findByEmployeeIdAndStatusNot(eq(employeeId), eq(AppointmentStatus.CANCELLED)))
                .thenReturn(List.of(overlappingApp));

        CreateSlotResponse response = service.createUnavailableSlot(employeeId, oneOffRequest(nextWeek, 12, 13));

        // Verificăm dacă programarea a fost mutată pe statusul CANCELLED
        verify(overlappingApp).setStatus(AppointmentStatus.CANCELLED);
        // Verificăm dacă s-a dat saveAll pentru ea
        verify(appointmentRepository).saveAll(List.of(overlappingApp));
        // Ne asigurăm că frontend-ul primește ID-ul anulat
        assertEquals(1, response.cancelledAppointmentIds().size());
        assertTrue(response.cancelledAppointmentIds().contains(appId));
    }

    @Test
    void rejectsEndBeforeStart() {
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.createUnavailableSlot(employeeId, oneOffRequest(nextWeek, 13, 12)));
        assertEquals(400, e.getStatusCode().value());
    }

    @Test
    void rejectsDateAndDayOfWeekTogether() {
        UnavailableSlotRequest request = new UnavailableSlotRequest(UnavailableType.BREAK, null,
                nextWeek, DayOfWeek.MONDAY, LocalTime.of(12, 0), LocalTime.of(13, 0));
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.createUnavailableSlot(employeeId, request));
        assertEquals(400, e.getStatusCode().value());
    }

    @Test
    void rejectsPastDate() {
        stubEmployee();
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.createUnavailableSlot(employeeId, oneOffRequest(LocalDate.now(ZoneOffset.UTC).minusDays(1), 12, 13)));
        assertEquals(400, e.getStatusCode().value());
    }

    @Test
    void rejectsOverlappingOneOffSlot() {
        stubEmployee();
        when(unavailableSlotRepository.findByEmployeeId(employeeId))
                .thenReturn(List.of(oneOffSlot(nextWeek, 12, 13)));

        UnavailableSlotRequest overlapping = new UnavailableSlotRequest(UnavailableType.BLOCKED, null,
                nextWeek, null, LocalTime.of(12, 30), LocalTime.of(13, 30));
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.createUnavailableSlot(employeeId, overlapping));
        assertEquals(409, e.getStatusCode().value());
    }

    @Test
    void allowsTouchingIntervals() {
        stubEmployee();
        when(unavailableSlotRepository.findByEmployeeId(employeeId))
                .thenReturn(List.of(oneOffSlot(nextWeek, 12, 13)));
        when(unavailableSlotRepository.save(any(UnavailableSlot.class))).thenAnswer(call -> call.getArgument(0));

        when(appointmentRepository.findByEmployeeIdAndStatusNot(eq(employeeId), eq(AppointmentStatus.CANCELLED)))
                .thenReturn(List.of());

        CreateSlotResponse response = service.createUnavailableSlot(employeeId, oneOffRequest(nextWeek, 13, 14));

        assertEquals(LocalTime.of(13, 0), response.slot().start());
    }

    @Test
    void rejectsRecurringSlotOverlappingFutureOneOffSlot() {
        stubEmployee();
        when(unavailableSlotRepository.findByEmployeeId(employeeId))
                .thenReturn(List.of(oneOffSlot(nextWeek, 12, 13)));

        UnavailableSlotRequest recurring = new UnavailableSlotRequest(UnavailableType.BREAK, "Lunch",
                null, nextWeek.getDayOfWeek(), LocalTime.of(12, 30), LocalTime.of(13, 30));
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.createUnavailableSlot(employeeId, recurring));
        assertEquals(409, e.getStatusCode().value());
    }

    @Test
    void expandsRecurringSlotsForEveryDateInRange() {
        stubEmployee();
        UnavailableSlot lunch = new UnavailableSlot();
        lunch.setType(UnavailableType.BREAK);
        lunch.setDayOfWeek(nextWeek.getDayOfWeek());
        lunch.setInterval(new TimeInterval(LocalTime.of(12, 0), LocalTime.of(13, 0)));
        UnavailableSlot blocked = oneOffSlot(nextWeek.plusDays(1), 9, 10);
        LocalDate to = nextWeek.plusDays(7);
        when(unavailableSlotRepository.findForRange(eq(employeeId), eq(nextWeek), eq(to)))
                .thenReturn(List.of(lunch, blocked));

        UnavailableSlots result = service.getUnavailableSlots(employeeId, null, nextWeek, to);

        assertEquals(3, result.slots().size());
        assertEquals(nextWeek, result.slots().get(0).date());
        assertTrue(result.slots().get(0).recurring());
        assertFalse(result.slots().get(1).recurring());
    }

    @Test
    void rejectsRangeWhereFromIsAfterTo() {
        stubEmployee();
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.getUnavailableSlots(employeeId, null, nextWeek.plusDays(1), nextWeek));
        assertEquals(400, e.getStatusCode().value());
    }

    @Test
    void returnsWorkingHoursOrderedByDay() {
        stubEmployee();
        when(workScheduleRepository.findByEmployeeId(employeeId))
                .thenReturn(List.of(workDay(DayOfWeek.WEDNESDAY), workDay(DayOfWeek.MONDAY)));

        WorkingHours result = service.getWorkingHours(employeeId);

        assertEquals(DayOfWeek.MONDAY, result.days().get(0).dayOfWeek());
        assertEquals(DayOfWeek.WEDNESDAY, result.days().get(1).dayOfWeek());
    }

    private void stubEmployee() {
        Employee employee = new Employee();
        employee.setId(employeeId);
        employee.setBusiness(new Business());
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
    }

    private UnavailableSlotRequest oneOffRequest(LocalDate date, int fromHour, int toHour) {
        return new UnavailableSlotRequest(UnavailableType.BREAK, "Lunch", date, null,
                LocalTime.of(fromHour, 0), LocalTime.of(toHour, 0));
    }

    private UnavailableSlot oneOffSlot(LocalDate date, int fromHour, int toHour) {
        UnavailableSlot slot = new UnavailableSlot();
        slot.setType(UnavailableType.BREAK);
        slot.setSpecificDate(date);
        slot.setInterval(new TimeInterval(LocalTime.of(fromHour, 0), LocalTime.of(toHour, 0)));
        return slot;
    }

    private WorkSchedule workDay(DayOfWeek day) {
        WorkSchedule schedule = new WorkSchedule();
        schedule.setDayOfWeek(day);
        schedule.setInterval(new TimeInterval(LocalTime.of(9, 0), LocalTime.of(17, 0)));
        return schedule;
    }
}