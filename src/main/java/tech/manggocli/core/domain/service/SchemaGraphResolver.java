package tech.manggocli.core.domain.service;

import tech.manggocli.core.domain.api.schema.ApiSchema;
import tech.manggocli.core.domain.api.schema.ApiSchemaProperty;
import tech.manggocli.core.domain.api.schema.ArrayAliasNode;
import tech.manggocli.core.domain.api.schema.EnumNode;
import tech.manggocli.core.domain.api.schema.MissingNode;
import tech.manggocli.core.domain.api.schema.ObjectNode;
import tech.manggocli.core.domain.api.schema.SchemaNode;
import tech.manggocli.core.domain.enums.SchemaKind;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Resolves the transitive closure of a schema reference into an ordered list of {@link SchemaNode}s
 * (parent before children). Type classification is delegated to {@link SchemaKind#of};
 * this class owns only traversal — deduplication, recursion, accumulation.
 *
 * <p>The {@code visited} set is supplied by the caller so deduplication state can span
 * multiple {@link #collect} invocations (e.g. across tags within one generation run).
 */
public final class SchemaGraphResolver {

    private final Map<String, ApiSchema> schemas;
    private final Set<String> visited;

    public SchemaGraphResolver(
            final Map<String, ApiSchema> schemas,
            final Set<String> visited
    ) {
        this.schemas = schemas;
        this.visited = visited;
    }

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

        final ApiSchema schema = schemas.get(name);
        switch (SchemaKind.of(name, schema)) {
            case LIST_WRAPPER -> recurseInto(ImportResolver.extractListItemType(name), tag, isRequest, result);
            case PRIMITIVE,
                 SCALAR_ALIAS -> { /* skip */ }
            case ENUM -> result.add(new EnumNode(name, schema));
            case ARRAY_ALIAS -> emitArrayAlias(name, schema, tag, isRequest, result);
            case OBJECT -> emitObject(name, schema, tag, isRequest, result);
            case MISSING -> emitMissing(name, tag, isRequest, result);
        }
    }

    private void emitArrayAlias(
            final String name,
            final ApiSchema schema,
            final String tag,
            final boolean isRequest,
            final List<SchemaNode> result
    ) {
        if (!markVisited(name, tag, isRequest)) return;
        result.add(new ArrayAliasNode(name, schema, isRequest));
        recurseInto(schema.getItemsType(), tag, isRequest, result);
    }

    private void emitObject(
            final String name,
            final ApiSchema schema,
            final String tag,
            final boolean isRequest,
            final List<SchemaNode> result
    ) {
        if (!markVisited(name, tag, isRequest)) return;
        result.add(new ObjectNode(name, schema, isRequest));
        for (final ApiSchemaProperty prop : schema.getProperties().values()) {
            recurseInto(prop.getJavaType(), tag, isRequest, result);
            recurseInto(ImportResolver.extractListItemType(prop.getJavaType()), tag, isRequest, result);
        }
    }

    private void emitMissing(
            final String name,
            final String tag,
            final boolean isRequest,
            final List<SchemaNode> result
    ) {
        if (!markVisited(name, tag, isRequest)) return;
        result.add(new MissingNode(name, isRequest));
    }

    private void recurseInto(
            final String javaType,
            final String tag,
            final boolean isRequest,
            final List<SchemaNode> result
    ) {
        if (javaType == null || ImportResolver.isJavaPrimitive(javaType)) return;
        visit(javaType, tag, isRequest, result);
    }

    /**
     * Records the visit. Returns {@code false} if already seen (caller should stop).
     * Enums are intentionally not tracked here — {@code EnumDtoStrategy} owns enum dedup.
     */
    private boolean markVisited(final String name, final String tag, final boolean isRequest) {
        return visited.add(tag + ":" + (isRequest ? "req:" : "res:") + name);
    }
}
