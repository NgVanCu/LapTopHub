package com.laptophub.shared.util;

import java.security.SecureRandom;
import java.util.Base64;

public final class EmailVerificationTokenGenerator {

    private static final int TOKEN_BYTE_LENGTH = 32;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private EmailVerificationTokenGenerator() {
    }

    public static String generate() {
        byte[] randomBytes = new byte[TOKEN_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
