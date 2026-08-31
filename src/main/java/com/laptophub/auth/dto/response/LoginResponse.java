package com.laptophub.auth.dto.response;

public record LoginResponse(LoginResult result, String rawRefreshToken) {
}
