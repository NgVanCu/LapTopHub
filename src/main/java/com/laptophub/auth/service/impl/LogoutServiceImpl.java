package com.laptophub.auth.service.impl;

import com.laptophub.auth.entity.RevokeReason;
import com.laptophub.auth.repository.RefreshTokenRepository;
import com.laptophub.auth.service.LogoutService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class LogoutServiceImpl implements LogoutService {
    private final RefreshTokenRepository refreshTokenRepository;

    public LogoutServiceImpl(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public void logout(Long userId, RevokeReason reason) {
        refreshTokenRepository.revokeActiveByUserId(userId, Instant.now(), reason);
    }
}
