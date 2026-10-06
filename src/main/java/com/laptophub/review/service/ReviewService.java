package com.laptophub.review.service;

import com.laptophub.review.dto.response.AdminReviewResponse;
import com.laptophub.review.dto.response.ReviewResponse;
import com.laptophub.review.dto.response.ReviewSummaryResponse;
import com.laptophub.review.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ReviewService {

    ReviewResponse create(Long userId, Long productId, int rating, String comment);

    Optional<ReviewResponse> getMyReview(Long userId, Long productId);

    Page<ReviewResponse> listPublicByProduct(Long productId, Pageable pageable);

    ReviewSummaryResponse getSummary(Long productId);

    Page<AdminReviewResponse> listForAdmin(Long productId, ReviewStatus status, Pageable pageable);

    AdminReviewResponse hide(Long reviewId);

    AdminReviewResponse unhide(Long reviewId);
}
