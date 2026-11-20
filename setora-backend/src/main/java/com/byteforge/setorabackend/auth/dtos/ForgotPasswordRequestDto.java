package com.byteforge.setorabackend.auth.dtos;

import jakarta.validation.constraints.*;

// for POST /api/auth/forgot-password
public record ForgotPasswordRequestDto(
        @NotBlank
        @Email
        String email
) {
}
