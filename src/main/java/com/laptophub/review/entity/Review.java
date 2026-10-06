package com.laptophub.review.entity;

import com.laptophub.review.enums.ReviewStatus;
import com.laptophub.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "rating", nullable = false)
    private int rating;

    @Column(name = "comment", nullable = false, length = 2000)
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReviewStatus status;

    private Review(Long productId, Long userId, Long orderId, int rating, String comment) {
        this.productId = Objects.requireNonNull(productId, "productId không được để trống");
        this.userId = Objects.requireNonNull(userId, "userId không được để trống");
        this.orderId = Objects.requireNonNull(orderId, "orderId không được để trống");
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("rating chỉ nằm trong khoang 1 đến 5");
        }
        this.rating = rating;
        this.comment = Objects.requireNonNull(comment, "comment không được để trống");
        this.status = ReviewStatus.VISIBLE;
    }

    public static Review create(Long productId, Long userId, Long orderId, int rating, String comment) {
        return new Review(productId, userId, orderId, rating, comment);
    }

    public void hide() {
        this.status = ReviewStatus.HIDDEN;
    }

    public void unhide() {
        this.status = ReviewStatus.VISIBLE;
    }
}