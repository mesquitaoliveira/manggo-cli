package tech.manggocli.infrastructure.parser.property;

import io.swagger.v3.oas.models.media.Schema;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Context object threaded through the property type resolution chain.
 * Carries all inputs needed by any handler without coupling handlers to OpenApiParser.
 * Thread-safety: Immutable record
 */
public record PropertyResolutionContext(
    String propName,
    Schema<?> propSchema,
    String parentName,
    BiConsumer<String, Schema<?>> registerSchema,
    Function<Schema<?>, String> resolveJavaType
) {}
