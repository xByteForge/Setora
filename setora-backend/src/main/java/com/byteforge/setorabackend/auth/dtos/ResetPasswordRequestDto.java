package com.byteforge.setorabackend.auth.dtos;

import jakarta.validation.constraints.*;

// for POST /api/auth/reset-password
public record ResetPasswordRequestDto(
        @NotBlank String token,

        @NotBlank @Size(min = 8, max = 64) String newPassword) {}
