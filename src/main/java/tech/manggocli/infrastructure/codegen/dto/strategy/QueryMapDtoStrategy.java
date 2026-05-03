package tech.manggocli.infrastructure.codegen.dto.strategy;

import tech.manggocli.infrastructure.codegen.layout.PackageNames;
import tech.manggocli.infrastructure.codegen.layout.Templates;
import tech.manggocli.infrastructure.codegen.shared.TemplateRenderer;
import tech.manggocli.infrastructure.codegen.dto.DtoGenerationContext;
import tech.manggocli.infrastructure.codegen.dto.DtoGenerationStrategy;
import tech.manggocli.infrastructure.codegen.dto.builder.DtoImportsCollector;
import tech.manggocli.infrastructure.codegen.dto.builder.FieldMapper;
import tech.manggocli.core.domain.api.ApiParameter;
import tech.manggocli.core.domain.api.ApiSchema;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static tech.manggocli.infrastructure.codegen.layout.MavenLayout.mainSourcePath;
import static tech.manggocli.infrastructure.codegen.layout.MavenLayout.writeJavaFile;

public class QueryMapDtoStrategy implements DtoGenerationStrategy {

    private final EnumDtoStrategy enumStrategy = new EnumDtoStrategy();

    @Override
    public void generate(final DtoGenerationContext ctx, final String dtoName) throws IOException {
        if (ctx.generated().contains(dtoName)) return;
        ctx.generated().add(dtoName);

        // Generate enum deps for query params typed as enums
        for (final ApiParameter p : ctx.operation().getQueryParams()) {
            final ApiSchema s = ctx.schemas().get(p.getJavaType());
            if (s != null && s.isEnumType()) enumStrategy.generate(ctx, p.getJavaType());
        }

        final String pkg = PackageNames.domainRequest(ctx.basePackage(), ctx.clientName(), ctx.tag());
        final Path dir = mainSourcePath(ctx.projectRoot(), pkg);

        final DtoImportsCollector imports = new DtoImportsCollector(ctx.basePackage(), ctx.clientName(), ctx.schemas());
        ctx.operation().getQueryParams().forEach(p -> imports.collectFromParameter(p, ctx.tag()));

        final List<ApiParameter> queryParams = ctx.operation().getQueryParams();
        final List<Map<String, Object>> fields = queryParams.stream()
                .map(FieldMapper::mapParameter).collect(Collectors.toList());
        final String constructorParams = queryParams.stream()
                .map(p -> p.getJavaType() + " " + p.getCamelCaseName())
                .collect(Collectors.joining(", "));

        final Map<String, Object> templateCtx = new LinkedHashMap<>();
        templateCtx.put("package", pkg);
        templateCtx.put("className", dtoName);
        templateCtx.put("imports", new ArrayList<>(imports.getImports()));
        templateCtx.put("fields", fields);
        templateCtx.put("constructorParams", constructorParams);
        templateCtx.put("apiModelImport", PackageNames.common(ctx.basePackage()) + ".ApiModel");

        writeJavaFile(dir, dtoName, TemplateRenderer.render(Templates.QUERY_MAP_DTO, templateCtx));
        ctx.notifier().notifyFileGenerated(dtoName);
    }
}
