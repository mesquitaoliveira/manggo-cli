package tech.manggocli.infrastructure.parser.schema;

import tech.manggocli.core.domain.api.ApiSchema;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static tech.manggocli.core.domain.service.NamingService.refToClassName;
import static tech.manggocli.core.domain.service.NamingService.toValidJavaClassName;

/**
 * Pattern: Single Responsibility
 * Purpose: Extracts and builds all ApiSchema entries from OpenAPI components/schemas.
 *          Uses a two-pass strategy: first build all schemas, then merge allOf references
 *          (allOf resolution requires all schemas to already be present in the map).
 * Thread-safety: Stateful — populates the shared schemas map
 */
public final class SchemaExtractor {

    private static final Logger log = LoggerFactory.getLogger(SchemaExtractor.class);

    private final Map<String, ApiSchema> schemas;
    private final SchemaBuilder builder;

    public SchemaExtractor(final Map<String, ApiSchema> schemas, final SchemaBuilder builder) {
        this.schemas = schemas;
        this.builder = builder;
    }

    public void extract(final OpenAPI openAPI) {
        if (openAPI.getComponents() == null || openAPI.getComponents().getSchemas() == null) return;

        buildAllSchemas(openAPI);
        mergeAllOf(openAPI);
        mergeComposedRefs(openAPI);
        resolveScalarAliasRefs();

        log.info("Schemas encontrados: {}", schemas.size());
    }

    // ─── Pass 1 ───────────────────────────────────────────────────────────────

    private void buildAllSchemas(final OpenAPI openAPI) {
        openAPI.getComponents().getSchemas().forEach((name, schema) -> {
            final String javaName = toValidJavaClassName(name);
            schemas.put(javaName, builder.buildApiSchema(javaName, schema));
        });
    }

    // ─── Pass 2: allOf merge ──────────────────────────────────────────────────

    private void mergeAllOf(final OpenAPI openAPI) {
        openAPI.getComponents().getSchemas().forEach((name, schema) -> {
            if (schema.getAllOf() == null) return;
            final String javaName = toValidJavaClassName(name);
            final ApiSchema apiSchema = schemas.get(javaName);
            if (apiSchema == null) return;

            final Set<String> extraRequired = collectAllOfRequired(schema.getAllOf());
            for (final Object sub : schema.getAllOf()) {
                if (sub instanceof Schema<?> subSchema) {
                    mergeAllOfSub(subSchema, javaName, extraRequired, apiSchema);
                }
            }
        });
    }

    private Set<String> collectAllOfRequired(final List<?> allOf) {
        return allOf.stream()
                .filter(sub -> sub instanceof Schema<?>)
                .map(sub -> (Schema<?>) sub)
                .filter(s -> s.getRequired() != null)
                .flatMap(s -> s.getRequired().stream())
                .collect(Collectors.toSet());
    }

    private void mergeAllOfSub(final Schema<?> sub, final String parentName,
                               final Set<String> extraRequired, final ApiSchema target) {
        if (sub.get$ref() != null) {
            mergeRefProperties(sub.get$ref(), target);
        } else if (sub.getProperties() != null) {
            mergeInlineProperties(sub, parentName, extraRequired, target);
        }
    }

    // ─── Pass 3: oneOf/anyOf ref merge ───────────────────────────────────────

    /**
     * For schemas whose oneOf/anyOf branches are all $refs (no inline object properties
     * were extracted in Pass 1), merges properties from each referenced schema.
     * Mirrors allOf merge policy: non-overwrite, all refs.
     * Only runs when the schema has no properties yet — avoids clobbering inline extractions.
     */
    private void mergeComposedRefs(final OpenAPI openAPI) {
        openAPI.getComponents().getSchemas().forEach((name, schema) -> {
            final String javaName = toValidJavaClassName(name);
            final ApiSchema apiSchema = schemas.get(javaName);
            if (apiSchema == null || !apiSchema.getProperties().isEmpty()) return;

            apiSchema.getOneOfTypes().forEach(ref -> mergeRefProperties(ref, apiSchema));
            apiSchema.getAnyOfTypes().forEach(ref -> mergeRefProperties(ref, apiSchema));
        });
    }

    // ─── Pass 4: scalar-alias $ref resolution ────────────────────────────────

    /**
     * Replaces property javaType values that point to scalar-type aliases (type:boolean/integer/
     * number/string with no enum) with the corresponding Java built-in. This prevents generated
     * DTOs from referencing classes that are intentionally not generated (see SchemaDependencyWalker).
     * Runs after all passes so resolution is order-independent.
     */
    private void resolveScalarAliasRefs() {
        schemas.forEach((ignored, apiSchema) ->
            apiSchema.getProperties().forEach((propName, prop) -> {
                final ApiSchema alias = schemas.get(prop.getJavaType());
                if (alias != null && !alias.isEnumType()
                        && isScalarType(alias.getType())
                        && alias.getProperties().isEmpty()) {
                    prop.setJavaType(scalarToJavaType(alias.getType()));
                }
            })
        );
    }

    private static boolean isScalarType(final String type) {
        return "boolean".equals(type) || "integer".equals(type)
            || "number".equals(type) || "string".equals(type);
    }

    private static String scalarToJavaType(final String oasType) {
        return switch (oasType) {
            case "boolean"  -> "Boolean";
            case "integer"  -> "Integer";
            case "number"   -> "Double";
            default         -> "String";
        };
    }

    private void mergeRefProperties(final String ref, final ApiSchema target) {
        final String refName = refToClassName(ref);
        final ApiSchema refSchema = schemas.get(refName);
        if (refSchema == null) return;
        refSchema.getProperties().forEach((propName, prop) -> {
            if (!target.getProperties().containsKey(propName))
                target.getProperties().put(propName, prop);
        });
    }

    private void mergeInlineProperties(final Schema<?> sub, final String parentName,
                                       final Set<String> extraRequired, final ApiSchema target) {
        final Set<String> subRequired = sub.getRequired() != null
            ? new HashSet<>(sub.getRequired()) : Collections.emptySet();
        subRequired.addAll(extraRequired);
        sub.getProperties().forEach((k, v) -> {
            if (!target.getProperties().containsKey(k))
                builder.buildProperty(k, v, parentName, subRequired, target);
        });
    }
}
