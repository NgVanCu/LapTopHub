package com.laptophub.auth.service.impl;

import com.laptophub.auth.dto.request.LoginRequest;
import com.laptophub.auth.dto.response.LoginResponse;
import com.laptophub.auth.dto.response.LoginResult;
import com.laptophub.auth.entity.RefreshToken;
import com.laptophub.auth.repository.RefreshTokenRepository;
import com.laptophub.auth.service.LoginService;
import com.laptophub.security.jwt.JwtService;
import com.laptophub.security.service.UserPrincipal;
import com.laptophub.shared.exception.AppException;
import com.laptophub.shared.exception.ErrorCode;
import com.laptophub.shared.properties.JwtProperties;
import com.laptophub.shared.util.HashToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class LoginServiceImpl implements LoginService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;

    public LoginServiceImpl(AuthenticationManager authenticationManager, JwtService jwtService
            ,JwtProperties jwtProperties, RefreshTokenRepository refreshTokenRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request){
        UserPrincipal principal = authenticate(request);
        if (principal.isPendingVerification()) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_VERIFIED);
        }

        long expiresInSeconds = jwtProperties.accessTokenExpiration().toSeconds();
        String accessToken = jwtService.generateAccessToken(principal);

        String rawRefreshToken = jwtService.generateRefreshToken();
        String hashRefreshToken = HashToken.hash(rawRefreshToken);

        String familyId = UUID.randomUUID().toString();
        Instant refreshExpiresAt = Instant.now().plus(jwtProperties.refreshExpiration());

        refreshTokenRepository.save(
                RefreshToken.create(principal.getId(), hashRefreshToken, familyId, refreshExpiresAt, Instant.now()));
        LoginResult loginResult = LoginResult.from(accessToken, "Bearer", expiresInSeconds);
        return  new LoginResponse(loginResult, rawRefreshToken);
    }

    private UserPrincipal authenticate(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
            return (UserPrincipal) authentication.getPrincipal();
        } catch (DisabledException e) {
            throw new AppException(ErrorCode.ACCOUNT_BLOCKED);
        } catch (BadCredentialsException e) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
    }
}
