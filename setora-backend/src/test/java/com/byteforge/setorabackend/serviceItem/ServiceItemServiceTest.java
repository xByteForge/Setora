package com.byteforge.setorabackend.serviceItem;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.byteforge.setorabackend.appointment.PaymentMethod;
import com.byteforge.setorabackend.business.model.Business;
import com.byteforge.setorabackend.serviceItem.dtos.ServiceResponseDto;
import com.byteforge.setorabackend.serviceItem.mapper.ServiceItemMapper;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ServiceItemServiceTest {

    @Mock
    private ServiceItemRepository serviceItemRepository;

    @Mock
    private ServiceItemMapper serviceItemMapper;

    @InjectMocks
    private ServiceItemService serviceItemService;

    @Test
    void returnsServiceWhenExists() {
        ServiceItem item = createService(1, 10, "Haircut", 30, BigDecimal.valueOf(50.0));
        when(serviceItemRepository.findById(1)).thenReturn(Optional.of(item));

        ServiceItem result = serviceItemService.findById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Haircut", result.getName());
        verify(serviceItemRepository).findById(1);
    }

    @Test
    void throwsNotFoundWhenServiceMissing() {
        when(serviceItemRepository.findById(99)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> serviceItemService.findById(99));
        assertEquals("Service not found", ex.getMessage());
        verify(serviceItemRepository).findById(99);
    }

    @Test
    void returnsServicesForBusiness() {
        int businessId = 10;
        ServiceItem item1 = createService(101, businessId, "Haircut", 30, BigDecimal.valueOf(50.0));
        ServiceItem item2 = createService(102, businessId, "Beard Trim", 20, BigDecimal.valueOf(30.0));

        ServiceResponseDto dto1 = new ServiceResponseDto(
                101, businessId, "Haircut", "Desc", null, BigDecimal.valueOf(50.0), 30, true, PaymentMethod.PHYSICAL);
        ServiceResponseDto dto2 = new ServiceResponseDto(
                102,
                businessId,
                "Beard Trim",
                "Desc",
                null,
                BigDecimal.valueOf(30.0),
                20,
                true,
                PaymentMethod.PHYSICAL);

        when(serviceItemRepository.findByBusinessId(businessId)).thenReturn(List.of(item1, item2));
        when(serviceItemMapper.toResponseDto(item1)).thenReturn(dto1);
        when(serviceItemMapper.toResponseDto(item2)).thenReturn(dto2);

        List<ServiceResponseDto> result = serviceItemService.getServicesByBusinessId(businessId);

        assertEquals(2, result.size());
        assertEquals(dto1, result.get(0));
        assertEquals(dto2, result.get(1));
        verify(serviceItemRepository).findByBusinessId(businessId);
        verify(serviceItemMapper).toResponseDto(item1);
        verify(serviceItemMapper).toResponseDto(item2);
    }

    @Test
    void returnsEmptyListWhenNoServicesForBusiness() {
        int businessId = 99;
        when(serviceItemRepository.findByBusinessId(businessId)).thenReturn(List.of());

        List<ServiceResponseDto> result = serviceItemService.getServicesByBusinessId(businessId);

        assertTrue(result.isEmpty());
        verify(serviceItemRepository).findByBusinessId(businessId);
        verifyNoInteractions(serviceItemMapper);
    }

    private ServiceItem createService(int id, int businessId, String name, int duration, BigDecimal price) {
        Business business = new Business();
        business.setId(businessId);

        ServiceItem item = new ServiceItem();
        item.setId(id);
        item.setBusiness(business);
        item.setName(name);
        item.setDurationMinutes(duration);
        item.setPrice(price);
        return item;
    }
}
