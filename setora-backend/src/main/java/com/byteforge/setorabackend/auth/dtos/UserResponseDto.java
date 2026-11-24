package com.byteforge.setorabackend.auth.dtos;

import com.byteforge.setorabackend.auth.UserRole;

// what backend returns when a user is fetched, created or updated
public record UserResponseDto(
        int id,
        String firstName,
        String lastName,
        String username,
        String email,
        String phoneNumber,
        UserRole role,
        boolean isDeleted) {}
