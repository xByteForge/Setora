package com.byteforge.setorabackend.appointmentAvailability;

import com.byteforge.setorabackend.appointment.Appointment;
import com.byteforge.setorabackend.appointment.AppointmentAvailabilityService;
import com.byteforge.setorabackend.appointment.AppointmentService;
import com.byteforge.setorabackend.appointment.AppointmentStatus;
import com.byteforge.setorabackend.business.Business;
import com.byteforge.setorabackend.employees.model.AvailabilityDtos;
import com.byteforge.setorabackend.employees.model.Employee;
import com.byteforge.setorabackend.employees.model.UnavailableType;
import com.byteforge.setorabackend.employees.model.WorkSchedule;
import com.byteforge.setorabackend.common.TimeInterval;
import com.byteforge.setorabackend.common.DateTimeInterval;
import com.byteforge.setorabackend.employees.service.EmployeeService;

import com.byteforge.setorabackend.serviceItem.ServiceItem;
import com.byteforge.setorabackend.serviceItem.ServiceItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class GetAllAvailableAppointmentTimesForTodayTest {

    @Mock
    private EmployeeService employeeService;

    @Mock
    private AppointmentService appointmentService;
    @Mock
    private ServiceItemService serviceItemService;

    @Spy
    @InjectMocks
    private AppointmentAvailabilityService appointmentAvailabilityService;

    private static final int BUSINESS_ID = 1;
    private static final int SERVICE_ID = 10;
    private static final int EMPLOYEE_ID = 20;

    private static final LocalDate DATE = LocalDate.of(2026, 11, 23);
    private static final ZoneId TIMEZONE = ZoneId.of("Europe/Bucharest");

    private Business business;
    private ServiceItem serviceItem;
    private Employee employee;
    private WorkSchedule workSchedule;

    @BeforeEach
    void setUp() {
        business = mock(Business.class);
        serviceItem = mock(ServiceItem.class);
        employee = mock(Employee.class);
        workSchedule = mock(WorkSchedule.class);

        when(business.getId()).thenReturn(BUSINESS_ID);
        when(business.getTimezone()).thenReturn(TIMEZONE);
        when(business.getTimeSlice()).thenReturn(30);

        when(serviceItem.getBusiness()).thenReturn(business);
        when(serviceItem.isActive()).thenReturn(true);
        when(serviceItem.getDurationMinutes()).thenReturn(60);

        when(employee.getId()).thenReturn(EMPLOYEE_ID);
        when(employee.getBusiness()).thenReturn(business);
        when(employee.isActive()).thenReturn(true);
        when(employee.getServiceItems()).thenReturn(Set.of(serviceItem));

        doReturn(serviceItem).when(serviceItemService).findById(SERVICE_ID);
    }

    private void prepareEmployeeWithSchedule(LocalTime start, LocalTime end) {
        TimeInterval workingInterval = new TimeInterval(start, end);

        when(workSchedule.getInterval()).thenReturn(workingInterval);

        when(employeeService.findById(EMPLOYEE_ID)).thenReturn(employee);

        when(employeeService.findWorkScheduleByEmployeeId(
                EMPLOYEE_ID, DATE.getDayOfWeek()))
                .thenReturn(Optional.of(workSchedule));

        when(employeeService.getUnavailableSlots(EMPLOYEE_ID, DATE, null, null))
                .thenReturn(new AvailabilityDtos.UnavailableSlots(
                        EMPLOYEE_ID,
                        TIMEZONE,
                        DATE,
                        DATE,
                        List.of()
                ));

        when(appointmentService.findByEmployeeIdOnDate(EMPLOYEE_ID, DATE))
                .thenReturn(List.of());
    }
    private void prepareEmployeeWithSchedule(
            Employee employee,
            int employeeId,
            LocalTime start,
            LocalTime end
    ) {
        TimeInterval workingInterval = new TimeInterval(start, end);
        WorkSchedule schedule = mock(WorkSchedule.class);

        when(employee.getId()).thenReturn(employeeId);
        when(schedule.getInterval()).thenReturn(workingInterval);

        when(employeeService.findWorkScheduleByEmployeeId(
                employeeId, DATE.getDayOfWeek()))
                .thenReturn(Optional.of(schedule));

        when(employeeService.getUnavailableSlots(employeeId, DATE, null, null))
                .thenReturn(new AvailabilityDtos.UnavailableSlots(
                        employeeId, TIMEZONE, DATE, DATE, List.of()
                ));

        when(appointmentService.findByEmployeeIdOnDate(employeeId, DATE))
                .thenReturn(List.of());
    }


    /// //////////////////////////////////////////////
    ///  TEST FOR GIVEN EMPLOYEE
    /// //////////////////////////////////////////////
    //region Tests for given employee
    @Test
    void getAllAvailableServiceItemsForToday_noConflicts_returnsAllStartingTimes() {
        prepareEmployeeWithSchedule(
                LocalTime.of(9, 0),
                LocalTime.of(12, 0)
        );

        List<LocalTime> result =
                appointmentAvailabilityService.getAllAvailableAppointmentTimesForToday(
                        BUSINESS_ID, DATE, SERVICE_ID, EMPLOYEE_ID
                );

        assertEquals(
                List.of(
                        LocalTime.of(9, 0),
                        LocalTime.of(9, 30),
                        LocalTime.of(10, 0),
                        LocalTime.of(10, 30),
                        LocalTime.of(11, 0)
                ),
                result
        );
    }

    @Test
    void getAllAvailableServiceItemsForToday_unavailableSlotExists_excludesOverlappingTimes() {
        prepareEmployeeWithSchedule(
                LocalTime.of(9, 0),
                LocalTime.of(12, 0)
        );

        AvailabilityDtos.UnavailableOccurrence unavailable =
                new AvailabilityDtos.UnavailableOccurrence(
                        1,
                        UnavailableType.BREAK,
                        "irrelevant",
                        false,
                        DATE,
                        LocalTime.of(10, 0),
                        LocalTime.of(11, 0)
                );

        when(employeeService.getUnavailableSlots(EMPLOYEE_ID, DATE, null, null))
                .thenReturn(new AvailabilityDtos.UnavailableSlots(
                        EMPLOYEE_ID,
                        TIMEZONE,
                        DATE,
                        DATE,
                        List.of(unavailable)
                ));

        //actual test
        List<LocalTime> result =
                appointmentAvailabilityService.getAllAvailableAppointmentTimesForToday(
                        BUSINESS_ID, DATE, SERVICE_ID, EMPLOYEE_ID
                );

        assertEquals(
                List.of(LocalTime.of(9, 0), LocalTime.of(11, 0)),
                result
        );
    }

    @Test
    void getAllAvailableServiceItemsForToday_existingAppointmentExists_excludesOverlappingTimes() {
        prepareEmployeeWithSchedule(
                LocalTime.of(9, 0),
                LocalTime.of(12, 0)
        );

        Appointment appointment = mock(Appointment.class);
        DateTimeInterval appointmentInterval = mock(DateTimeInterval.class);

        Instant appointmentStart = DATE.atTime(10, 0)
                .atZone(TIMEZONE).toInstant();

        Instant appointmentEnd = DATE.atTime(11, 0)
                .atZone(TIMEZONE).toInstant();

        when(appointment.getInterval()).thenReturn(appointmentInterval);
        when(appointmentInterval.getStart()).thenReturn(appointmentStart);
        when(appointmentInterval.getEnd()).thenReturn(appointmentEnd);
        when(appointment.getStatus()).thenReturn(AppointmentStatus.CONFIRMED);

        when(appointmentService.findByEmployeeIdOnDate(EMPLOYEE_ID, DATE))
                .thenReturn(List.of(appointment));

        List<LocalTime> result =
                appointmentAvailabilityService.getAllAvailableAppointmentTimesForToday(
                        BUSINESS_ID, DATE, SERVICE_ID, EMPLOYEE_ID
                );

        assertEquals(
                List.of(LocalTime.of(9, 0), LocalTime.of(11, 0)),
                result
        );
    }

    @Test
    void getAllAvailableServiceItemsForToday_employeeHasNoWorkSchedule_returnsEmptyList() {
        when(employeeService.findById(EMPLOYEE_ID)).thenReturn(employee);

        when(employeeService.findWorkScheduleByEmployeeId(
                EMPLOYEE_ID, DATE.getDayOfWeek()))
                .thenReturn(Optional.empty());

        List<LocalTime> result =
                appointmentAvailabilityService.getAllAvailableAppointmentTimesForToday(
                        BUSINESS_ID, DATE, SERVICE_ID, EMPLOYEE_ID
                );

        assertTrue(result.isEmpty());

        verify(employeeService, never())
                .getUnavailableSlots(anyInt(), any(), any(), any());
    }

    @Test
    void getAllAvailableServiceItemsForToday_serviceBelongsToDifferentBusiness_throwsException() {
        when(business.getId()).thenReturn(999);

        assertThrows(
                RuntimeException.class,
                () -> appointmentAvailabilityService.getAllAvailableAppointmentTimesForToday(
                        BUSINESS_ID, DATE, SERVICE_ID, EMPLOYEE_ID
                )
        );
    }

    @Test
    void getAllAvailableServiceItemsForToday_serviceIsInactive_throwsException() {
        when(serviceItem.isActive()).thenReturn(false);

        assertThrows(
                RuntimeException.class,
                () -> appointmentAvailabilityService.getAllAvailableAppointmentTimesForToday(
                        BUSINESS_ID, DATE, SERVICE_ID, EMPLOYEE_ID
                )
        );
    }

    @Test
    void getAllAvailableServiceItemsForToday_employeeDoesNotOfferService_throwsException() {
        when(employee.getServiceItems()).thenReturn(Set.of());

        assertThrows(
                RuntimeException.class,
                () -> appointmentAvailabilityService.getAllAvailableAppointmentTimesForToday(
                        BUSINESS_ID, DATE, SERVICE_ID, EMPLOYEE_ID
                )
        );
    }

    @Test
    void getAllAvailableServiceItemsForToday_employeeIsInactive_throwsException() {
        when(employee.isActive()).thenReturn(false);

        assertThrows(
                RuntimeException.class,
                () -> appointmentAvailabilityService.getAllAvailableAppointmentTimesForToday(
                        BUSINESS_ID, DATE, SERVICE_ID, EMPLOYEE_ID
                )
        );
    }

    @Test
    void getAllAvailableServiceItemsForToday_employeeBelongsToDifferentBusiness_throwsException() {
        Business anotherBusiness = mock(Business.class);
        when(anotherBusiness.getId()).thenReturn(999);
        when(employee.getBusiness()).thenReturn(anotherBusiness);

        assertThrows(
                RuntimeException.class,
                () -> appointmentAvailabilityService.getAllAvailableAppointmentTimesForToday(
                        BUSINESS_ID, DATE, SERVICE_ID, EMPLOYEE_ID
                )
        );
    }

    //endregion


    /// //////////////////////////////////////////////
    ///  TEST FOR NO EMPLOYEE CHOSEN
    /// //////////////////////////////////////////////
    //region No employee selected

    @Test
    void getAllAvailableServiceItemsForToday_noEmployeeSelected_returnsCombinedDeduplicatedAvailableTimes() {
        Employee employee1 = mock(Employee.class);
        Employee employee2 = mock(Employee.class);

        prepareEmployeeWithSchedule(
                employee1, 21,
                LocalTime.of(9, 0),
                LocalTime.of(12, 0)
        );

        prepareEmployeeWithSchedule(
                employee2, 22,
                LocalTime.of(10, 0),
                LocalTime.of(13, 0)
        );

        when(employeeService.findAllEmployeesByServiceId(SERVICE_ID))
                .thenReturn(List.of(employee1, employee2));

        List<LocalTime> result =
                appointmentAvailabilityService.getAllAvailableAppointmentTimesForToday(
                        BUSINESS_ID, DATE, SERVICE_ID, null
                );

        assertEquals(
                List.of(
                        LocalTime.of(9, 0),
                        LocalTime.of(9, 30),
                        LocalTime.of(10, 0),
                        LocalTime.of(10, 30),
                        LocalTime.of(11, 0),
                        LocalTime.of(11, 30),
                        LocalTime.of(12, 0)
                ),
                result
        );
    }

    @Test
    void getAllAvailableServiceItemsForToday_noEmployeeSelected_noEligibleEmployees_returnsEmptyList() {
        when(employeeService.findAllEmployeesByServiceId(SERVICE_ID))
                .thenReturn(List.of());

        List<LocalTime> result =
                appointmentAvailabilityService.getAllAvailableAppointmentTimesForToday(
                        BUSINESS_ID, DATE, SERVICE_ID, null
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllAvailableServiceItemsForToday_noEmployeeSelected_employeeHasNoWorkSchedule_ignoresEmployee() {
        Employee employee1 = mock(Employee.class);
        Employee employee2 = mock(Employee.class);

        prepareEmployeeWithSchedule(
                employee1, 21,
                LocalTime.of(9, 0),
                LocalTime.of(12, 0)
        );

        when(employee2.getId()).thenReturn(22);

        when(employeeService.findWorkScheduleByEmployeeId(
                22, DATE.getDayOfWeek()))
                .thenReturn(Optional.empty());

        when(employeeService.findAllEmployeesByServiceId(SERVICE_ID))
                .thenReturn(List.of(employee1, employee2));

        List<LocalTime> result =
                appointmentAvailabilityService.getAllAvailableAppointmentTimesForToday(
                        BUSINESS_ID, DATE, SERVICE_ID, null
                );

        assertEquals(
                List.of(
                        LocalTime.of(9, 0),
                        LocalTime.of(9, 30),
                        LocalTime.of(10, 0),
                        LocalTime.of(10, 30),
                        LocalTime.of(11, 0)
                ),
                result
        );

        verify(employeeService, never())
                .getUnavailableSlots(22, DATE, null, null);
    }

//endregion

}