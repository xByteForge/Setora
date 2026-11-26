package com.byteforge.setorabackend.employees.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.byteforge.setorabackend.employees.model.EmployeeResponseDto;
import com.byteforge.setorabackend.employees.service.EmployeeService;
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
class EmployeeControllerTest {

    @Mock
    private EmployeeService availabilityService;

    @InjectMocks
    private EmployeeController employeeController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(employeeController).build();
    }

    @Test
    void returnsEmployeesForBusiness() throws Exception {
        int businessId = 1;
        EmployeeResponseDto dto = new EmployeeResponseDto(
                10, businessId, 100, "Alex Smith", "alex@example.com", "+40711223344", "Stylist", true, List.of(1, 2));

        when(availabilityService.getEmployeesByBusinessId(businessId)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/employees")
                        .param("businessId", String.valueOf(businessId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].businessId").value(businessId))
                .andExpect(jsonPath("$[0].userId").value(100))
                .andExpect(jsonPath("$[0].name").value("Alex Smith"))
                .andExpect(jsonPath("$[0].email").value("alex@example.com"))
                .andExpect(jsonPath("$[0].phoneNumber").value("+40711223344"))
                .andExpect(jsonPath("$[0].employmentRole").value("Stylist"))
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$[0].serviceIds.length()").value(2));

        verify(availabilityService).getEmployeesByBusinessId(businessId);
    }

    @Test
    void returnsEmptyListWhenNoEmployees() throws Exception {
        int businessId = 99;
        when(availabilityService.getEmployeesByBusinessId(businessId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/employees")
                        .param("businessId", String.valueOf(businessId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(availabilityService).getEmployeesByBusinessId(businessId);
    }

    @Test
    void rejectsMissingBusinessId() throws Exception {
        mockMvc.perform(get("/api/employees").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
