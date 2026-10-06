package com.laptophub.voucher.repository;

import com.laptophub.voucher.entity.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    Optional<Voucher> findByCode(String code);

    @Query("SELECT v FROM Voucher v WHERE (:code IS NULL OR v.code = :code) "
            + "AND (:active IS NULL OR v.active = :active) ORDER BY v.createdAt DESC")
    Page<Voucher> search(@Param("code") String code, @Param("active") Boolean active, Pageable pageable);

    // Update có điều kiện — chỉ tăng nếu còn lượt (giống InventoryBalanceRepository).
    // 0 dòng ảnh hưởng = đã hết lượt dùng do race condition giữa các checkout đồng thời.
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Voucher v SET v.usedCount = v.usedCount + 1 "
            + "WHERE v.id = :id AND (v.usageLimit IS NULL OR v.usedCount < v.usageLimit)")
    int incrementUsedCount(@Param("id") Long id);
}
