package com.laptophub.auth.service.impl;

import com.laptophub.auth.dto.request.ChangePasswordRequest;
import com.laptophub.auth.entity.RevokeReason;
import com.laptophub.auth.repository.RefreshTokenRepository;
import com.laptophub.auth.service.ChangePasswordService;
import com.laptophub.shared.exception.AppException;
import com.laptophub.shared.exception.ErrorCode;
import com.laptophub.user.entity.User;
import com.laptophub.user.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ChangePasswordServiceImpl implements ChangePasswordService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    public ChangePasswordServiceImpl(UserService userService, PasswordEncoder passwordEncoder,
                                     RefreshTokenRepository refreshTokenRepository) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request){
        User user = userService.findById(userId).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        refreshTokenRepository.revokeActiveByUserId(userId, Instant.now(), RevokeReason.LOGOUT);

        User managedUser = userService.findById(userId).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
        managedUser.changePasswordHash(passwordEncoder.encode(request.newPassword()));
    }
}
