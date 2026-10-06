package com.laptophub.voucher.repository;

import com.laptophub.voucher.entity.VoucherUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoucherUsageRepository extends JpaRepository<VoucherUsage, Long> {

    long countByVoucherIdAndUserId(Long voucherId, Long userId);

    Optional<VoucherUsage> findByOrderId(Long orderId);
}
