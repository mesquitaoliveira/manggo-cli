package tech.manggocli.core.domain.service;

import java.util.regex.Pattern;

/**
 * Pure case-conversion utilities for Java naming.
 * No I/O, no framework dependencies, no side effects.
 */
public final class JavaNames {

    private static final Pattern WORD_SPLITTER = Pattern.compile("[-_\\s]+");

    private JavaNames() {
    }

    public static String toCamelCase(final String input) {
        if (input == null || input.isBlank()) return "unknownMethod";
        final String[] tokens = WORD_SPLITTER.split(input);
        if (tokens.length == 0) return input;
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            final String t = tokens[i];
            if (t.isEmpty()) continue;
            if (i == 0) {
                sb.append(Character.toLowerCase(t.charAt(0))).append(t.substring(1));
            } else {
                sb.append(Character.toUpperCase(t.charAt(0))).append(t.substring(1));
            }
        }
        return sb.toString();
    }

    public static String toPascalCase(final String input) {
        if (input == null || input.isBlank()) return "Unknown";
        final String[] parts = WORD_SPLITTER.split(input);
        final StringBuilder sb = new StringBuilder();
        for (final String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }

    public static String capitalize(final String s) {
        if (s == null || s.isEmpty()) return "Unknown";
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
