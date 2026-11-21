package com.byteforge.setorabackend.business.dtos;

import java.time.Instant;
import java.util.List;

public record BusinessResponseDto(
        int id,
        int ownerId,
        String name,
        String slug,
        String description,
        BusinessLocationDto location,
        String phone,
        String email,
        String timezone,
        Instant createdAt,
        List<String> photoUrls,
        String thumbnailUrl,
        List<String> tags) {}
