package com.byteforge.setorabackend.auth.dtos;

import com.byteforge.setorabackend.auth.UserRole;
import jakarta.validation.constraints.*;

// for POST /api/users
public record CreateUserRequestDto(
        @NotBlank @Size(max = 100) String firstName,

        @NotBlank @Size(max = 100) String lastName,

        @NotBlank @Email @Size(max = 255) String email,

        @Size(max = 30) String phoneNumber,

        @NotBlank @Size(min = 8, max = 64) String password,

        @NotNull UserRole role) {}
