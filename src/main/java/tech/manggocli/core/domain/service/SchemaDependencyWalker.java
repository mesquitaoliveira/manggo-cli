package tech.manggocli.core.domain.service;

import tech.manggocli.core.domain.api.ApiSchema;
import tech.manggocli.core.domain.api.ApiSchemaProperty;
import tech.manggocli.core.domain.api.SchemaNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Domain service: walks the schema dependency graph, collecting all reachable schemas
 * in dependency order (parent before children).
 *
 * Decouples graph traversal from file I/O — DtoGenerator handles writing;
 * this class handles the "what needs to be generated" question.
 *
 * Deduplication state is shared with the caller via the {@code visited} set,
 * allowing cross-tag deduplication to be owned by the orchestrating generator.
 */
public final class SchemaDependencyWalker {

    private final Map<String, ApiSchema> schemas;
    private final Set<String> visited;

    public SchemaDependencyWalker(
            final Map<String, ApiSchema> schemas,
            final Set<String> visited
    ) {
        this.schemas = schemas;
        this.visited = visited;
    }

    /**
     * Collects all schemas reachable from {@code rootName}, including transitive dependencies.
     * List<X> types are unwrapped; the item type is visited instead.
     * Array-alias schemas are represented as a {@link SchemaNode} with {@link SchemaNode#isArrayAlias()} == true.
     */
    public List<SchemaNode> collect(
            final String rootName,
            final String tag,
            final boolean isRequest
    ) {
        final List<SchemaNode> result = new ArrayList<>();
        visit(rootName, tag, isRequest, result);
        return result;
    }

    private void visit(
            final String name,
            final String tag,
            final boolean isRequest,
            final List<SchemaNode> result
    ) {
        if (name == null) return;

        if (name.startsWith("List<")) {
            final String inner = ImportResolver.extractListItemType(name);
            final String ref   = toRef(inner);
            if (ref != null) visit(ref, tag, isRequest, result);
            return;
        }

        if (ImportResolver.isJavaPrimitive(name)) return;

        final ApiSchema schema = schemas.get(name);

        // Skip scalar-type aliases with no properties: type:boolean/integer/number/string schemas
        // that are not enums map to a Java built-in — generating a class for them is wrong.
        if (schema != null && !schema.isEnumType()
                && isPrimitiveScalarType(schema.getType())
                && schema.getProperties().isEmpty()) {
            return;
        }

        // Enums: do NOT track in visited — generateEnum() owns enum deduplication.
        // Tracking here would mark the key before generateEnum() runs, causing it to skip writing.
        if (schema != null && schema.isEnumType()) {
            result.add(SchemaNode.enumNode(name, schema));
            return;
        }

        final String key = tag + ":" + (isRequest ? "req:" : "res:") + name;
        if (!visited.add(key)) return;

        if (schema != null && "array".equals(schema.getType())) {
            final String itemsType = schema.getItemsType();
            final String ref       = toRef(itemsType);
            result.add(SchemaNode.arrayAlias(name, schema, isRequest));
            if (ref != null) visit(ref, tag, isRequest, result);
            return;
        }

        if (schema != null) {
            result.add(SchemaNode.objectNode(name, schema, isRequest));
            for (final ApiSchemaProperty prop : schema.getProperties().values()) {
                final String ref = toRef(prop.getJavaType());
                if (ref != null) visit(ref, tag, isRequest, result);
                final String inner = ImportResolver.extractListItemType(prop.getJavaType());
                if (inner != null) { final String r = toRef(inner); if (r != null) visit(r, tag, isRequest, result); }
            }
        } else {
            result.add(SchemaNode.missing(name, isRequest));
        }
    }

    private static String toRef(final String javaType) {
        return ImportResolver.isJavaPrimitive(javaType) ? null : javaType;
    }

    private static boolean isPrimitiveScalarType(final String type) {
        return "boolean".equals(type) || "integer".equals(type)
            || "number".equals(type) || "string".equals(type);
    }
}
