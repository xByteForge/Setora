package com.byteforge.setorabackend.business.dtos;

import com.byteforge.setorabackend.auth.User;
import com.byteforge.setorabackend.employees.model.Employee;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

public record EmployeeReferenceDto(
        UUID id,
        String name,
        String firstName,
        String lastName,
        String initials,
        String email,
        String phoneNumber,
        List<UUID> locationIds
) {
    public static EmployeeReferenceDto from(Employee employee) {
        User user = employee.getUser();
        String first = user != null && user.getFirstname() != null ? user.getFirstname().trim() : "";
        String last = user != null && user.getSurname() != null ? user.getSurname().trim() : "";
        String fullName = (first + " " + last).trim();
        String initials = computeInitials(first, last);
        UUID businessId = employee.getBusiness() != null ? employee.getBusiness().getId() : null;
        List<UUID> locations = businessId != null ? List.of(businessId) : List.of();

        return new EmployeeReferenceDto(
                employee.getId(),
                fullName,
                first,
                last,
                initials,
                user != null ? user.getEmail() : null,
                user != null ? user.getPhoneNumber() : null,
                locations
        );
    }

    private static String computeInitials(String first, String last) {
        StringBuilder sb = new StringBuilder();
        if (first != null && !first.isBlank()) {
            sb.append(first.trim().charAt(0));
        }
        if (last != null && !last.isBlank()) {
            sb.append(last.trim().charAt(0));
        }
        return sb.toString().toUpperCase(Locale.ROOT);
    }
}

