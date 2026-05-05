package tech.manggocli.infrastructure.codegen.shared;

import tech.manggocli.core.domain.api.operation.ApiOperation;
import tech.manggocli.core.domain.api.operation.ApiParameter;
import tech.manggocli.core.domain.api.schema.ApiSchema;
import tech.manggocli.core.domain.service.OperationTypeResolver;
import tech.manggocli.infrastructure.codegen.layout.PackageNames;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static tech.manggocli.core.domain.service.ImportResolver.IMPORT_LIST;
import static tech.manggocli.core.domain.service.ImportResolver.extractListItemType;
import static tech.manggocli.core.domain.service.ImportResolver.importFor;
import static tech.manggocli.core.domain.service.ImportResolver.isJavaPrimitive;

/**
 * Resolves and adds import statements for generated client code (request DTOs, response types).
 * Shared by rest clients, interfaces, and smoke test generators.
 * Thread-safety: Stateless (all mutable state lives in the caller-supplied Set)
 */
public final class ClientImportCollector {

    public static final String IMPORT_JAVA_UTIL_LIST = IMPORT_LIST;
    private final String basePackage;
    private final String clientName;
    private final Map<String, ApiSchema> schemas;

    public ClientImportCollector(final String basePackage, final String clientName, final Map<String, ApiSchema> schemas) {
        this.basePackage = basePackage;
        this.clientName = clientName;
        this.schemas = Objects.requireNonNullElse(schemas, Collections.emptyMap());
    }

    public void collectFromOperation(final ApiOperation op, final String tag, final Set<String> imports) {
        addRequestImport(OperationTypeResolver.resolveRequestType(tag, op), tag, imports);
        addResponseImports(OperationTypeResolver.resolveResponseType(op), tag, imports);
    }

    public void collectParameterImports(final ApiOperation op, final Set<String> imports) {
        if (!op.needsRequestObject()) {
            op.getPathParams().stream()
                    .map(ApiParameter::getJavaType).flatMap(t -> importFor(t).stream())
                    .forEach(imports::add);
            op.getQueryParams().stream()
                    .flatMap(p -> importsForType(p.getJavaType()))
                    .forEach(imports::add);
        }
        op.getHeaderParams().stream()
                .map(ApiParameter::getJavaType).flatMap(t -> importFor(t).stream())
                .forEach(imports::add);
    }

    public void addRequestImport(final String requestType, final String tag, final Set<String> imports) {
        ofNullable(requestType)
                .filter(t -> !isJavaPrimitive(t))
                .ifPresent(t -> imports.add(PackageNames.importDomainRequest(basePackage, clientName, tag, t)));
    }

    public void addResponseImports(final String responseType, final String tag, final Set<String> imports) {
        if (responseType == null) return;
        if (responseType.startsWith("List<")) {
            imports.add(IMPORT_JAVA_UTIL_LIST);
            final String inner = extractListItemType(responseType);
            if (inner != null) addSingleResponseImport(inner, tag, imports);
        } else {
            addSingleResponseImport(responseType, tag, imports);
        }
    }

    private void addSingleResponseImport(final String typeName, final String tag, final Set<String> imports) {
        if (isJavaPrimitive(typeName)) {
            importFor(typeName).ifPresent(imports::add);
        } else {
            final ApiSchema schema = schemas.get(typeName);
            final String imp = (schema != null && schema.isEnumType())
                    ? PackageNames.importDomainEnums(basePackage, clientName, tag, typeName)
                    : PackageNames.importDomainResponse(basePackage, clientName, tag, typeName);
            imports.add(imp);
        }
    }

    private static Stream<String> importsForType(final String javaType) {
        final Stream.Builder<String> b = Stream.builder();
        importFor(javaType).ifPresent(b);
        final String inner = extractListItemType(javaType);
        if (inner != null) importFor(inner).ifPresent(b);
        return b.build();
    }
}
