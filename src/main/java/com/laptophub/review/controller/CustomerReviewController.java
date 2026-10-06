package com.laptophub.review.controller;

import com.laptophub.review.dto.request.ReviewCreateRequest;
import com.laptophub.review.dto.response.ReviewResponse;
import com.laptophub.review.service.ReviewService;
import com.laptophub.security.currentuser.CurrentUserProvider;
import com.laptophub.shared.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer/products/{productId}/reviews")
public class CustomerReviewController {

    private final ReviewService reviewService;
    private final CurrentUserProvider currentUserProvider;

    public CustomerReviewController(ReviewService reviewService, CurrentUserProvider currentUserProvider) {
        this.reviewService = reviewService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> create(@PathVariable Long productId,
                                                              @Valid @RequestBody ReviewCreateRequest request) {
        Long userId = currentUserProvider.getCurrentUser().userId();
        ReviewResponse response = reviewService.create(userId, productId, request.rating(), request.comment());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tạo đánh giá thành công",response));
    }

    // Trả 200 với data = null nếu chưa review (không phải 404) — đây là 1
    // trạng thái hợp lệ của "form review", không phải lỗi tra cứu.
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ReviewResponse>> getMine(@PathVariable Long productId) {
        Long userId = currentUserProvider.getCurrentUser().userId();
        ReviewResponse response = reviewService.getMyReview(userId, productId).orElse(null);
        return ResponseEntity.ok(ApiResponse.success("Lấy đánh giá của bạn thành công",response));
    }
}