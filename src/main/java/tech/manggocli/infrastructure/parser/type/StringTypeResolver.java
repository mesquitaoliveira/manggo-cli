package tech.manggocli.infrastructure.parser.type;

import io.swagger.v3.oas.models.media.Schema;

import java.util.function.Function;

/**
 * Pattern: Strategy
 * Purpose: Resolves OAS string types to Java types, honoring format (date, uuid, binary...)
 * Thread-safety: Stateless
 */
public final class StringTypeResolver implements JavaTypeResolver {

    @Override
    public String resolve(final Schema<?> schema, final Function<Schema<?>, String> recursive) {
        final String format = schema.getFormat();
        if (format == null) return "String";
        return switch (format) {
            case "date" -> "LocalDate";
            case "date-time" -> "LocalDateTime";
            case "uuid" -> "UUID";
            case "binary" -> "byte[]";
            default -> "String";
        };
    }
}
