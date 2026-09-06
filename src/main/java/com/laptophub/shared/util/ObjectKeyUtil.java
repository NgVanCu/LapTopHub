package com.laptophub.shared.util;

import java.util.UUID;

public final class ObjectKeyUtil {
    private ObjectKeyUtil() {
    }

    public static String generateKey(String prefix, String filename) {

        String extension = extractExtension(filename);

        return prefix + "/"
                + UUID.randomUUID()
                + extension;
    }

    private static String extractExtension(String filename) {

        int dotIndex = filename.lastIndexOf('.');

        if (dotIndex < 0) {
            return "";
        }

        return filename.substring(dotIndex).toLowerCase();
    }
}
