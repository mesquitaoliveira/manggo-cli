package tech.manggocli.core.domain.service;

import java.util.Optional;
import java.util.Set;

/**
 * Domain service: resolves Java import statements for generated code.
 * Knows which types require explicit imports and how to unwrap List<X>.
 */
public final class ImportResolver {

    public static final String IMPORT_LIST = "import java.util.List;";
    public static final String IMPORT_LOCAL_DATE = "import java.time.LocalDate;";
    public static final String IMPORT_LOCAL_DATE_TIME = "import java.time.LocalDateTime;";
    public static final String IMPORT_UUID = "import java.util.UUID;";
    public static final String IMPORT_BIG_DECIMAL = "import java.math.BigDecimal;";

    private static final Set<String> PRIMITIVES = Set.of(
            "String", "Integer", "Long", "Double", "Float", "Boolean",
            "Object", "LocalDate", "LocalDateTime", "UUID", "BigDecimal",
            "byte[]", "void", "Void"
    );

    public static final String PREFIX = "List<";

    private ImportResolver() {
    }

    public static Optional<String> importFor(final String javaType) {
        if (javaType == null) return Optional.empty();
        if (javaType.startsWith(PREFIX)) return Optional.of(IMPORT_LIST);
        return Optional.ofNullable(switch (javaType) {
            case "LocalDate" -> IMPORT_LOCAL_DATE;
            case "LocalDateTime" -> IMPORT_LOCAL_DATE_TIME;
            case "UUID" -> IMPORT_UUID;
            case "BigDecimal" -> IMPORT_BIG_DECIMAL;
            default -> null;
        });
    }

    public static String extractListItemType(final String javaType) {
        return (javaType != null && javaType.startsWith(PREFIX))
                ? javaType.substring(5, javaType.length() - 1) : null;
    }

    /**
     * Returns true for Java built-in types that don't need a cross-package import
     * from the generated domain packages.
     */
    public static boolean isJavaPrimitive(final String type) {
        if (type == null) return true;
        return PRIMITIVES.contains(type) || type.startsWith(PREFIX);
    }
}
