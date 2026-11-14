package com.byteforge.setorabackend.business.dtos;

import com.byteforge.setorabackend.business.Business;

import java.util.UUID;

public record BusinessLocationDto(
        UUID id,
        String name,
        String address,
        Double latitude,
        Double longitude
) {
    public static BusinessLocationDto from(Business business) {
        String displayName = business.getName();
        if (business.getAddress() != null && !business.getAddress().isBlank()) {
            displayName = business.getName() + " · " + business.getAddress();
        }
        return new BusinessLocationDto(
                business.getId(),
                displayName,
                business.getAddress(),
                business.getLatitude(),
                business.getLongitude()
        );
    }
}

