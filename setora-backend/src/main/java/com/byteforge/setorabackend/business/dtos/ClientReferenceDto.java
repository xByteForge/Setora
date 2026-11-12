package com.byteforge.setorabackend.business.dtos;

import com.byteforge.setorabackend.auth.User;

import java.util.UUID;

public record ClientReferenceDto(
        UUID id,
        String name,
        String firstName,
        String lastName,
        String email,
        String phoneNumber
) {
    public static ClientReferenceDto from(User user) {
        String first = user.getFirstname() != null ? user.getFirstname().trim() : "";
        String last = user.getSurname() != null ? user.getSurname().trim() : "";
        String fullName = (first + " " + last).trim();
        return new ClientReferenceDto(
                user.getId(),
                fullName,
                first,
                last,
                user.getEmail(),
                user.getPhoneNumber()
        );
    }
}

