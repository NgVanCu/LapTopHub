package com.laptophub.review.controller;

import com.laptophub.review.dto.response.AdminReviewResponse;
import com.laptophub.review.enums.ReviewStatus;
import com.laptophub.review.service.ReviewService;
import com.laptophub.shared.response.ApiResponse;
import com.laptophub.shared.response.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/reviews")
public class AdminReviewController {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final ReviewService reviewService;

    public AdminReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminReviewResponse>>> list(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) ReviewStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE));
        var result = reviewService.listForAdmin(productId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đánh giá thành công",PageResponse.of(result)));
    }

    @PostMapping("/{id}/hide")
    public ResponseEntity<ApiResponse<AdminReviewResponse>> hide(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Ẩn đánh giá thành công",reviewService.hide(id)));
    }

    @PostMapping("/{id}/unhide")
    public ResponseEntity<ApiResponse<AdminReviewResponse>> unhide(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Hiển thị đánh giá thành công",reviewService.unhide(id)));
    }
}

