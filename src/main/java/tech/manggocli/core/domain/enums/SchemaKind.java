package tech.manggocli.core.domain.enums;

import tech.manggocli.core.domain.api.schema.ApiSchema;
import tech.manggocli.core.domain.service.ImportResolver;

/**
 * Structural category of a schema reference encountered during graph traversal.
 * Smart enum: owns its classification logic via {@link #of(String, ApiSchema)},
 * keeping the rule and its outcomes in a single, type-safe place.
 */
public enum SchemaKind {

    /**
     * {@code List<X>} wrapper type — caller should unwrap and recurse on inner type.
     */
    LIST_WRAPPER,
    /**
     * Java built-in (String, Integer, ...) — skip.
     */
    PRIMITIVE,
    /**
     * Schema whose only role is aliasing a scalar JSON type — skip.
     */
    SCALAR_ALIAS,
    /**
     * Schema with {@code enum:} values — emit EnumNode.
     */
    ENUM,
    /**
     * Schema with {@code type: array} at top level — emit ArrayAliasNode.
     */
    ARRAY_ALIAS,
    /**
     * Object schema with properties — emit ObjectNode and recurse on properties.
     */
    OBJECT,
    /**
     * Reference to a schema not present in the spec — emit MissingNode (stub).
     */
    MISSING;

    /**
     * Classifies a schema reference by name and (possibly null) resolved schema.
     * Pure function: no I/O, no traversal, no node construction.
     */
    public static SchemaKind of(final String name, final ApiSchema schema) {
        if (name != null && name.startsWith(ImportResolver.PREFIX)) return LIST_WRAPPER;
        if (ImportResolver.isJavaPrimitive(name)) return PRIMITIVE;
        if (schema == null) return MISSING;
        if (schema.isEnumType()) return ENUM;
        if (isScalarAlias(schema)) return SCALAR_ALIAS;
        if ("array".equals(schema.getType())) return ARRAY_ALIAS;
        return OBJECT;
    }

    private static boolean isScalarAlias(final ApiSchema schema) {
        return !schema.isEnumType()
                && schema.getProperties().isEmpty()
                && isPrimitiveScalarType(schema.getType());
    }

    private static boolean isPrimitiveScalarType(final String type) {
        return "boolean".equals(type) || "integer".equals(type)
                || "number".equals(type) || "string".equals(type);
    }
}
