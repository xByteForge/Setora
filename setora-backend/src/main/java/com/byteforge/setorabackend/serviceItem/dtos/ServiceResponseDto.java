package com.byteforge.setorabackend.serviceItem.dtos;

import com.byteforge.setorabackend.appointment.PaymentMethod;
import java.math.BigDecimal;

public record ServiceResponseDto(
        int id,
        int businessId,
        String name,
        String description,
        ServiceTypeDto type,
        BigDecimal price,
        int durationMinutes,
        boolean active,
        PaymentMethod paymentMethod
) {}
