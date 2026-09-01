package com.laptophub.user.controller;

import com.laptophub.security.currentuser.CurrentUserProvider;
import com.laptophub.shared.exception.AppException;
import com.laptophub.shared.exception.ErrorCode;
import com.laptophub.shared.response.ApiResponse;
import com.laptophub.shared.response.PageResponse;
import com.laptophub.user.dto.response.AdminUserResponse;
import com.laptophub.user.enums.UserRole;
import com.laptophub.user.enums.UserStatus;
import com.laptophub.user.service.UserService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {
    private final UserService userService;
    private final CurrentUserProvider currentUserProvider;

    public AdminUserController(UserService userService, CurrentUserProvider currentUserProvider) {
        this.userService = userService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminUserResponse>>> list(
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim().toLowerCase();
        var page = userService.search(role, status, normalizedKeyword, pageable).map(AdminUserResponse::from);
        return ResponseEntity.ok(ApiResponse.success("lấy danh sách khách hàng thành công",PageResponse.of(page)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminUserResponse>> getOne(@PathVariable Long id) {
        var user = userService.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        return ResponseEntity.ok(ApiResponse.success("Ok",AdminUserResponse.from(user)));
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<ApiResponse<AdminUserResponse>> block(@PathVariable Long id) {
        Long actingAdminId = currentUserProvider.getCurrentUser().userId();
        var user = userService.blockUser(actingAdminId, id);
        return ResponseEntity.ok(ApiResponse.success("Chặn thành công",AdminUserResponse.from(user)));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<AdminUserResponse>> activate(@PathVariable Long id) {
        var user = userService.activateUser(id);
        return ResponseEntity.ok(ApiResponse.success("Kích hoạt lại thành công",AdminUserResponse.from(user)));
    }
}
