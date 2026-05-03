package tech.manggocli.infrastructure.parser.type;

import io.swagger.v3.oas.models.media.Schema;

import java.util.function.Function;

/**
 * Pattern: Strategy
 * Purpose: Resolves OAS number types (Float for float format, Double otherwise)
 * Thread-safety: Stateless
 */
public final class NumberTypeResolver implements JavaTypeResolver {

    @Override
    public String resolve(final Schema<?> schema, final Function<Schema<?>, String> recursive) {
        return "float".equals(schema.getFormat()) ? "Float" : "Double";
    }
}
