package com.laptophub.auth.service;

import com.laptophub.auth.dto.response.LoginResponse;

public interface RefreshService {
    LoginResponse refresh(String rawRefreshToken);
}
