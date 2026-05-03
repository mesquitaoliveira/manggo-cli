package tech.manggocli.infrastructure.parser.schema;

import tech.manggocli.core.domain.api.ApiSchema;
import tech.manggocli.core.domain.api.ApiSchemaProperty;
import tech.manggocli.infrastructure.parser.property.AnyOfPropertyHandler;
import tech.manggocli.infrastructure.parser.property.EnumPropertyHandler;
import tech.manggocli.infrastructure.parser.property.FallbackPropertyHandler;
import tech.manggocli.infrastructure.parser.property.InlineObjectPropertyHandler;
import tech.manggocli.infrastructure.parser.property.OneOfPropertyHandler;
import tech.manggocli.infrastructure.parser.property.PropertyResolutionContext;
import tech.manggocli.infrastructure.parser.property.PropertyTypeHandler;
import tech.manggocli.infrastructure.parser.type.JavaTypeResolverRegistry;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static tech.manggocli.core.domain.service.NamingService.refToClassName;
import static tech.manggocli.core.domain.service.NamingService.toValidJavaClassName;

/**
 * Pattern: Single Responsibility
 * Purpose: Constructs ApiSchema and ApiSchemaProperty domain objects from raw OAS schemas.
 *          Delegates type resolution to JavaTypeResolverRegistry (Strategy)
 *          and property type detection to PropertyTypeHandler chain (Chain of Responsibility).
 * Thread-safety: Stateful — holds reference to shared schemas map
 */
public final class SchemaBuilder {

    private final JavaTypeResolverRegistry typeRegistry = new JavaTypeResolverRegistry();

    private final PropertyTypeHandler propertyChain = PropertyTypeHandler.link(
        new EnumPropertyHandler(),
        new InlineObjectPropertyHandler(),
        new OneOfPropertyHandler(),
        new AnyOfPropertyHandler(),
        new FallbackPropertyHandler()
    );

    private final Map<String, ApiSchema> schemas;

    public SchemaBuilder(final Map<String, ApiSchema> schemas) {
        this.schemas = schemas;
    }

    public ApiSchema buildApiSchema(final String name, final Schema<?> schema) {
        final ApiSchema apiSchema = new ApiSchema();
        apiSchema.setName(name);
        apiSchema.setType(schema.getType());

        final Set<String> requiredFields = schema.getRequired() != null
            ? new HashSet<>(schema.getRequired()) : Collections.emptySet();

        if (schema.getProperties() != null)
            schema.getProperties().forEach((k, v) ->
                buildProperty(k, v, name, requiredFields, apiSchema));

        if (schema instanceof ArraySchema arraySchema) {
            Schema<?> items = arraySchema.getItems();
            if (items != null) apiSchema.setItemsType(resolveJavaType(items));
        }

        if (schema.getEnum() != null && !schema.getEnum().isEmpty()) {
            apiSchema.setEnumType(true);
            apiSchema.setEnumValues(schema.getEnum().stream()
                    .filter(Objects::nonNull).map(Object::toString).collect(Collectors.toList()));
        }

        if (schema.getOneOf() != null && !schema.getOneOf().isEmpty())
            extractComposedSchemaProperties(schema.getOneOf(), name, apiSchema, apiSchema.getOneOfTypes());

        if (schema.getAnyOf() != null && !schema.getAnyOf().isEmpty())
            extractComposedSchemaProperties(schema.getAnyOf(), name, apiSchema, apiSchema.getAnyOfTypes());

        if (schema.getDiscriminator() != null)
            apiSchema.setDiscriminatorProperty(schema.getDiscriminator().getPropertyName());

        return apiSchema;
    }

    public void buildProperty(final String propName, final Schema<?> propSchema, final String parentName,
                              final Set<String> requiredFields, final ApiSchema apiSchema) {
        final ApiSchemaProperty prop = new ApiSchemaProperty();
        prop.setName(propName);

        final PropertyResolutionContext ctx = new PropertyResolutionContext(
            propName,
            propSchema,
            parentName,
            this::registerSchemaIfAbsent,
            this::resolveJavaType
        );
        prop.setJavaType(propertyChain.resolve(ctx));

        prop.setRequired(requiredFields.contains(propName));
        prop.setDescription(propSchema.getDescription());
        prop.setFormat(propSchema.getFormat());
        apiSchema.getProperties().put(propName, prop);
    }

    public String resolveJavaType(final Schema<?> schema) {
        if (schema == null) return "Object";

        if (schema.get$ref() != null) {
            return refToClassName(schema.get$ref());
        }
        if (schema.getName() != null && !isPrimitiveType(schema.getName())) {
            return toValidJavaClassName(schema.getName());
        }

        String type = schema.getType();
        if (type == null && schema.getTypes() != null) {
            type = schema.getTypes().stream()
                .filter(t -> t != null && !"null".equals(t))
                .findFirst()
                .orElse(null);
        }
        if (type == null) return "Object";

        return typeRegistry.resolve(type, schema, this::resolveJavaType);
    }

    void registerSchemaIfAbsent(final String name, final Schema<?> rawSchema) {
        if (!schemas.containsKey(name))
            schemas.put(name, buildApiSchema(name, rawSchema));
    }

    /**
     * Processes all branches of a oneOf/anyOf schema:
     * - $ref branches → name stored in typeNames for metadata
     * - inline object branches → properties merged into target (non-overwrite, all branches)
     */
    private void extractComposedSchemaProperties(final List<?> branches, final String name,
                                                  final ApiSchema target, final List<String> typeNames) {
        for (final Object branch : branches) {
            if (!(branch instanceof Schema<?> sub)) continue;
            if (sub.get$ref() != null) {
                typeNames.add(refToClassName(sub.get$ref()));
                continue;
            }
            if (!"object".equals(sub.getType())) continue;
            if (sub.getProperties() == null || sub.getProperties().isEmpty()) continue;
            final Set<String> branchRequired = sub.getRequired() != null
                ? new HashSet<>(sub.getRequired()) : Collections.emptySet();
            sub.getProperties().forEach((k, v) -> {
                if (!target.getProperties().containsKey(k))
                    buildProperty(k, v, name, branchRequired, target);
            });
        }
    }

    private boolean isPrimitiveType(final String name) {
        return Set.of("string", "integer", "number", "boolean", "array", "object").contains(name);
    }
}
