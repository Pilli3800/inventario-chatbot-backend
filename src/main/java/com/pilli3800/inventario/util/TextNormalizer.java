package com.pilli3800.inventario.util;

public final class TextNormalizer {

    private TextNormalizer() {}

    public static String normalize(String text) {
        if (text == null) return "";
        return text
                .toLowerCase()
                .replaceAll("[^a-záéíóúñ ]", "")
                .trim();
    }

    public static String firstWord(String text) {
        String normalized = normalize(text);
        String[] parts = normalized.split("\\s+");
        return parts.length > 0 ? parts[0] : "";
    }

    public static String normalizeCode(String text) {
        if (text == null) return "";
        return text
                .replaceAll("\\s+", "")
                .toUpperCase(java.util.Locale.ROOT);
    }
}
