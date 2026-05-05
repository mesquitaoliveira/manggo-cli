package tech.manggocli.core.domain.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Sanitizes arbitrary OpenAPI strings into valid Java identifiers.
 * Three concerns: class names, enum constants, and {@code $ref} → class.
 * <p>
 * The symbol-to-word table for enum constants is data, not code — adding a new
 * symbol is a one-line edit (OCP).
 */
public final class JavaIdentifiers {

    private static final Pattern NON_UPPERCASE_ALPHANUMERIC = Pattern.compile("[^A-Z0-9]+");
    private static final Pattern LEADING_TRAILING_UNDERSCORE = Pattern.compile("^_|_$");

    private static final Map<Character, String> SYMBOL_WORDS = new LinkedHashMap<>();

    static {
        SYMBOL_WORDS.put('*', "STAR");
        SYMBOL_WORDS.put('+', "PLUS");
        SYMBOL_WORDS.put('?', "QUESTION");
        SYMBOL_WORDS.put('!', "EXCLAMATION");
        SYMBOL_WORDS.put('@', "AT");
        SYMBOL_WORDS.put('#', "HASH");
        SYMBOL_WORDS.put('$', "DOLLAR");
        SYMBOL_WORDS.put('%', "PERCENT");
        SYMBOL_WORDS.put('^', "CARET");
        SYMBOL_WORDS.put('&', "AND");
        SYMBOL_WORDS.put('=', "EQUALS");
        SYMBOL_WORDS.put('|', "PIPE");
        SYMBOL_WORDS.put('~', "TILDE");
    }

    private JavaIdentifiers() {
    }

    /**
     * Converts an OpenAPI schema name to a valid Java class name.
     * Invalid chars are removed; the next letter is capitalized.
     * Ex: {@code "PolicyVerdictActionEnum-2"} → {@code "PolicyVerdictActionEnum2"}.
     */
    public static String toValidJavaClassName(final String name) {
        if (name == null || name.isBlank()) return "Unknown";
        final StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = false;
        for (final char c : name.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                sb.append(capitalizeNext && Character.isLetter(c) ? Character.toUpperCase(c) : c);
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
     * Sanitizes an OpenAPI enum value into a valid Java {@code SCREAMING_SNAKE_CASE} constant.
     * Symbols listed in {@link #SYMBOL_WORDS} are replaced with their word equivalents.
     */
    public static String sanitizeEnumConstant(final String val) {
        if (val == null || val.isBlank()) return "UNKNOWN";
        final String replaced = replaceSymbols(val);
        String result = LEADING_TRAILING_UNDERSCORE.matcher(
                NON_UPPERCASE_ALPHANUMERIC.matcher(replaced.toUpperCase()).replaceAll("_")
        ).replaceAll("");
        if (result.isEmpty()) result = "UNKNOWN";
        if (Character.isDigit(result.charAt(0))) result = "_" + result;
        return result;
    }

    /**
     * Resolves an OpenAPI {@code $ref} (e.g. {@code "#/components/schemas/Foo"}) to a Java class name.
     */
    public static String refToClassName(final String ref) {
        return toValidJavaClassName(ref.substring(ref.lastIndexOf('/') + 1));
    }

    private static String replaceSymbols(final String val) {
        final StringBuilder sb = new StringBuilder(val.length());
        for (final char c : val.toCharArray()) {
            final String word = SYMBOL_WORDS.get(c);
            sb.append(word != null ? word : c);
        }
        return sb.toString();
    }
}
