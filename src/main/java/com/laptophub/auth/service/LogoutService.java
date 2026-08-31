package com.laptophub.auth.service;

import com.laptophub.auth.entity.RevokeReason;

public interface LogoutService {

    void logout(Long userId, RevokeReason reason);
}
