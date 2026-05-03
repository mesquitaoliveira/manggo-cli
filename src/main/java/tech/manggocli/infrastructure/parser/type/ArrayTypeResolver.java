package tech.manggocli.infrastructure.parser.type;

import io.swagger.v3.oas.models.media.Schema;

import java.util.function.Function;

/**
 * Pattern: Strategy
 * Purpose: Resolves OAS array types to Java List<T>, using recursive resolution for item type
 * Thread-safety: Stateless
 */
public final class ArrayTypeResolver implements JavaTypeResolver {

    @Override
    public String resolve(final Schema<?> schema, final Function<Schema<?>, String> recursive) {
        final Schema<?> items = schema.getItems();
        return items != null ? "List<" + recursive.apply(items) + ">" : "List<Object>";
    }
}
