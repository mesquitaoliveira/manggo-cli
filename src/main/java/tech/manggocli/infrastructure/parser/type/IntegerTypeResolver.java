package tech.manggocli.infrastructure.parser.type;

import io.swagger.v3.oas.models.media.Schema;

import java.util.function.Function;

/**
 * Pattern: Strategy
 * Purpose: Resolves OAS integer types (Long for int64 format, Integer otherwise)
 * Thread-safety: Stateless
 */
public final class IntegerTypeResolver implements JavaTypeResolver {

    @Override
    public String resolve(final Schema<?> schema, final Function<Schema<?>, String> recursive) {
        return "long".equals(schema.getFormat()) ? "Long" : "Integer";
    }
}
