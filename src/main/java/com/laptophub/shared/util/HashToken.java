package com.laptophub.shared.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class HashToken {
    private static final String ALGORITHM = "SHA-256";

    private HashToken() {}

    public static String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 luôn có sẵn trong JCA provider mặc định của mọi JVM
            // chuẩn — nhánh này về lý thuyết không bao giờ chạy.
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
