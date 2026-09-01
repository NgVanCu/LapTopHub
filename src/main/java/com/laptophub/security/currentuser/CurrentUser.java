package com.laptophub.security.currentuser;

import com.laptophub.user.enums.UserRole;

public record CurrentUser(Long userId, String email, UserRole role) {
}