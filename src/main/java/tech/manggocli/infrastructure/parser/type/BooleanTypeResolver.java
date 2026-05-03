package tech.manggocli.infrastructure.parser.type;

import io.swagger.v3.oas.models.media.Schema;

import java.util.function.Function;

/**
 * Pattern: Strategy
 * Purpose: Resolves OAS boolean to Java Boolean
 * Thread-safety: Stateless
 */
public final class BooleanTypeResolver implements JavaTypeResolver {

    @Override
    public String resolve(final Schema<?> schema, final Function<Schema<?>, String> recursive) {
        return "Boolean";
    }
}
