package com.laptophub.payment.service;

import com.laptophub.payment.entity.Payment;

import java.util.Map;

public interface VnPayService {
    String buildPaymentUrl(Payment payment, String clientIp);

    boolean verifySignature(Map<String, String> allParams);


}
