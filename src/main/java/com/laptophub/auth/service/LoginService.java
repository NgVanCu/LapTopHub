package com.laptophub.auth.service;

import com.laptophub.auth.dto.request.LoginRequest;
import com.laptophub.auth.dto.response.LoginResponse;

public interface LoginService {
    LoginResponse login(LoginRequest request);
}
