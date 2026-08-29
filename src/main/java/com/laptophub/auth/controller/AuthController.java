package com.laptophub.auth.controller;

import com.laptophub.auth.dto.request.RegisterRequest;
import com.laptophub.auth.dto.request.ResendVerificationEmailRequest;
import com.laptophub.auth.dto.request.VerifyEmailRequest;
import com.laptophub.auth.dto.response.RegisterResponse;
import com.laptophub.auth.service.EmailVerificationService;
import com.laptophub.auth.service.RegisterService;
import com.laptophub.shared.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final RegisterService registerService;
    private final EmailVerificationService emailVerificationService;

    public AuthController(RegisterService registerService, EmailVerificationService emailVerificationService) {
        this.registerService = registerService;
        this.emailVerificationService = emailVerificationService;
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
}
