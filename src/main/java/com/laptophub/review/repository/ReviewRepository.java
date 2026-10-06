package com.laptophub.review.repository;

import com.laptophub.review.dto.projection.ReviewSummary;
import com.laptophub.review.entity.Review;
import com.laptophub.review.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    // Dùng bởi CustomerReviewController để FE biết đã review chưa — trả về
    // review của chính user kể cả khi bị Admin HIDDEN (họ vẫn cần thấy nội
    // dung mình đã viết).
    Optional<Review> findByUserIdAndProductId(Long userId, Long productId);

    // Public — chỉ VISIBLE. OrderByCreatedAtDesc để đảm bảo thứ tự phân trang
    // ổn định (derived query không có ORDER BY mặc định thì không deterministic).
    Page<Review> findByProductIdAndStatusOrderByCreatedAtDesc(Long productId, ReviewStatus status, Pageable pageable);

    // Admin — mọi status, filter tùy chọn theo productId/status, mirror
    // ProductRepository.searchAdmin/OrderRepository.search.
    @Query("""
            select r from Review r
            where (:productId is null or r.productId = :productId)
              and (:status is null or r.status = :status)
            order by r.createdAt desc
            """)
    Page<Review> search(@Param("productId") Long productId, @Param("status") ReviewStatus status, Pageable pageable);

    // averageRating null khi chưa có review VISIBLE nào (không có hàng để
    // AVG) — ReviewService tự map null -> 0/không hiển thị.
    @Query("""
            select avg(r.rating) as averageRating, count(r) as reviewCount
            from Review r
            where r.productId = :productId and r.status = 'VISIBLE'
            """)
    ReviewSummary getSummary(@Param("productId") Long productId);
}
