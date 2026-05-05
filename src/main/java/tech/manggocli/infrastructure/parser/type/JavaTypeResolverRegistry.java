package tech.manggocli.infrastructure.parser.type;

import io.swagger.v3.oas.models.media.Schema;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Pattern: Strategy + Registry
 * Purpose: O(1) selection of Java type resolver by OAS type string
 * Thread-safety: Stateless (Map built in constructor, all resolvers are stateless)
 */
public final class JavaTypeResolverRegistry {

    private final Map<String, JavaTypeResolver> resolvers;
    private static final JavaTypeResolver FALLBACK = (schema, recursive) -> "String";

    public JavaTypeResolverRegistry() {
        final Map<String, JavaTypeResolver> map = new LinkedHashMap<>();
        map.put("string", new StringTypeResolver());
        map.put("integer", new IntegerTypeResolver());
        map.put("number", new NumberTypeResolver());
        map.put("boolean", new BooleanTypeResolver());
        map.put("array", new ArrayTypeResolver());
        map.put("object", new ObjectTypeResolver());
        this.resolvers = Collections.unmodifiableMap(map);
    }

    public String resolve(final String type, final Schema<?> schema, final Function<Schema<?>, String> recursive) {
        return resolvers.getOrDefault(type, FALLBACK).resolve(schema, recursive);
    }
}
