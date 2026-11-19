package com.byteforge.setorabackend.auth.dtos;

import jakarta.validation.constraints.*;

// for PUT /api/auth/change-password
public record ChangePasswordRequestDto(
        @NotBlank
        String currentPassword,     // needs password in order to change it

        @NotBlank
        @Size(min = 8, max = 64)
        String newPassword
) {
}
