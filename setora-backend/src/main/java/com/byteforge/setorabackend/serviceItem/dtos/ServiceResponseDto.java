package com.byteforge.setorabackend.serviceItem.dtos;

import com.byteforge.setorabackend.appointment.PaymentMethod;
import com.byteforge.setorabackend.serviceItem.ServiceItem;
import java.math.BigDecimal;

public record ServiceResponseDto(
        int id,
        int businessId,
        String name,
        String description,
        BigDecimal price,
        int durationMinutes,
        boolean active,
        PaymentMethod paymentMethod) {
    public static ServiceResponseDto from(ServiceItem item) {
        return new ServiceResponseDto(
                item.getId(),
                item.getBusiness().getId(),
                item.getName(),
                item.getDescription(),
                item.getPrice(),
                item.getDurationMinutes(),
                item.isActive(),
                item.getPaymentMethod());
    }
}
