package tech.manggocli.infrastructure.parser.type;

import io.swagger.v3.oas.models.media.Schema;

import java.util.function.Function;

/**
 * Pattern: Strategy
 * Purpose: Resolves OAS object types to Java Object (no further type info available inline)
 * Thread-safety: Stateless
 */
public final class ObjectTypeResolver implements JavaTypeResolver {

    @Override
    public String resolve(final Schema<?> schema, final Function<Schema<?>, String> recursive) {
        return "Object";
    }
}
