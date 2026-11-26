package com.byteforge.setorabackend.appointment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.byteforge.setorabackend.appointment.dtos.AppointmentResponseDto;
import com.byteforge.setorabackend.appointment.dtos.CreateAppointmentRequestDto;
import com.byteforge.setorabackend.business.model.Business;
import com.byteforge.setorabackend.business.service.BusinessService;
import com.byteforge.setorabackend.employees.model.Employee;
import com.byteforge.setorabackend.employees.service.EmployeeService;
import com.byteforge.setorabackend.serviceItem.ServiceItem;
import com.byteforge.setorabackend.serviceItem.ServiceItemService;
import com.byteforge.setorabackend.user.User;
import com.byteforge.setorabackend.user.UserService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private BusinessService businessService;

    @Mock
    private ServiceItemService serviceItemService;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AppointmentService appointmentService;

    private Business business;
    private ServiceItem service;
    private Employee employee;
    private User employeeUser;
    private User registeredClient;

    @BeforeEach
    void setUp() {
        business = createBusiness(1, "Studio Test", ZoneId.of("Europe/Bucharest"));
        service = createService(10, business, "Haircut", 45, BigDecimal.valueOf(60.00));
        employeeUser = createUser(50, "George", "Ionescu", "george@example.com", "+40733445566");
        employee = createEmployee(20, business, employeeUser);
        registeredClient = createUser(100, "Maria", "Popescu", "maria@example.com", "+40722334455");
    }

    @Test
    void createsAppointmentForRegisteredClient() {
        CreateAppointmentRequestDto request = validRequest(100, null, null, null);

        when(serviceItemService.findById(10)).thenReturn(service);
        when(employeeService.findById(20)).thenReturn(employee);
        when(userService.findActiveUserById(100)).thenReturn(registeredClient);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> {
            Appointment a = inv.getArgument(0);
            a.setId(999);
            return a;
        });

        AppointmentResponseDto response = appointmentService.createAppointment(request);

        assertNotNull(response);
        assertEquals(999, response.id());
        assertEquals(1, response.businessId());
        assertEquals(10, response.serviceId());
        assertEquals("Haircut", response.serviceName());
        assertEquals(100, response.clientId());
        assertEquals("Maria Popescu", response.clientName());
        assertEquals("maria@example.com", response.clientEmail());
        assertEquals("+40722334455", response.clientPhone());
        assertEquals(20, response.employeeId());
        assertEquals("George Ionescu", response.employeeName());
        assertEquals(LocalDate.of(2026, 12, 1), response.date());
        assertEquals(LocalTime.of(14, 0), response.startTime());
        assertEquals(LocalTime.of(14, 45), response.endTime());
        assertEquals(AppointmentStatus.CONFIRMED, response.status());
        assertEquals(BigDecimal.valueOf(60.00), response.price());
        assertEquals(PaymentMethod.PHYSICAL, response.paymentMethod());
        assertEquals("Please be on time", response.clientNote());

        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void createsAppointmentForGuestClient() {
        CreateAppointmentRequestDto request = new CreateAppointmentRequestDto(
                1,
                10,
                20,
                null,
                "Ana Guest",
                "ana.guest@example.com",
                "+40799887766",
                LocalDate.of(2026, 12, 2),
                LocalTime.of(10, 30),
                null,
                null,
                "First time visit");

        when(serviceItemService.findById(10)).thenReturn(service);
        when(employeeService.findById(20)).thenReturn(employee);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> {
            Appointment a = inv.getArgument(0);
            a.setId(1000);
            return a;
        });

        AppointmentResponseDto response = appointmentService.createAppointment(request);

        assertNotNull(response);
        assertEquals(1000, response.id());
        assertNull(response.clientId());
        assertEquals("Ana Guest", response.clientName());
        assertEquals("ana.guest@example.com", response.clientEmail());
        assertEquals("+40799887766", response.clientPhone());
        assertEquals(AppointmentStatus.CONFIRMED, response.status());
        assertEquals(PaymentMethod.PHYSICAL, response.paymentMethod());

        verify(appointmentRepository).save(any(Appointment.class));
        verifyNoInteractions(userService);
    }

    @Test
    void rejectsNullRequest() {
        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> appointmentService.createAppointment(null));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Request body is required"));
    }

    @Test
    void rejectsMissingServiceId() {
        CreateAppointmentRequestDto request = new CreateAppointmentRequestDto(
                1, null, 20, 100, null, null, null, LocalDate.of(2026, 12, 1), LocalTime.of(10, 0), null, null, null);

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> appointmentService.createAppointment(request));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("serviceId is required"));
    }

    @Test
    void rejectsMissingEmployeeId() {
        CreateAppointmentRequestDto request = new CreateAppointmentRequestDto(
                1, 10, null, 100, null, null, null, LocalDate.of(2026, 12, 1), LocalTime.of(10, 0), null, null, null);

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> appointmentService.createAppointment(request));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("employeeId is required"));
    }

    @Test
    void rejectsMissingDate() {
        CreateAppointmentRequestDto request = new CreateAppointmentRequestDto(
                1, 10, 20, 100, null, null, null, null, LocalTime.of(10, 0), null, null, null);

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> appointmentService.createAppointment(request));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("date is required"));
    }

    @Test
    void rejectsMissingStartTime() {
        CreateAppointmentRequestDto request = new CreateAppointmentRequestDto(
                1, 10, 20, 100, null, null, null, LocalDate.of(2026, 12, 1), null, null, null, null);

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> appointmentService.createAppointment(request));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("startTime is required"));
    }

    @Test
    void rejectsEmployeeFromDifferentBusiness() {
        Business otherBusiness = createBusiness(2, "Other Studio", ZoneId.of("Europe/Bucharest"));
        employee.setBusiness(otherBusiness);

        CreateAppointmentRequestDto request = validRequest(100, null, null, null);

        when(serviceItemService.findById(10)).thenReturn(service);
        when(employeeService.findById(20)).thenReturn(employee);

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> appointmentService.createAppointment(request));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Employee does not belong to the business offering this service"));
    }

    @Test
    void rejectsBusinessIdMismatch() {
        CreateAppointmentRequestDto request = new CreateAppointmentRequestDto(
                999, 10, 20, 100, null, null, null, LocalDate.of(2026, 12, 1), LocalTime.of(10, 0), null, null, null);

        when(serviceItemService.findById(10)).thenReturn(service);
        when(employeeService.findById(20)).thenReturn(employee);

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> appointmentService.createAppointment(request));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Service does not belong to the specified business"));
    }

    @Test
    void rejectsGuestWithoutName() {
        CreateAppointmentRequestDto request = new CreateAppointmentRequestDto(
                1,
                10,
                20,
                null,
                "   ",
                "guest@example.com",
                "+40711111111",
                LocalDate.of(2026, 12, 1),
                LocalTime.of(10, 0),
                null,
                null,
                null);

        when(serviceItemService.findById(10)).thenReturn(service);
        when(employeeService.findById(20)).thenReturn(employee);

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> appointmentService.createAppointment(request));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("clientName is required when clientId is not provided"));
    }

    @Test
    void rejectsGuestWithoutContact() {
        CreateAppointmentRequestDto request = new CreateAppointmentRequestDto(
                1,
                10,
                20,
                null,
                "Guest Name",
                null,
                "   ",
                LocalDate.of(2026, 12, 1),
                LocalTime.of(10, 0),
                null,
                null,
                null);

        when(serviceItemService.findById(10)).thenReturn(service);
        when(employeeService.findById(20)).thenReturn(employee);

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> appointmentService.createAppointment(request));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("At least one of clientEmail or clientPhone must be provided"));
    }

    @Test
    void throwsConflictOnDataIntegrityViolation() {
        CreateAppointmentRequestDto request = validRequest(100, null, null, null);

        when(serviceItemService.findById(10)).thenReturn(service);
        when(employeeService.findById(20)).thenReturn(employee);
        when(userService.findActiveUserById(100)).thenReturn(registeredClient);
        when(appointmentRepository.save(any(Appointment.class)))
                .thenThrow(new DataIntegrityViolationException("chk_appointment_client"));

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> appointmentService.createAppointment(request));
        assertEquals(409, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Appointment data violates database constraints"));
    }

    private Business createBusiness(int id, String name, ZoneId timezone) {
        Business b = new Business();
        b.setId(id);
        b.setName(name);
        b.setTimezone(timezone);
        return b;
    }

    private ServiceItem createService(int id, Business b, String name, int duration, BigDecimal price) {
        ServiceItem s = new ServiceItem();
        s.setId(id);
        s.setBusiness(b);
        s.setName(name);
        s.setDurationMinutes(duration);
        s.setPrice(price);
        s.setPaymentMethod(PaymentMethod.PHYSICAL);
        return s;
    }

    private User createUser(int id, String first, String last, String email, String phone) {
        User u = new User();
        u.setId(id);
        u.setFirstName(first);
        u.setLastName(last);
        u.setEmail(email);
        u.setPhoneNumber(phone);
        return u;
    }

    private Employee createEmployee(int id, Business b, User u) {
        Employee e = new Employee();
        e.setId(id);
        e.setBusiness(b);
        e.setUser(u);
        return e;
    }

    private CreateAppointmentRequestDto validRequest(Integer clientId, String name, String email, String phone) {
        return new CreateAppointmentRequestDto(
                1,
                10,
                20,
                clientId,
                name,
                email,
                phone,
                LocalDate.of(2026, 12, 1),
                LocalTime.of(14, 0),
                PaymentMethod.PHYSICAL,
                AppointmentStatus.CONFIRMED,
                "Please be on time");
    }
}
