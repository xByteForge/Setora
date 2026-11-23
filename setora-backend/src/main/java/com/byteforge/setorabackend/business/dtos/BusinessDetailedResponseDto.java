package com.byteforge.setorabackend.business.dtos;

import com.byteforge.setorabackend.serviceItem.dtos.ServiceResponseDto;

import java.time.Instant;
import java.util.List;

public record BusinessDetailedResponseDto(
        int id,
        int ownerId,
        CategoryDto category,
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
        List<String> tags,
        List<ServiceResponseDto> services
) {}
