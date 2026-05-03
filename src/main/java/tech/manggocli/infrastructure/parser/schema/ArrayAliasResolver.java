package tech.manggocli.infrastructure.parser.schema;

import tech.manggocli.core.domain.api.ApiSchema;
import tech.manggocli.core.domain.api.ApiSchemaProperty;

import java.util.Map;

import static tech.manggocli.core.domain.service.ImportResolver.extractListItemType;

/**
 * Pattern: Single Responsibility
 * Purpose: Resolves array-alias schemas (type=array with no properties) to List<ItemType>.
 *          Example: AuditLogsData (array alias) → List<AuditLogData>
 * Thread-safety: Stateful — holds reference to shared schemas map
 */
public final class ArrayAliasResolver {

    private final Map<String, ApiSchema> schemas;

    public ArrayAliasResolver(final Map<String, ApiSchema> schemas) {
        this.schemas = schemas;
    }

    /**
     * Post-processes all schema properties, replacing array-alias type references
     * with their concrete List<ItemType> form.
     */
    public void resolveInProperties() {
        for (final ApiSchema schema : schemas.values()) {
            for (final ApiSchemaProperty prop : schema.getProperties().values()) {
                final String resolved = resolveAlias(prop.getJavaType());
                if (!resolved.equals(prop.getJavaType())) {
                    prop.setJavaType(resolved);
                }
            }
        }
    }

    /**
     * Returns "List<ItemType>" if typeName refers to an array-alias schema,
     * otherwise returns typeName unchanged.
     */
    public String resolveAlias(final String typeName) {
        if (typeName == null) return null;
        if (typeName.startsWith("List<")) {
            final String inner = extractListItemType(typeName);
            if (inner == null) return typeName;
            final String resolved = resolveAlias(inner);
            return resolved.equals(inner) ? typeName : "List<" + resolved + ">";
        }
        final ApiSchema ref = schemas.get(typeName);
        if (ref != null && "array".equals(ref.getType()) && ref.getItemsType() != null) {
            return "List<" + ref.getItemsType() + ">";
        }
        return typeName;
    }
}
