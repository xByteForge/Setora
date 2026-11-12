package com.byteforge.setorabackend.business.dtos;

import com.byteforge.setorabackend.serviceItem.ServiceItem;

import java.math.BigDecimal;
import java.util.UUID;

public record ServiceReferenceDto(
        UUID id,
        String name,
        String description,
        int durationMinutes,
        BigDecimal price,
        boolean active
) {
    public static ServiceReferenceDto from(ServiceItem item) {
        return new ServiceReferenceDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getDurationMinutes(),
                item.getPrice(),
                item.isActive()
        );
    }
}

