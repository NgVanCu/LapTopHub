package com.laptophub.order.controller;

import com.laptophub.order.dto.request.CheckoutRequest;
import com.laptophub.order.dto.request.ReturnRequestCreateRequest;
import com.laptophub.order.dto.response.CheckoutResult;
import com.laptophub.order.dto.response.OrderResponse;
import com.laptophub.order.dto.response.OrderSummaryResponse;
import com.laptophub.order.dto.response.ReturnRequestResponse;
import com.laptophub.order.entity.Order;
import com.laptophub.order.entity.OrderItem;
import com.laptophub.order.enums.PaymentMethod;
import com.laptophub.order.service.OrderService;
import com.laptophub.order.service.ReturnRequestService;
import com.laptophub.payment.dto.response.PaymentUrlResponse;
import com.laptophub.payment.entity.Payment;
import com.laptophub.payment.service.PaymentService;
import com.laptophub.payment.service.VnPayService;
import com.laptophub.security.currentuser.CurrentUserProvider;
import com.laptophub.shared.exception.AppException;
import com.laptophub.shared.exception.ErrorCode;
import com.laptophub.shared.response.ApiResponse;
import com.laptophub.shared.response.PageResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer/orders")
public class CustomerOrderController {

    private final OrderService orderService;
    private final ReturnRequestService returnRequestService;
    private final PaymentService paymentService;
    private final VnPayService vnPayService;
    private final CurrentUserProvider currentUserProvider;

    public CustomerOrderController(OrderService orderService, ReturnRequestService returnRequestService,
                                   PaymentService paymentService, VnPayService vnPayService, CurrentUserProvider currentUserProvider) {
        this.orderService = orderService;
        this.returnRequestService = returnRequestService;
        this.paymentService = paymentService;
        this.vnPayService = vnPayService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(@Valid @RequestBody CheckoutRequest request) {
        Long userId = currentUserProvider.getCurrentUser().userId();
        CheckoutResult result = orderService.checkout(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đặt hàng thành công",toResponse(result.order(), result.items())));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderSummaryResponse>>> list(Pageable pageable) {
        Long userId = currentUserProvider.getCurrentUser().userId();
        var page = orderService.toSummaryResponses(orderService.listByUser(userId, pageable));
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đơn hàng thành công",PageResponse.of(page)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOne(@PathVariable Long id) {
        Long userId = currentUserProvider.getCurrentUser().userId();
        Order order = orderService.getOwnedOrThrow(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin đơn hàng thành công",toResponse(order, orderService.getItems(order.getId()))));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancel(@PathVariable Long id) {
        Long userId = currentUserProvider.getCurrentUser().userId();
        Order order = orderService.cancelByCustomer(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Hủy đơn hàng thành công",toResponse(order, orderService.getItems(order.getId()))));
    }

    // Dùng chung cho cả lần đầu lấy URL thanh toán lẫn mọi lần thử lại (kể cả
    // sau khi thanh toán fail) — paymentService.prepareForRetry luôn sinh
    // gatewayTxnRef/expiresAt mới, không có endpoint/nhánh "retry" riêng.
    @PostMapping("/{id}/payment-url")
    public ResponseEntity<ApiResponse<PaymentUrlResponse>> getPaymentUrl(@PathVariable Long id,
                                                                         HttpServletRequest request) {
        Long userId = currentUserProvider.getCurrentUser().userId();
        Order order = orderService.getOwnedOrThrow(userId, id);
        if (order.getPaymentMethod() != PaymentMethod.ONLINE) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Đơn hàng không dùng thanh toán online");
        }
        Payment payment = paymentService.prepareForRetry(id);
        String paymentUrl = vnPayService.buildPaymentUrl(payment, request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success("Tạo URL thanh toán thành công",new PaymentUrlResponse(paymentUrl)));
    }

    @PostMapping("/{id}/return-requests")
    public ResponseEntity<ApiResponse<ReturnRequestResponse>> createReturnRequest(@PathVariable Long id,
                                                                                  @Valid @RequestBody ReturnRequestCreateRequest request) {
        Long userId = currentUserProvider.getCurrentUser().userId();
        var returnRequest = returnRequestService.create(userId, id, request.reason());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo yêu cầu hoàn trả thành công",ReturnRequestResponse.from(returnRequest)));
    }

    private OrderResponse toResponse(Order order, List<OrderItem> items) {
        return OrderResponse.from(order, orderService.toItemResponses(items),
                orderService.getPaymentStatus(order.getId()));
    }
}
