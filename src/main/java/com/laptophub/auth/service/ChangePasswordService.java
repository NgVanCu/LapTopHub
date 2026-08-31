package com.laptophub.auth.service;

import com.laptophub.auth.dto.request.ChangePasswordRequest;

public interface ChangePasswordService {
    void changePassword(Long userId, ChangePasswordRequest request);
}
