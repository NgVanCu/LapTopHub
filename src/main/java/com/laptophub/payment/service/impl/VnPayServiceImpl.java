package com.laptophub.payment.service.impl;

import com.laptophub.payment.config.VnPayProperties;
import com.laptophub.payment.entity.Payment;
import com.laptophub.payment.service.VnPayService;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class VnPayServiceImpl implements VnPayService {
    private static final DateTimeFormatter VNPAY_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

    private final VnPayProperties properties;

    public VnPayServiceImpl(VnPayProperties properties) {
        this.properties = properties;
    }

    @Override
    public String buildPaymentUrl(Payment payment, String clientIp) {
        Instant now = Instant.now();
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", properties.tmnCode());
        params.put("vnp_Amount", payment.getAmount().multiply(BigDecimal.valueOf(100)).toBigInteger().toString());
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", payment.getGatewayTxnRef());
        params.put("vnp_OrderInfo", "Thanh toan don hang " + payment.getOrderId());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", properties.returnUrl());
        params.put("vnp_IpAddr", clientIp);
        params.put("vnp_CreateDate", VNPAY_DATE_FORMAT.format(now));
        params.put("vnp_ExpireDate", VNPAY_DATE_FORMAT.format(payment.getExpiresAt()));

        String query = buildEncodedQuery(params);
        String secureHash = sign(query);
        return properties.payUrl() + "?" + query + "&vnp_SecureHash=" + secureHash;
    }

    // allParams là toàn bộ query param nhận được từ VNPay (IPN hoặc return),
    // bao gồm cả vnp_SecureHash — hàm tự loại nó (và vnp_SecureHashType) ra
    // trước khi tính lại chữ ký để so khớp.
    @Override
    public boolean verifySignature(Map<String, String> allParams) {
        String receivedHash = allParams.get("vnp_SecureHash");
        if (receivedHash == null || receivedHash.isBlank()) {
            return false;
        }
        Map<String, String> filtered = new TreeMap<>(allParams);
        filtered.remove("vnp_SecureHash");
        filtered.remove("vnp_SecureHashType");
        String expected = sign(buildEncodedQuery(filtered));
        return expected.equalsIgnoreCase(receivedHash);
    }

    // sortedParams đã sort theo key (TreeMap) — hashData ký và query string
    // build ra giống hệt nhau (key=value đã URL-encode, nối bằng &), đúng
    // cách VNPay tự làm ở phía họ khi verify.
    private String buildEncodedQuery(Map<String, String> sortedParams) {
        return sortedParams.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                .map(entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    private String sign(String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            hmac.init(new SecretKeySpec(properties.hashSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] hashBytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hashBytes.length * 2);
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Không thể ký dữ liệu VNPay", e);
        }
    }
}
