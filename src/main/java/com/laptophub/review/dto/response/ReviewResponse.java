package com.laptophub.review.dto.response;

import com.laptophub.review.entity.Review;

import java.time.Instant;

public record ReviewResponse(
        Long id,
        Long productId,
        String reviewerName,
        int rating,
        String comment,
        Instant createdAt) {

    public static ReviewResponse from(Review review, String reviewerName) {
        return new ReviewResponse(
                review.getId(), review.getProductId(), reviewerName, review.getRating(), review.getComment(),
                review.getCreatedAt());
    }
}
