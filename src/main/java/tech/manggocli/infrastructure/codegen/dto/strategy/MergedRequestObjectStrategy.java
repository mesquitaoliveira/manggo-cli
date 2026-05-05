package tech.manggocli.infrastructure.codegen.dto.strategy;

import tech.manggocli.core.domain.api.operation.ApiOperation;
import tech.manggocli.core.domain.api.operation.ApiParameter;
import tech.manggocli.core.domain.api.schema.ApiSchema;
import tech.manggocli.infrastructure.codegen.dto.DtoGenerationContext;
import tech.manggocli.infrastructure.codegen.dto.DtoGenerationStrategy;
import tech.manggocli.infrastructure.codegen.dto.builder.DtoImportsCollector;
import tech.manggocli.infrastructure.codegen.layout.PackageNames;
import tech.manggocli.infrastructure.codegen.layout.Templates;
import tech.manggocli.infrastructure.codegen.shared.TemplateRenderer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static tech.manggocli.core.domain.service.JavaNames.capitalize;
import static tech.manggocli.infrastructure.codegen.layout.MavenLayout.mainSourcePath;
import static tech.manggocli.infrastructure.codegen.layout.MavenLayout.writeJavaFile;

/**
 * Generates a consolidated RequestObject that merges path params + query params + body fields
 * into one class, avoiding multiple @Param args that break Feign compilation.
 * <p>
 * Header params are intentionally excluded — they remain as @Param method arguments.
 */
public class MergedRequestObjectStrategy implements DtoGenerationStrategy {

    private final EnumDtoStrategy enumStrategy = new EnumDtoStrategy();

    @Override
    public void generate(final DtoGenerationContext ctx, final String dtoName) throws IOException {
        if (ctx.generated().contains(dtoName)) return;
        ctx.generated().add(dtoName);

        final ApiOperation op = ctx.operation();
        final String pkg = PackageNames.domainRequest(ctx.basePackage(), ctx.clientName(), ctx.tag());
        final Path dir = mainSourcePath(ctx.projectRoot(), pkg);
        final ApiSchema body = op.getRequestBodySchema() != null ? ctx.schemas().get(op.getRequestBodySchema()) : null;

        // Enum deps for path and query params
        for (final ApiParameter p : Stream.concat(op.getPathParams().stream(), op.getQueryParams().stream()).toList())
            generateEnumIfNeeded(ctx, p.getJavaType());

        // Imports — headers excluded (they are @Param method args, not object fields)
        final DtoImportsCollector imports = new DtoImportsCollector(ctx.basePackage(), ctx.clientName(), ctx.schemas());
        op.getPathParams().forEach(p -> imports.collectFromParameter(p, ctx.tag()));
        op.getQueryParams().forEach(p -> imports.collectFromParameter(p, ctx.tag()));

        if (body != null) body.getProperties()
                .values()
                .forEach(p -> imports.collectFromProperty(p, ctx.tag()));

        final Set<String> paramFields = Stream
                .concat(op.getPathParams().stream(), op.getQueryParams().stream())
                .map(ApiParameter::getCamelCaseName)
                .collect(Collectors.toSet());

        final List<ApiParameter> required = Stream
                .concat(op.getPathParams().stream(), op.getQueryParams().stream())
                .filter(ApiParameter::isRequired)
                .collect(Collectors.toList());

        // Accessors: all path+query params, then non-duplicate body fields
        final List<Map<String, Object>> accessors = Stream
                .concat(op.getPathParams().stream(), op.getQueryParams().stream())
                .map(this::accessorFor)
                .collect(Collectors.toCollection(ArrayList::new));
        if (body != null) {
            body.getProperties().values().stream()
                    .filter(prop -> !paramFields.contains(prop.getCamelCaseName()))
                    .map(prop -> Map.<String, Object>of(
                            "javaType", prop.getJavaType(),
                            "fieldName", prop.getCamelCaseName(),
                            "capitalizedName", prop.getCapitalizedName()))
                    .forEach(accessors::add);
        }

        final Map<String, Object> templateCtx = buildTemplateContext(
                ctx, pkg, dtoName, op, body, imports, paramFields, required, accessors);

        writeJavaFile(dir, dtoName, TemplateRenderer.render(Templates.REQUEST_OBJECT, templateCtx));
        ctx.notifier().notifyFileGenerated(dtoName);
    }

    private static Map<String, Object> buildTemplateContext(
            final DtoGenerationContext ctx, final String pkg, final String dtoName,
            final ApiOperation op, final ApiSchema body, final DtoImportsCollector imports,
            final Set<String> paramFields, final List<ApiParameter> required,
            final List<Map<String, Object>> accessors) {
        final List<Map<String, Object>> bodyFields = getMaps(body, paramFields);

        final Map<String, Object> m = new LinkedHashMap<>();
        m.put("package", pkg);
        m.put("className", dtoName);
        m.put("imports", new ArrayList<>(imports.getImports()));
        m.put("pathParams", toParamFields(op.getPathParams()));
        m.put("hasPathParams", !op.getPathParams().isEmpty());
        m.put("headerParams", Collections.emptyList());
        m.put("hasHeaderParams", false);
        m.put("queryParams", toParamFields(op.getQueryParams()));
        m.put("hasQueryParams", !op.getQueryParams().isEmpty());
        m.put("bodyFields", bodyFields);
        m.put("hasBodyFields", !bodyFields.isEmpty());
        m.put("hasRequired", !required.isEmpty());
        m.put("requiredSignature", required.stream()
                .map(p -> p.getJavaType() + " " + p.getCamelCaseName()).collect(Collectors.joining(", ")));
        m.put("requiredParams", required.stream()
                .map(p -> Map.of("fieldName", p.getCamelCaseName())).collect(Collectors.toList()));
        m.put("accessors", accessors);
        m.put("apiModelImport", PackageNames.common(ctx.basePackage()) + ".ApiModel");
        return m;
    }

    private static List<Map<String, Object>> getMaps(final ApiSchema body, final Set<String> paramFields) {
        if (body == null) return List.of();
        return body.getProperties().values().stream()
                .filter(prop -> !paramFields.contains(prop.getCamelCaseName()))
                .map(prop -> {
                    final Map<String, Object> f = new LinkedHashMap<>();
                    f.put("name", prop.getName());
                    f.put("javaType", prop.getJavaType());
                    f.put("fieldName", prop.getCamelCaseName());
                    f.put("capitalizedName", prop.getCapitalizedName());
                    f.put("description", prop.getDescription());
                    return f;
                })
                .collect(Collectors.toList());
    }

    private void generateEnumIfNeeded(final DtoGenerationContext ctx, final String javaType) throws IOException {
        final ApiSchema s = ctx.schemas().get(javaType);
        if (s != null && s.isEnumType()) enumStrategy.generate(ctx, javaType);
    }

    private Map<String, Object> accessorFor(final ApiParameter p) {
        return Map.of("javaType", p.getJavaType(), "fieldName", p.getCamelCaseName(),
                "capitalizedName", capitalize(p.getCamelCaseName()));
    }

    private static List<Map<String, Object>> toParamFields(final List<ApiParameter> params) {
        return params.stream().map(p -> {
            final Map<String, Object> m = new LinkedHashMap<>();
            m.put("javaType", p.getJavaType());
            m.put("fieldName", p.getCamelCaseName());
            return m;
        }).collect(Collectors.toList());
    }
}
