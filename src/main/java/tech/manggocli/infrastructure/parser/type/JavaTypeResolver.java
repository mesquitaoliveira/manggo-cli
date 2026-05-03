package tech.manggocli.infrastructure.parser.type;

import io.swagger.v3.oas.models.media.Schema;

import java.util.function.Function;

/**
 * Pattern: Strategy
 * Purpose: Resolves a Java type name from an OAS schema of a specific type string
 * Thread-safety: Stateless
 */
public interface JavaTypeResolver {
    String resolve(Schema<?> schema, Function<Schema<?>, String> recursive);
}
