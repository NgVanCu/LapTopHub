package com.laptophub.review.service.impl;

import com.laptophub.order.service.OrderService;
import com.laptophub.product.entity.Product;
import com.laptophub.product.enums.ProductStatus;
import com.laptophub.product.service.ProductService;
import com.laptophub.review.dto.projection.ReviewSummary;
import com.laptophub.review.dto.response.AdminReviewResponse;
import com.laptophub.review.dto.response.ReviewResponse;
import com.laptophub.review.dto.response.ReviewSummaryResponse;
import com.laptophub.review.entity.Review;
import com.laptophub.review.enums.ReviewStatus;
import com.laptophub.review.repository.ReviewRepository;
import com.laptophub.review.service.ReviewService;
import com.laptophub.shared.exception.AppException;
import com.laptophub.shared.exception.ErrorCode;
import com.laptophub.user.entity.User;
import com.laptophub.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductService productService;
    private final OrderService orderService;
    private final UserService userService;

    public ReviewServiceImpl(ReviewRepository reviewRepository, ProductService productService,
                             OrderService orderService, UserService userService) {
        this.reviewRepository = reviewRepository;
        this.productService = productService;
        this.orderService = orderService;
        this.userService = userService;
    }
    @Override
    @Transactional
    public ReviewResponse create(Long userId, Long productId, int rating, String comment) {
        Product product = productService.getByIdOrThrow(productId);
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        Long orderId = orderService.findDeliveredOrderIdForProduct(userId, productId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_ELIGIBLE));

        if (reviewRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = reviewRepository.save(Review.create(productId, userId, orderId, rating, comment));
        return ReviewResponse.from(review, reviewerName(userId));
    }

    // Trả về review của chính user kể cả khi bị Admin HIDDEN — dùng để FE
    // biết đã review chưa (ẩn form) mà không lộ review của người khác.
    @Override
    public Optional<ReviewResponse> getMyReview(Long userId, Long productId) {
        return reviewRepository.findByUserIdAndProductId(userId, productId)
                .map(review -> ReviewResponse.from(review, reviewerName(userId)));
    }

    // Public — chỉ VISIBLE, batch fetch tên người review 1 lần cho cả trang,
    // tránh N+1.
    @Override
    public Page<ReviewResponse> listPublicByProduct(Long productId, Pageable pageable) {
        Page<Review> page = reviewRepository.findByProductIdAndStatusOrderByCreatedAtDesc(productId,
                ReviewStatus.VISIBLE, pageable);
        Map<Long, String> namesByUserId = batchReviewerNames(page.getContent());
        return page.map(review -> ReviewResponse.from(review, namesByUserId.get(review.getUserId())));
    }

    // averageRating null khi chưa có review VISIBLE nào (không có hàng để
    // AVG) — trả thẳng null, không quy về 0 (tránh hiểu nhầm "0 sao").
    @Override
    public ReviewSummaryResponse getSummary(Long productId) {
        ReviewSummary summary = reviewRepository.getSummary(productId);
        return new ReviewSummaryResponse(summary.getAverageRating(), summary.getReviewCount());
    }

    // Admin — mọi status, filter tùy chọn theo productId/status.
    @Override
    public Page<AdminReviewResponse> listForAdmin(Long productId, ReviewStatus status, Pageable pageable) {
        Page<Review> page = reviewRepository.search(productId, status, pageable);
        Map<Long, String> namesByUserId = batchReviewerNames(page.getContent());
        return page.map(review -> AdminReviewResponse.from(review, namesByUserId.get(review.getUserId())));
    }

    // Admin ẩn/hiện lại review vi phạm — không sửa nội dung, không hard
    // delete (PROJECT_RULES.md §7).
    @Override
    @Transactional
    public AdminReviewResponse hide(Long reviewId) {
        Review review = getByIdOrThrow(reviewId);
        review.hide();
        return AdminReviewResponse.from(review, reviewerName(review.getUserId()));
    }

    @Override
    @Transactional
    public AdminReviewResponse unhide(Long reviewId) {
        Review review = getByIdOrThrow(reviewId);
        review.unhide();
        return AdminReviewResponse.from(review, reviewerName(review.getUserId()));
    }

    private Review getByIdOrThrow(Long reviewId) {
        return reviewRepository.findById(reviewId).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private Map<Long, String> batchReviewerNames(List<Review> reviews) {
        return userService.findFullNamesByIds(reviews.stream().map(Review::getUserId).distinct().toList());
    }

    // Chỉ 1 user/lần gọi (không phải danh sách) nên tra trực tiếp qua
    // UserService.findById, không cần batch findFullNamesByIds ở đây.
    private String reviewerName(Long userId) {
        return userService.findById(userId).map(User::getFullName).orElse(null);
    }
}
