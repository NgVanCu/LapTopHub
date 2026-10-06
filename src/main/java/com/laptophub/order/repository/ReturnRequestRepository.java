package com.laptophub.order.repository;

import com.laptophub.order.entity.ReturnRequest;
import com.laptophub.order.enums.ReturnRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {

    List<ReturnRequest> findByOrderId(Long orderId);

    // Chặn tạo 2 return request cùng đang REQUESTED cho 1 đơn — kiểm tra ở
    // ReturnRequestService.create trước khi save.
    boolean existsByOrderIdAndStatus(Long orderId, ReturnRequestStatus status);

    // Dùng để kiểm tra ownership khi Customer xem 1 return request cụ thể —
    // không tồn tại hoặc không phải của user này đều coi như not found, giống
    // AddressRepository.findByIdAndUserId.
    Optional<ReturnRequest> findByIdAndUserId(Long id, Long userId);

    // Dùng cho Admin liệt kê mọi return request, lọc theo trạng thái (tùy
    // chọn) — mirror OrderRepository.search.
    @Query("SELECT r FROM ReturnRequest r WHERE (:status IS NULL OR r.status = :status) ORDER BY r.createdAt DESC")
    Page<ReturnRequest> search(@Param("status") ReturnRequestStatus status, Pageable pageable);
}