package com.byteforge.setorabackend.serviceItem;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
public class ServiceItemService {
    final private ServiceItemRepository serviceItemRepository;

    public ServiceItem findById (int serviceId){
        return serviceItemRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));

    }
}
