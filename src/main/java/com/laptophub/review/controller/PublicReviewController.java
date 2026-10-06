package com.laptophub.review.controller;

import com.laptophub.review.dto.response.ReviewResponse;
import com.laptophub.review.dto.response.ReviewSummaryResponse;
import com.laptophub.review.service.ReviewService;
import com.laptophub.shared.response.ApiResponse;
import com.laptophub.shared.response.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public/products/{productId}/reviews")
public class PublicReviewController {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final ReviewService reviewService;

    public PublicReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> list(@PathVariable Long productId,
                                                                          @RequestParam(defaultValue = "0") int page,
                                                                          @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0),  Math.clamp(size, 1, MAX_PAGE_SIZE));
        var result = reviewService.listPublicByProduct(productId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đánh giá thành công",PageResponse.of(result)));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<ReviewSummaryResponse>> summary(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success("Lấy tổng quan đánh giá thành công",reviewService.getSummary(productId)));
    }
}
