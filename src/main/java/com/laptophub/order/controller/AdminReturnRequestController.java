package com.laptophub.order.controller;

import com.laptophub.order.dto.request.ReturnRequestDecisionRequest;
import com.laptophub.order.dto.response.ReturnRequestResponse;
import com.laptophub.order.enums.ReturnRequestStatus;
import com.laptophub.order.service.ReturnRequestService;
import com.laptophub.security.currentuser.CurrentUserProvider;
import com.laptophub.shared.response.ApiResponse;
import com.laptophub.shared.response.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/return-requests")
public class AdminReturnRequestController {
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final ReturnRequestService returnRequestService;
    private final CurrentUserProvider currentUserProvider;

    public AdminReturnRequestController(ReturnRequestService returnRequestService,
                                        CurrentUserProvider currentUserProvider) {
        this.returnRequestService = returnRequestService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ReturnRequestResponse>>> list(
            @RequestParam(required = false) ReturnRequestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE));
        Page<ReturnRequestResponse> result = returnRequestService.listAdmin(status, pageable)
                .map(ReturnRequestResponse::from);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách yêu cầu hoàn trả thành công",PageResponse.of(result)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReturnRequestResponse>> getOne(@PathVariable Long id) {
        var request = returnRequestService.getByIdOrThrow(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin yêu cầu hoàn trả thành công",ReturnRequestResponse.from(request)));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<ReturnRequestResponse>> approve(@PathVariable Long id) {
        Long actingAdminId = currentUserProvider.getCurrentUser().userId();
        var request = returnRequestService.approve(id, actingAdminId);
        return ResponseEntity.ok(ApiResponse.success("Duyệt yêu cầu hoàn trả thành công",ReturnRequestResponse.from(request)));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<ReturnRequestResponse>> reject(@PathVariable Long id,
                                                                     @Valid @RequestBody(required = false) ReturnRequestDecisionRequest request) {
        Long actingAdminId = currentUserProvider.getCurrentUser().userId();
        String note = request == null ? null : request.note();
        var result = returnRequestService.reject(id, actingAdminId, note);
        return ResponseEntity.ok(ApiResponse.success("Từ chối yêu cầu hoàn trả thành công",ReturnRequestResponse.from(result)));
    }
}
