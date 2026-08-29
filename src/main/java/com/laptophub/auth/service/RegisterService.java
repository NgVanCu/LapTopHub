package com.laptophub.auth.service;

import com.laptophub.auth.dto.request.RegisterRequest;
import com.laptophub.auth.dto.response.RegisterResponse;
import com.laptophub.user.entity.User;

public interface RegisterService {
    User register(RegisterRequest request);
}
