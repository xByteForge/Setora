package com.byteforge.setorabackend.business.dtos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record BusinessResponseDto(
        int id,
        int ownerId,
        String category,
        String name,
        String slug,
        String description,
        BusinessLocationDto location,
        String city,
        String phone,
        String email,
        String timezone,
        Instant createdAt,
        List<String> photoUrls,
        String thumbnailUrl,
        List<String> tags,
        BigDecimal minPrice) {}
