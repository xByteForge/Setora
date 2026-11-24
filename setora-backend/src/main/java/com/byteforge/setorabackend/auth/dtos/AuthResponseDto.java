package com.byteforge.setorabackend.auth.dtos;

import com.byteforge.setorabackend.user.dtos.UserResponseDto;

// response after login or token refresh
public record AuthResponseDto(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds, // for accessToken
        UserResponseDto user) {}
