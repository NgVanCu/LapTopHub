package com.laptophub.payment.dto.response;

public record VnPayIpnResponse(String RspCode, String Message) {

    public static VnPayIpnResponse of(String rspCode, String message) {
        return new VnPayIpnResponse(rspCode, message);
    }
}
