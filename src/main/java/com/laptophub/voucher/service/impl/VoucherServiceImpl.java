package com.laptophub.voucher.service.impl;

import com.laptophub.shared.exception.AppException;
import com.laptophub.shared.exception.ErrorCode;
import com.laptophub.voucher.dto.response.VoucherValidationResult;
import com.laptophub.voucher.entity.Voucher;
import com.laptophub.voucher.entity.VoucherUsage;
import com.laptophub.voucher.enums.VoucherDiscountType;
import com.laptophub.voucher.repository.VoucherRepository;
import com.laptophub.voucher.repository.VoucherUsageRepository;
import com.laptophub.voucher.service.VoucherService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final VoucherUsageRepository voucherUsageRepository;

    public VoucherServiceImpl(VoucherRepository voucherRepository, VoucherUsageRepository voucherUsageRepository) {
        this.voucherRepository = voucherRepository;
        this.voucherUsageRepository = voucherUsageRepository;
    }

    @Override
    public VoucherValidationResult validate(String code, Long userId, BigDecimal orderAmount) {
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));

        if (!voucher.isActive()) {
            throw new AppException(ErrorCode.VOUCHER_NOT_APPLICABLE, "Voucher hiện không hoạt động");
        }
        if (!voucher.isWithinWindow(Instant.now())) {
            throw new AppException(ErrorCode.VOUCHER_NOT_APPLICABLE,
                    "Voucher đã hết hạn hoặc chưa tới thời gian áp dụng");
        }
        if (!voucher.meetsMinOrder(orderAmount)) {
            throw new AppException(ErrorCode.VOUCHER_NOT_APPLICABLE,
                    "Đơn hàng chưa đạt giá trị tối thiểu để áp dụng voucher");
        }
        if (voucher.getUsageLimit() != null && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            throw new AppException(ErrorCode.VOUCHER_NOT_APPLICABLE, "Voucher đã hết lượt sử dụng");
        }
        if (voucher.getUsageLimitPerUser() != null && voucherUsageRepository
                .countByVoucherIdAndUserId(voucher.getId(), userId) >= voucher.getUsageLimitPerUser()) {
            throw new AppException(ErrorCode.VOUCHER_NOT_APPLICABLE, "Bạn đã dùng hết lượt cho voucher này");
        }

        return new VoucherValidationResult(voucher, voucher.computeDiscount(orderAmount));
    }

    @Override
    @Transactional
    public VoucherUsage redeem(Long voucherId, Long orderId, Long userId, BigDecimal discountAmount) {
        int rows = voucherRepository.incrementUsedCount(voucherId);
        if (rows == 0) {
            throw new AppException(ErrorCode.VOUCHER_NOT_APPLICABLE, "Voucher đã hết lượt sử dụng");
        }
        return voucherUsageRepository.save(VoucherUsage.create(voucherId, orderId, userId, discountAmount));
    }

    @Override
    public Voucher getByIdOrThrow(Long id) {
        return voucherRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));
    }

    @Override
    public Page<Voucher> listAdmin(String code, Boolean active, Pageable pageable) {
        return voucherRepository.search(code, active, pageable);
    }

    @Override
    @Transactional
    public Voucher create(String code, String description, VoucherDiscountType discountType, BigDecimal discountValue,
                          BigDecimal maxDiscountAmount, BigDecimal minOrderAmount, Integer usageLimit, Integer usageLimitPerUser,
                          Instant startAt, Instant endAt) {
        if (voucherRepository.findByCode(code).isPresent()) {
            throw new AppException(ErrorCode.RESOURCE_CONFLICT, "Mã voucher đã tồn tại");
        }
        Voucher voucher = Voucher.create(code, description, discountType, discountValue, maxDiscountAmount,
                minOrderAmount, usageLimit, usageLimitPerUser, startAt, endAt);
        return voucherRepository.save(voucher);
    }

    @Override
    @Transactional
    public Voucher update(Long id, String description, VoucherDiscountType discountType, BigDecimal discountValue,
                          BigDecimal maxDiscountAmount, BigDecimal minOrderAmount, Integer usageLimit, Integer usageLimitPerUser,
                          Instant startAt, Instant endAt, boolean active) {
        Voucher voucher = getByIdOrThrow(id);
        voucher.update(description, discountType, discountValue, maxDiscountAmount, minOrderAmount, usageLimit,
                usageLimitPerUser, startAt, endAt, active);
        return voucher;
    }
}
