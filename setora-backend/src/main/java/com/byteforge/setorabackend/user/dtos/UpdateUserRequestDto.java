package com.byteforge.setorabackend.user.dtos;

import jakarta.validation.constraints.*;
import java.util.Locale;

// for PUT /api/users/{id}
public record UpdateUserRequestDto(
        @NotBlank @Size(max = 100) String firstName,

        @NotBlank @Size(max = 100) String lastName,

        @NotBlank @Size(max = 100) String username,

        @NotBlank @Email @Size(max = 255) String email,

        @Size(max = 30) String phoneNumber) {
    @Override
    public String email() {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
