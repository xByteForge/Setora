package com.byteforge.setorabackend.business.dtos;

public record BusinessLocationDto(
        String locationName,
        String address,
        Double latitude,
        Double longitude
) {}