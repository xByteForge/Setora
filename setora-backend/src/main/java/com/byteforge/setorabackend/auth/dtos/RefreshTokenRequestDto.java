package com.byteforge.setorabackend.auth.dtos;

import jakarta.validation.constraints.NotBlank;

// for POST /api/auth/refresh and POST /api/auth/logout
public record RefreshTokenRequestDto(
        @NotBlank
        String refreshToken
) {
}
