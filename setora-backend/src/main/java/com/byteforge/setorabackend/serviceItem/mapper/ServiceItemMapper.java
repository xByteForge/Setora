package com.byteforge.setorabackend.serviceItem.mapper;

import com.byteforge.setorabackend.serviceItem.ServiceItem;
import com.byteforge.setorabackend.serviceItem.ServiceType;
import com.byteforge.setorabackend.serviceItem.dtos.ServiceResponseDto;
import com.byteforge.setorabackend.serviceItem.dtos.ServiceTypeDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ServiceItemMapper {

    public ServiceResponseDto toResponseDto(ServiceItem item) {
        return new ServiceResponseDto(
                item.getId(),
                item.getBusiness().getId(),
                item.getName(),
                item.getDescription(),
                toServiceTypeDto(item.getServiceType()),
                item.getPrice(),
                item.getDurationMinutes(),
                item.isActive(),
                item.getPaymentMethod()
        );
    }

    public List<ServiceResponseDto> toResponseDtoList(List<ServiceItem> items) {
        if (items == null) {
            return List.of();
        }
        return items.stream()
                .map(this::toResponseDto)
                .toList();
    }

    public ServiceTypeDto toServiceTypeDto(ServiceType serviceType) {
        if (serviceType == null) {
            return null;
        }

        return new ServiceTypeDto(
                serviceType.getId(),
                serviceType.getName(),
                serviceType.getCategory().getId()
        );
    }
}