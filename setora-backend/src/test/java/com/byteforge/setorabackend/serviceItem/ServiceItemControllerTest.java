package com.byteforge.setorabackend.serviceItem;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.byteforge.setorabackend.appointment.PaymentMethod;
import com.byteforge.setorabackend.serviceItem.dtos.ServiceResponseDto;
import java.math.BigDecimal;
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
class ServiceItemControllerTest {

    @Mock
    private ServiceItemService serviceItemService;

    @InjectMocks
    private ServiceItemController serviceItemController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(serviceItemController).build();
    }

    @Test
    void returnsServicesForBusiness() throws Exception {
        int businessId = 1;
        ServiceResponseDto dto = new ServiceResponseDto(
                10,
                businessId,
                "Haircut",
                "Classic haircut",
                null,
                BigDecimal.valueOf(50.00),
                30,
                true,
                PaymentMethod.PHYSICAL);

        when(serviceItemService.getServicesByBusinessId(businessId)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/services")
                        .param("businessId", String.valueOf(businessId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].businessId").value(businessId))
                .andExpect(jsonPath("$[0].name").value("Haircut"))
                .andExpect(jsonPath("$[0].durationMinutes").value(30))
                .andExpect(jsonPath("$[0].price").value(50.00))
                .andExpect(jsonPath("$[0].paymentMethod").value("PHYSICAL"));

        verify(serviceItemService).getServicesByBusinessId(businessId);
    }

    @Test
    void returnsEmptyListWhenNoServices() throws Exception {
        int businessId = 99;
        when(serviceItemService.getServicesByBusinessId(businessId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/services")
                        .param("businessId", String.valueOf(businessId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(serviceItemService).getServicesByBusinessId(businessId);
    }

    @Test
    void rejectsMissingBusinessId() throws Exception {
        mockMvc.perform(get("/api/services").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
