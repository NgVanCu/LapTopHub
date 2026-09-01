package com.laptophub.auth.service.impl;

import com.laptophub.auth.dto.response.LoginResponse;
import com.laptophub.auth.dto.response.LoginResult;
import com.laptophub.auth.entity.RefreshToken;
import com.laptophub.auth.entity.RevokeReason;
import com.laptophub.auth.repository.RefreshTokenRepository;
import com.laptophub.auth.service.LogoutService;
import com.laptophub.auth.service.RefreshService;
import com.laptophub.security.jwt.JwtService;
import com.laptophub.security.service.UserPrincipal;
import com.laptophub.shared.exception.AppException;
import com.laptophub.shared.exception.ErrorCode;
import com.laptophub.shared.properties.JwtProperties;
import com.laptophub.shared.util.HashToken;
import com.laptophub.user.entity.User;
import com.laptophub.user.enums.UserStatus;
import com.laptophub.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class RefreshServiceImpl implements RefreshService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final UserService userService;
    private final JwtProperties jwtProperties;

    public RefreshServiceImpl(RefreshTokenRepository refreshTokenRepository,
                              JwtService jwtService, UserService userService,
                              JwtProperties jwtProperties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.userService = userService;
        this.jwtProperties = jwtProperties;
    }

    @Override
    @Transactional(noRollbackFor = AppException.class)
    // ý nghĩa của transactional này là nếu gặp lỗi business thì nó kh roll back
    public LoginResponse refresh(String rawRefreshToken) {
        String refreshTokenHash = HashToken.hash(rawRefreshToken);

        RefreshToken existing = refreshTokenRepository.findByRefreshTokenHash(refreshTokenHash)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        if (existing.getRevokedAt() != null) {
            refreshTokenRepository.revokeActiveByFamilyId(existing.getFamilyId(), Instant.now(), RevokeReason.REUSE_DETECTED);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (existing.getExpiresAt().isBefore(Instant.now())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        User user = userService.findById(existing.getUserId())
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
        String newRawRefreshToken = jwtService.generateRefreshToken();
        String newRefreshTokenHash = HashToken.hash(newRawRefreshToken);
        Instant newRefreshExpiresAt = Instant.now().plus(jwtProperties.refreshExpiration());
        RefreshToken newToken = refreshTokenRepository.save(
                RefreshToken.create(existing.getUserId(),
                        newRefreshTokenHash, existing.getFamilyId(),
                        newRefreshExpiresAt, Instant.now())
        );
        existing.revoke(Instant.now(), RevokeReason.ROTATED);
        existing.markReplacedBy(newToken.getId());

        String accessToken = jwtService.generateAccessToken(UserPrincipal.from(user));
        long expiresInSeconds = jwtProperties.accessTokenExpiration().toSeconds();
        LoginResult result = LoginResult.from(accessToken, "Bearer", expiresInSeconds);
        return new LoginResponse(result, newRawRefreshToken);
    }
}
