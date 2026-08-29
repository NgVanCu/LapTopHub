package com.laptophub.shared.util;

import java.security.SecureRandom;
import java.util.Base64;

public final class EmailVerificationTokenGenerator {

    // 256 bit = 32 byte, đúng tối thiểu yêu cầu.
    private static final int TOKEN_BYTE_LENGTH = 32;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private EmailVerificationTokenGenerator() {
    }

    // URL-safe Base64, không padding: token có thể nằm trong query string
    // hoặc JSON body mà không cần percent-encode ký tự '=', '+', '/'.
    public static String generate() {
        byte[] randomBytes = new byte[TOKEN_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
