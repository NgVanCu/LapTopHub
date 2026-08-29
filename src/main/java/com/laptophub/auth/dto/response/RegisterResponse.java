package com.laptophub.auth.dto.response;

import com.laptophub.user.entity.User;

public record RegisterResponse(Long id, String email, String fullName, String phone) {
    public static RegisterResponse from(User user) {
        return new RegisterResponse(user.getId(), user.getEmail(), user.getFullName(), user.getPhone());
    }
}
