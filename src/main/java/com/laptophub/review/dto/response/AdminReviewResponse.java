package com.laptophub.review.dto.response;

import com.laptophub.review.entity.Review;
import com.laptophub.review.enums.ReviewStatus;

import java.time.Instant;

public record AdminReviewResponse(
        Long id,
        Long productId,
        Long userId,
        String reviewerName,
        Long orderId,
        int rating,
        String comment,
        ReviewStatus status,
        Instant createdAt) {

    public static AdminReviewResponse from(Review review, String reviewerName) {
        return new AdminReviewResponse(
                review.getId(), review.getProductId(), review.getUserId(), reviewerName, review.getOrderId(),
                review.getRating(), review.getComment(), review.getStatus(), review.getCreatedAt());
    }
}
