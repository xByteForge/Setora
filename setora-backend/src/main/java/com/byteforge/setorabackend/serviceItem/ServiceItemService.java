package com.byteforge.setorabackend.serviceItem;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceItemService {
    final private ServiceItemRepository serviceItemRepository;

    public ServiceItem findById (UUID serviceId){
        return serviceItemRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));

    }
}
