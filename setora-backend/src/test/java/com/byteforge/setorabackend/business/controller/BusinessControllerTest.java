package com.byteforge.setorabackend.business.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.byteforge.setorabackend.appointment.PaymentMethod;
import com.byteforge.setorabackend.business.dtos.BusinessDetailedResponseDto;
import com.byteforge.setorabackend.business.service.BusinessService;
import com.byteforge.setorabackend.employees.model.EmployeeResponseDto;
import com.byteforge.setorabackend.employees.service.EmployeeService;
import com.byteforge.setorabackend.serviceItem.ServiceItemService;
import com.byteforge.setorabackend.serviceItem.dtos.ServiceResponseDto;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
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
class BusinessControllerTest {

    @Mock
    private BusinessService businessService;

    @Mock
    private ServiceItemService serviceItemService;

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private BusinessController businessController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(businessController).build();
    }

    @Test
    void returnsServicesForBusiness() throws Exception {
        int businessId = 1;
        ServiceResponseDto serviceDto = new ServiceResponseDto(
                10,
                businessId,
                "Haircut",
                "Classic haircut",
                null,
                BigDecimal.valueOf(50.00),
                30,
                true,
                PaymentMethod.PHYSICAL);

        when(serviceItemService.getServicesByBusinessId(businessId)).thenReturn(List.of(serviceDto));

        mockMvc.perform(get("/api/businesses/{businessId}/services", businessId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].businessId").value(businessId))
                .andExpect(jsonPath("$[0].name").value("Haircut"))
                .andExpect(jsonPath("$[0].price").value(50.00));

        verify(serviceItemService).getServicesByBusinessId(businessId);
    }

    @Test
    void returnsEmptyListWhenNoServices() throws Exception {
        int businessId = 99;
        when(serviceItemService.getServicesByBusinessId(businessId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/businesses/{businessId}/services", businessId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(serviceItemService).getServicesByBusinessId(businessId);
    }

    @Test
    void returnsEmployeesForBusiness() throws Exception {
        int businessId = 1;
        EmployeeResponseDto employeeDto = new EmployeeResponseDto(
                20, businessId, 100, "Alex Smith", "alex@example.com", "+40711223344", "Stylist", true, List.of(10));

        when(employeeService.getEmployeesByBusinessId(businessId)).thenReturn(List.of(employeeDto));

        mockMvc.perform(get("/api/businesses/{businessId}/employees", businessId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(20))
                .andExpect(jsonPath("$[0].businessId").value(businessId))
                .andExpect(jsonPath("$[0].name").value("Alex Smith"))
                .andExpect(jsonPath("$[0].email").value("alex@example.com"))
                .andExpect(jsonPath("$[0].employmentRole").value("Stylist"));

        verify(employeeService).getEmployeesByBusinessId(businessId);
    }

    @Test
    void returnsEmptyListWhenNoEmployees() throws Exception {
        int businessId = 99;
        when(employeeService.getEmployeesByBusinessId(businessId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/businesses/{businessId}/employees", businessId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(employeeService).getEmployeesByBusinessId(businessId);
    }

    @Test
    void returnsBusinessById() throws Exception {
        int businessId = 1;
        BusinessDetailedResponseDto dto = new BusinessDetailedResponseDto(
                businessId,
                10,
                null,
                "Studio Armonia",
                "studio-armonia",
                "Description",
                null,
                "+40712345678",
                "studio@example.com",
                "Europe/Bucharest",
                Instant.now(),
                List.of(),
                null,
                List.of(),
                List.of());

        when(businessService.getBusinessById(businessId)).thenReturn(dto);

        mockMvc.perform(get("/api/businesses/{businessId}", businessId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(businessId))
                .andExpect(jsonPath("$.name").value("Studio Armonia"))
                .andExpect(jsonPath("$.slug").value("studio-armonia"));

        verify(businessService).getBusinessById(businessId);
    }
}
