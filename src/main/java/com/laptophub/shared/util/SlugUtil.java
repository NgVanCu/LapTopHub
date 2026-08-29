package com.laptophub.shared.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class SlugUtil {
    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");
    private static final Pattern EDGE_HYPHENS = Pattern.compile("^-+|-+$");

    private SlugUtil() {
    }

    public static String generate(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("input must not be null or blank");
        }

        String withoutD = input.replace('đ', 'd').replace('Đ', 'D');
        String normalized = Normalizer.normalize(withoutD, Normalizer.Form.NFD);
        String withoutDiacritics = DIACRITICS.matcher(normalized).replaceAll("");
        String lower = withoutDiacritics.toLowerCase(Locale.ROOT);
        String hyphenated = NON_ALPHANUMERIC.matcher(lower).replaceAll("-");
        return EDGE_HYPHENS.matcher(hyphenated).replaceAll("");
    }
}
