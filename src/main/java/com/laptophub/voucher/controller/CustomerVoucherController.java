package com.laptophub.voucher.controller;

import com.laptophub.cart.dto.response.CartLine;
import com.laptophub.cart.service.CartService;
import com.laptophub.security.currentuser.CurrentUserProvider;
import com.laptophub.shared.exception.AppException;
import com.laptophub.shared.exception.ErrorCode;
import com.laptophub.shared.response.ApiResponse;
import com.laptophub.voucher.dto.request.VoucherValidateRequest;
import com.laptophub.voucher.dto.response.VoucherResponse;
import com.laptophub.voucher.dto.response.VoucherValidateResponse;
import com.laptophub.voucher.service.VoucherService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/customer/vouchers")
public class CustomerVoucherController {

    private final VoucherService voucherService;
    private final CartService cartService;
    private final CurrentUserProvider currentUserProvider;

    public CustomerVoucherController(VoucherService voucherService, CartService cartService, CurrentUserProvider currentUserProvider) {
        this.voucherService = voucherService;
        this.cartService = cartService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<VoucherValidateResponse>> validate(
            @Valid @RequestBody VoucherValidateRequest request) {
        Long userId = currentUserProvider.getCurrentUser().userId();
        List<CartLine> lines = cartService.getItemsWithLivePrice(userId);
        if (lines.isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Giỏ hàng trống");
        }

        BigDecimal orderAmount = lines.stream()
                .map(line -> line.variant().getPrice().multiply(BigDecimal.valueOf(line.item().getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var result = voucherService.validate(request.code(), userId, orderAmount);
        return ResponseEntity
                .ok(ApiResponse.success("Kiểm tra voucher thành công",VoucherValidateResponse.of(request.code(), orderAmount, result.discountAmount())));
    }
}

