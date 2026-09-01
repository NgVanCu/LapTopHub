package com.laptophub.user.controller;

import com.laptophub.security.currentuser.CurrentUserProvider;
import com.laptophub.shared.exception.AppException;
import com.laptophub.shared.exception.ErrorCode;
import com.laptophub.shared.response.ApiResponse;
import com.laptophub.user.dto.request.UpdateProfileRequest;
import com.laptophub.user.dto.response.ProfileResponse;
import com.laptophub.user.entity.User;
import com.laptophub.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer/profile")
public class CustomerProfileController {
    private final UserService userService;
    private final CurrentUserProvider currentUserProvider;
    public CustomerProfileController(UserService userService, CurrentUserProvider currentUserProvider) {
        this.userService = userService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile() {
        Long userId = currentUserProvider.getCurrentUser().userId();
        User user = userService.findById(userId).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin thành công!",ProfileResponse.from(user)));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = currentUserProvider.getCurrentUser().userId();
        var user = userService.updateProfile(userId, request.fullName(), request.phone());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin thành công",ProfileResponse.from(user)));
    }
}
