package com.byteforge.setorabackend.user.dtos;

import com.byteforge.setorabackend.user.UserRole;

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
