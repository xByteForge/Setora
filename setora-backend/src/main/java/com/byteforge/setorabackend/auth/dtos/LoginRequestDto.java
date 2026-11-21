package com.byteforge.setorabackend.auth.dtos;

import jakarta.validation.constraints.*;

// for POST /api/auth/login
public record LoginRequestDto(
        @NotBlank @Email String email, @NotBlank String password) {}
