package com.byteforge.setorabackend.auth.dtos;

import jakarta.validation.constraints.NotBlank;

// for POST /api/auth/logout
public record LogoutRequestDto(
        @NotBlank
        String refreshToken
) {
}
