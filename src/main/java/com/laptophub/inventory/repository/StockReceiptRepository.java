package com.laptophub.inventory.repository;

import com.laptophub.inventory.entity.StockReceipt;
import com.laptophub.inventory.enums.StockReceiptStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
@Repository
public interface StockReceiptRepository extends JpaRepository<StockReceipt, Long> {

    boolean existsByCode(String code);

    @Query("SELECT r FROM StockReceipt r WHERE (:status IS NULL OR r.status = :status) ORDER BY r.createdAt DESC")
    Page<StockReceipt> search(@Param("status") StockReceiptStatus status, Pageable pageable);

    // Gate atomic DRAFT -> CONFIRMED — chỉ 1 trong N request confirm() đồng
    // thời thắng cuộc đua (xem StockReceiptService.confirm). UPDATE luôn đọc
    // dữ liệu committed mới nhất + khoá row, không dùng snapshot REPEATABLE
    // READ như SELECT thường — nên là gate đáng tin cậy cho race.
    @Modifying(clearAutomatically = true)
    @Query("UPDATE StockReceipt r SET r.status = 'CONFIRMED', r.confirmedByUserId = :confirmedByUserId, "
            + "r.confirmedAt = :confirmedAt WHERE r.id = :id AND r.status = 'DRAFT'")
    int confirmIfDraft(@Param("id") Long id, @Param("confirmedByUserId") Long confirmedByUserId,
                       @Param("confirmedAt") Instant confirmedAt);
}
