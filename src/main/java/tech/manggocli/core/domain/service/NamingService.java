package tech.manggocli.core.domain.service;

import java.util.regex.Pattern;

/**
 * Domain service: Java naming rules for code generation.
 * Pure functions — no I/O, no framework dependencies.
 */
public final class NamingService {

    private static final Pattern WORD_SPLITTER               = Pattern.compile("[-_\\s]+");
    private static final Pattern NON_ALPHANUMERIC            = Pattern.compile("[^a-zA-Z0-9]+");
    private static final Pattern NON_UPPERCASE_ALPHANUMERIC  = Pattern.compile("[^A-Z0-9]+");
    private static final Pattern LEADING_TRAILING_UNDERSCORE = Pattern.compile("^_|_$");

    private NamingService() {}

    public static String toCamelCase(final String input) {
        if (input == null || input.isBlank()) return "unknownMethod";
        final String[] tokens = WORD_SPLITTER.split(input);
        if (tokens.length == 0) return input;
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            final String t = tokens[i];
            if (t.isEmpty()) continue;
            if (i == 0) {
                sb.append(Character.toLowerCase(t.charAt(0)));
                sb.append(t.substring(1));
            } else {
                sb.append(Character.toUpperCase(t.charAt(0)));
                sb.append(t.substring(1));
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
                sb.append(Character.toUpperCase(part.charAt(0)));
                sb.append(part.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }

    /**
     * Converts an OpenAPI schema name to a valid Java class name.
     * Invalid chars are removed; next letter is capitalized.
     * Ex: "PolicyVerdictActionEnum-2" → "PolicyVerdictActionEnum2"
     */
    public static String toValidJavaClassName(final String name) {
        if (name == null || name.isBlank()) return "Unknown";
        final StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = false;
        for (char c : name.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                sb.append(capitalizeNext && Character.isLetter(c)
                    ? Character.toUpperCase(c) : c);
                capitalizeNext = false;
            } else {
                capitalizeNext = true;
            }
        }
        String result = sb.toString();
        if (result.isEmpty()) return "Unknown";
        result = Character.toUpperCase(result.charAt(0)) + result.substring(1);
        if (Character.isDigit(result.charAt(0))) result = "S" + result;
        return result;
    }

    /**
     * Sanitizes an OpenAPI enum value to a valid Java SCREAMING_SNAKE_CASE constant.
     * Special chars are replaced with word equivalents (* → STAR, + → PLUS, etc.).
     */
    public static String sanitizeEnumConstant(final String val) {
        if (val == null || val.isBlank()) return "UNKNOWN";
        final String s = val
            .replace("*", "STAR").replace("+", "PLUS").replace("?", "QUESTION")
            .replace("!", "EXCLAMATION").replace("@", "AT").replace("#", "HASH")
            .replace("$", "DOLLAR").replace("%", "PERCENT").replace("^", "CARET")
            .replace("&", "AND").replace("=", "EQUALS").replace("|", "PIPE")
            .replace("~", "TILDE");
        String result = LEADING_TRAILING_UNDERSCORE.matcher(
            NON_UPPERCASE_ALPHANUMERIC.matcher(s.toUpperCase()).replaceAll("_")
        ).replaceAll("");
        if (result.isEmpty()) result = "UNKNOWN";
        if (Character.isDigit(result.charAt(0))) result = "_" + result;
        return result;
    }

    public static String capitalize(final String s) {
        if (s == null || s.isEmpty()) return "Unknown";
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static String refToClassName(final String ref) {
        return toValidJavaClassName(ref.substring(ref.lastIndexOf('/') + 1));
    }
}
