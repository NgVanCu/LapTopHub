package com.laptophub.payment.controller;

import com.laptophub.payment.dto.response.PaymentResponse;
import com.laptophub.payment.enums.PaymentStatus;
import com.laptophub.payment.service.PaymentService;
import com.laptophub.shared.response.ApiResponse;
import com.laptophub.shared.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/payments")
public class AdminPaymentController {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final PaymentService paymentService;

    public AdminPaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> list(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) Long orderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE));
        Page<PaymentResponse> result = paymentService.listAdmin(status, orderId, pageable).map(PaymentResponse::from);
        return ResponseEntity.ok(ApiResponse.success( "Lấy danh sách thanh toán thành công",PageResponse.of(result)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin thanh toán thành công",PaymentResponse.from(paymentService.getByIdOrThrow(id))));
    }
}
