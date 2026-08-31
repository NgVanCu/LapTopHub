package com.laptophub.auth.controller;

import com.laptophub.auth.dto.request.*;
import com.laptophub.auth.dto.response.LoginResponse;
import com.laptophub.auth.dto.response.LoginResult;
import com.laptophub.auth.dto.response.RegisterResponse;
import com.laptophub.auth.entity.RevokeReason;
import com.laptophub.auth.service.*;
import com.laptophub.auth.token.RefreshTokenCookieFactory;
import com.laptophub.security.service.UserPrincipal;
import com.laptophub.shared.exception.AppException;
import com.laptophub.shared.exception.ErrorCode;
import com.laptophub.shared.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final RegisterService registerService;
    private final EmailVerificationService emailVerificationService;
    private final LoginService loginService;
    private final RefreshTokenCookieFactory refreshTokenCookieFactory;
    private final LogoutService logoutService;
    private final ChangePasswordService changePasswordService;
    private final RefreshService refreshService;
    public AuthController(RegisterService registerService, EmailVerificationService emailVerificationService,
                          LoginService loginService, RefreshTokenCookieFactory refreshTokenCookieFactory,
                          LogoutService logoutService,ChangePasswordService changePasswordService,
                          RefreshService refreshService) {
        this.registerService = registerService;
        this.emailVerificationService = emailVerificationService;
        this.loginService = loginService;
        this.refreshTokenCookieFactory =  refreshTokenCookieFactory;
        this.logoutService = logoutService;
        this.changePasswordService = changePasswordService;
        this.refreshService = refreshService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = RegisterResponse.from(registerService.register(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse
                .success(HttpStatus.CREATED,"Đăng ký thành công, vui lòng xác thực email", response));
    }
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        emailVerificationService.verify(request.token());
        return ResponseEntity.ok(ApiResponse.success("Xác thực email thành công", null));
    }

    @PostMapping("/resend-verification-email")
    public ResponseEntity<ApiResponse<Void>> resendVerificationEmail(
            @Valid @RequestBody ResendVerificationEmailRequest request) {
        emailVerificationService.resend(request.email());
        return ResponseEntity.ok(ApiResponse.success(
                "Nếu email tồn tại và chưa xác thực, email xác thực mới đã được gửi", null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResult>> login(@Valid @RequestBody LoginRequest request,
                                                          HttpServletResponse httpResponse) {
        LoginResponse response = loginService.login(request);
        httpResponse.addHeader(HttpHeaders.SET_COOKIE,
                refreshTokenCookieFactory.build(response.rawRefreshToken()).toString());
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công!",  response.result()));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse httpResponse,
                                                    @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("userId");
        logoutService.logout(userId, RevokeReason.LOGOUT);
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookieFactory.clear().toString());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                            @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("userId");
        changePasswordService.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công", null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResult>> refresh(
            @CookieValue(name = RefreshTokenCookieFactory.COOKIE_NAME, required = false) String rawRefreshToken,
            HttpServletResponse httpResponse) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        LoginResponse result = refreshService.refresh(rawRefreshToken);
        httpResponse.addHeader(HttpHeaders.SET_COOKIE,
                refreshTokenCookieFactory.build(result.rawRefreshToken()).toString());
        return ResponseEntity.ok(ApiResponse.success("Ok",result.result()));
    }
}
