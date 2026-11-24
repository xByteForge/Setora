package com.byteforge.setorabackend.serviceItem;

import com.byteforge.setorabackend.serviceItem.dtos.ServiceResponseDto;
import com.byteforge.setorabackend.serviceItem.mapper.ServiceItemMapper;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServiceItemService {
    private final ServiceItemRepository serviceItemRepository;

    private final ServiceItemMapper serviceItemMapper;

    public ServiceItem findById(int serviceId) {
        return serviceItemRepository
                .findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));
    }

    @Transactional(readOnly = true)
    public List<ServiceResponseDto> getServicesByBusinessId(int businessId) {
        return serviceItemRepository.findByBusinessId(businessId).stream()
                .map(serviceItemMapper::toResponseDto)
                .toList();
    }
}
