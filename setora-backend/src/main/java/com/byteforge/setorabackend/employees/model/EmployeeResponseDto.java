package com.byteforge.setorabackend.employees.model;

import com.byteforge.setorabackend.serviceItem.ServiceItem;
import com.byteforge.setorabackend.user.User;
import java.util.List;

public record EmployeeResponseDto(
        int id,
        int businessId,
        int userId,
        String name,
        String email,
        String phoneNumber,
        String employmentRole,
        boolean active,
        List<Integer> serviceIds) {
    public static EmployeeResponseDto from(Employee employee) {
        User user = employee.getUser();
        List<Integer> serviceIds = employee.getServiceItems() != null
                ? employee.getServiceItems().stream().map(ServiceItem::getId).toList()
                : List.of();

        return new EmployeeResponseDto(
                employee.getId(),
                employee.getBusiness().getId(),
                user != null ? user.getId() : 0,
                user != null ? user.getName() : "",
                user != null ? user.getEmail() : null,
                user != null ? user.getPhoneNumber() : null,
                employee.getEmploymentRole(),
                employee.isActive(),
                serviceIds);
    }
}
