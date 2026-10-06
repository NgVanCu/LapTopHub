package com.laptophub.voucher.controller;

import com.laptophub.shared.response.ApiResponse;
import com.laptophub.shared.response.PageResponse;
import com.laptophub.voucher.dto.request.VoucherCreateRequest;
import com.laptophub.voucher.dto.request.VoucherUpdateRequest;
import com.laptophub.voucher.dto.response.VoucherResponse;
import com.laptophub.voucher.entity.Voucher;
import com.laptophub.voucher.service.VoucherService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/vouchers")
public class AdminVoucherController {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final VoucherService voucherService;

    public AdminVoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<VoucherResponse>>> list(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE));
        Page<VoucherResponse> result = voucherService.listAdmin(code, active, pageable).map(VoucherResponse::from);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách voucher thành công",PageResponse.of(result)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VoucherResponse>> getOne(@PathVariable Long id) {
        Voucher voucher = voucherService.getByIdOrThrow(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin voucher thành công",VoucherResponse.from(voucher)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VoucherResponse>> create(@Valid @RequestBody VoucherCreateRequest request) {
        Voucher voucher = voucherService.create(request.code(), request.description(), request.discountType(),
                request.discountValue(), request.maxDiscountAmount(), request.minOrderAmount(), request.usageLimit(),
                request.usageLimitPerUser(), request.startAt(), request.endAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tạo voucher thành công",VoucherResponse.from(voucher)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VoucherResponse>> update(@PathVariable Long id,
                                                               @Valid @RequestBody VoucherUpdateRequest request) {
        Voucher voucher = voucherService.update(id, request.description(), request.discountType(),
                request.discountValue(), request.maxDiscountAmount(), request.minOrderAmount(), request.usageLimit(),
                request.usageLimitPerUser(), request.startAt(), request.endAt(), request.active());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật voucher thành công",VoucherResponse.from(voucher)));
    }
}

