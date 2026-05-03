package tech.manggocli.infrastructure.codegen.dto.builder;

import tech.manggocli.infrastructure.codegen.layout.PackageNames;
import tech.manggocli.core.domain.api.ApiParameter;
import tech.manggocli.core.domain.api.ApiSchema;
import tech.manggocli.core.domain.api.ApiSchemaProperty;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static tech.manggocli.core.domain.service.ImportResolver.extractListItemType;
import static tech.manggocli.core.domain.service.ImportResolver.importFor;

/**
 * Collects imports required for DTOs being generated.
 * Handles standard Java types (UUID, List, etc.), project enums, and custom types.
 * Deduplicates automatically via Set.
 */
public class DtoImportsCollector {

    private final String basePackage;
    private final String clientName;
    private final Map<String, ApiSchema> schemas;
    private final Set<String> imports = new LinkedHashSet<>();

    public DtoImportsCollector(final String basePackage, final String clientName, final Map<String, ApiSchema> schemas) {
        this.basePackage = basePackage;
        this.clientName = clientName;
        this.schemas = schemas;
    }

    /**
     * Collects imports for a parameter (query, path, header).
     */
    public void collectFromParameter(final ApiParameter param, final String tag) {
        if (param == null) return;
        collectFromType(param.getJavaType(), tag);
    }

    /**
     * Collects imports for a schema property.
     */
    public void collectFromProperty(final ApiSchemaProperty prop, final String tag) {
        if (prop == null) return;
        collectFromType(prop.getJavaType(), tag);
    }

    /**
     * Collects imports for a Java type (primitive, List&lt;T&gt;, or custom).
     */
    private void collectFromType(final String javaType, final String tag) {
        if (javaType == null) return;
        importFor(javaType).or(() -> resolveEnumImport(javaType, tag)).ifPresent(imports::add);
        final String innerType = extractListItemType(javaType);
        if (innerType != null)
            importFor(innerType).or(() -> resolveEnumImport(innerType, tag)).ifPresent(imports::add);
    }

    private Optional<String> resolveEnumImport(final String javaType, final String tag) {
        final ApiSchema schema = schemas.get(javaType);
        if (schema != null && schema.isEnumType()) {
            return Optional.of(PackageNames.importDomainEnums(basePackage, clientName, tag, javaType));
        }
        return Optional.empty();
    }

    /**
     * Returns all collected imports.
     */
    public Set<String> getImports() {
        return new LinkedHashSet<>(imports);
    }
}
