package com.byteforge.setorabackend.appointment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.byteforge.setorabackend.appointment.dtos.AppointmentResponseDto;
import com.byteforge.setorabackend.appointment.dtos.CreateAppointmentRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerTest {

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private AppointmentController appointmentController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(appointmentController).build();
    }

    @Test
    void createsAppointmentSuccessfully() throws Exception {
        CreateAppointmentRequestDto request = new CreateAppointmentRequestDto(
                1,
                10,
                20,
                100,
                null,
                null,
                null,
                LocalDate.of(2026, 12, 1),
                LocalTime.of(14, 0),
                PaymentMethod.PHYSICAL,
                AppointmentStatus.CONFIRMED,
                "Note");

        AppointmentResponseDto responseDto = new AppointmentResponseDto(
                55,
                1,
                10,
                "Haircut",
                45,
                100,
                "Maria Popescu",
                "+40722334455",
                "maria@example.com",
                20,
                "George Ionescu",
                LocalDate.of(2026, 12, 1),
                LocalTime.of(14, 0),
                LocalTime.of(14, 45),
                AppointmentStatus.CONFIRMED,
                BigDecimal.valueOf(60.00),
                PaymentMethod.PHYSICAL,
                "Note");

        when(appointmentService.createAppointment(any(CreateAppointmentRequestDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(55))
                .andExpect(jsonPath("$.businessId").value(1))
                .andExpect(jsonPath("$.serviceId").value(10))
                .andExpect(jsonPath("$.serviceName").value("Haircut"))
                .andExpect(jsonPath("$.durationMinutes").value(45))
                .andExpect(jsonPath("$.clientId").value(100))
                .andExpect(jsonPath("$.clientName").value("Maria Popescu"))
                .andExpect(jsonPath("$.employeeId").value(20))
                .andExpect(jsonPath("$.employeeName").value("George Ionescu"))
                .andExpect(jsonPath("$.date").value("2026-12-01"))
                .andExpect(jsonPath("$.startTime").value("14:00:00"))
                .andExpect(jsonPath("$.endTime").value("14:45:00"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.paymentMethod").value("PHYSICAL"))
                .andExpect(jsonPath("$.clientNote").value("Note"));

        verify(appointmentService).createAppointment(any(CreateAppointmentRequestDto.class));
    }

    @Test
    void rejectsInvalidAppointmentRequest() throws Exception {
        // Missing required @NotNull fields
        String invalidJson = "{\"businessId\": 1}";

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
