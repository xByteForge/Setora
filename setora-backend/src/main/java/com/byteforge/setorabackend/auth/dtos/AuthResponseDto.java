package com.byteforge.setorabackend.auth.dtos;

// response after login or token refresh
public record AuthResponseDto(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds, // for accessToken
        UserResponseDto user) {}
