package com.laptophub.user.dto.response;

import com.laptophub.user.entity.User;
import com.laptophub.user.enums.UserRole;
import com.laptophub.user.enums.UserStatus;

import java.time.Instant;

public record AdminUserResponse(
        Long id,
        String email,
        String fullName,
        String phone,
        UserRole role,
        UserStatus status,
        Instant createdAt,
        Instant updatedAt) {

    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
