package com.pghpizza.api.common;

import java.util.Locale;

public final class TextSanitizer {
    private TextSanitizer() {
    }

    public static String trim(String value) {
        return value == null ? null : value.trim();
    }

    public static String normalizeEmail(String value) {
        return trim(value).toLowerCase(Locale.ROOT);
    }

    public static String emptyToNull(String value) {
        String trimmed = trim(value);
        return trimmed == null || trimmed.isEmpty() ? null : trimmed;
    }
}
