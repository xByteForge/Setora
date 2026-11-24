package com.byteforge.setorabackend.auth.dtos;

import jakarta.validation.constraints.*;

// for POST /api/auth/register
public record RegisterRequestDto(
        @NotBlank @Size(max = 100) String firstName,

        @NotBlank @Size(max = 100) String lastName,

        @NotBlank @Email @Size(max = 255) String email,

        @Size(max = 30) String phoneNumber,

        @NotBlank @Size(min = 8, max = 64) String password) {}
