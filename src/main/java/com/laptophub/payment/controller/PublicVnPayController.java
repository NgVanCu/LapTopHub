package com.laptophub.payment.controller;

import com.laptophub.payment.dto.response.VnPayIpnResponse;
import com.laptophub.payment.dto.response.VnPayReturnResponse;
import com.laptophub.payment.entity.Payment;
import com.laptophub.payment.enums.PaymentStatus;
import com.laptophub.payment.service.PaymentService;
import com.laptophub.payment.service.VnPayService;
import com.laptophub.shared.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/public/payments/vnpay")
public class PublicVnPayController {

    private static final Logger log = LoggerFactory.getLogger(PublicVnPayController.class);

    private final PaymentService paymentService;
    private final VnPayService vnPayService;


    public PublicVnPayController(PaymentService paymentService, VnPayService vnPayService) {
        this.paymentService = paymentService;
        this.vnPayService = vnPayService;
    }

    // Response theo đúng hợp đồng VNPay (KHÔNG bọc ApiResponse — xem
    // VnPayIpnResponse). Idempotent bằng update có điều kiện ở
    // PaymentRepository — 0 dòng ảnh hưởng nghĩa là đã xử lý trước đó (VNPay
    // có thể gọi lại IPN nhiều lần) hoặc payment đã ở trạng thái cuối khác
    // (CANCELLED do đơn bị hủy trong lúc khách đang thanh toán).
    @GetMapping("/ipn")
    public VnPayIpnResponse ipn(@RequestParam Map<String, String> params) {
        if (!vnPayService.verifySignature(params)) {
            return VnPayIpnResponse.of("97", "Invalid signature");
        }

        Payment payment = paymentService.findByGatewayTxnRef(params.get("vnp_TxnRef")).orElse(null);
        if (payment == null) {
            return VnPayIpnResponse.of("01", "Order not found");
        }

        String expectedAmount = payment.getAmount().multiply(BigDecimal.valueOf(100)).toBigInteger().toString();
        if (!expectedAmount.equals(params.get("vnp_Amount"))) {
            return VnPayIpnResponse.of("04", "Invalid amount");
        }

        boolean vnpaySuccess = "00".equals(params.get("vnp_ResponseCode"));
        boolean updated = vnpaySuccess
                ? paymentService.markPaid(payment.getId(), params.get("vnp_TransactionNo"), Instant.now())
                : paymentService.markFailed(payment.getId());

        if (updated) {
            return VnPayIpnResponse.of("00", "Confirm Success");
        }

        // 0 dòng ảnh hưởng: hoặc IPN gọi lặp bình thường (đã PAID), hoặc IPN
        // thành công đến trễ sau khi đơn đã bị hủy (đã CANCELLED) — trường hợp
        // sau cần log để đối soát/hoàn tiền thủ công, không có mã VNPay riêng
        // cho tình huống này nên vẫn trả "02" để VNPay dừng gọi lại.
        PaymentStatus currentStatus = paymentService.getByIdOrThrow(payment.getId()).getStatus();
        if (currentStatus == PaymentStatus.CANCELLED && vnpaySuccess) {
            log.warn(
                    "IPN báo thanh toán thành công nhưng payment {} (order {}) đã bị hủy trước đó — "
                            + "amount={}, vnp_TransactionNo={}. Cần đối soát/hoàn tiền thủ công.",
                    payment.getId(), payment.getOrderId(), payment.getAmount(), params.get("vnp_TransactionNo"));
        }
        return VnPayIpnResponse.of("02", "Order already confirmed");
    }

    // Chỉ đọc để hiển thị cho khách — verify chữ ký nhưng KHÔNG đổi
    // Payment.status (nguồn xác nhận thật là /ipn).
    @GetMapping("/return")
    public ResponseEntity<ApiResponse<VnPayReturnResponse>> returnUrl(@RequestParam Map<String, String> params) {
        if (!vnPayService.verifySignature(params)) {
            return ResponseEntity.ok(ApiResponse.success("Thanh toán không hợp lệ",new VnPayReturnResponse(false, null, "Chữ ký không hợp lệ")));
        }

        Payment payment = paymentService.findByGatewayTxnRef(params.get("vnp_TxnRef")).orElse(null);
        if (payment == null) {
            return ResponseEntity
                    .ok(ApiResponse.success("Không tìm thấy giao dịch",new VnPayReturnResponse(false, null, "Không tìm thấy giao dịch")));
        }

        boolean success = "00".equals(params.get("vnp_ResponseCode"));
        String message = success ? "Thanh toán thành công" : "Thanh toán không thành công";
        return ResponseEntity.ok(ApiResponse.success("Thanh toán thành công",new VnPayReturnResponse(success, payment.getOrderId(), message)));
    }
}

