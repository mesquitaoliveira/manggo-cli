package tech.manggocli.infrastructure.codegen.dto.strategy;

import tech.manggocli.infrastructure.codegen.layout.PackageNames;
import tech.manggocli.infrastructure.codegen.layout.Templates;
import tech.manggocli.infrastructure.codegen.shared.TemplateRenderer;
import tech.manggocli.infrastructure.codegen.dto.DtoGenerationContext;
import tech.manggocli.infrastructure.codegen.dto.DtoGenerationStrategy;
import tech.manggocli.infrastructure.codegen.dto.builder.DtoImportsCollector;
import tech.manggocli.infrastructure.codegen.dto.builder.FieldMapper;
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

/**
 * Writes a single OBJECT or MISSING schema node produced by SchemaDependencyWalker.
 * Package (request vs response) comes from ctx.isRequest(), set by the orchestrator
 * based on the walker node's isRequest flag.
 */
public class RegularDtoStrategy implements DtoGenerationStrategy {

    @Override
    public void generate(final DtoGenerationContext ctx, final String dtoName) throws IOException {
        final String pkg = ctx.isRequest()
                ? PackageNames.domainRequest(ctx.basePackage(), ctx.clientName(), ctx.tag())
                : PackageNames.domainResponse(ctx.basePackage(), ctx.clientName(), ctx.tag());
        final Path dir = mainSourcePath(ctx.projectRoot(), pkg);
        final String apiModelImport = PackageNames.common(ctx.basePackage()) + ".ApiModel";
        final ApiSchema schema = ctx.schemas().get(dtoName);

        if (schema == null) {
            generateStub(pkg, dtoName, apiModelImport, dir, ctx);
        } else if (schema.getProperties().isEmpty()) {
            generateEmpty(pkg, dtoName, apiModelImport, dir, ctx);
        } else {
            generateRegular(pkg, dtoName, apiModelImport, dir, ctx, schema);
        }
    }

    private static void generateStub(final String pkg, final String dtoName, final String apiModelImport,
                                     final Path dir, final DtoGenerationContext ctx) throws IOException {
        writeJavaFile(dir, dtoName,
                TemplateRenderer.render(Templates.STUB_DTO,
                        Map.of("package", pkg, "className", dtoName, "apiModelImport", apiModelImport)));
        ctx.notifier().notifyError("Schema not found in components/schemas: " + dtoName);
    }

    private static void generateEmpty(final String pkg, final String dtoName, final String apiModelImport,
                                      final Path dir, final DtoGenerationContext ctx) throws IOException {
        writeJavaFile(dir, dtoName,
                TemplateRenderer.render(Templates.EMPTY_DTO,
                        Map.of("package", pkg, "className", dtoName, "apiModelImport", apiModelImport)));
        ctx.notifier().notifyFileGenerated(dtoName);
    }

    private static void generateRegular(final String pkg, final String dtoName, final String apiModelImport,
                                        final Path dir, final DtoGenerationContext ctx,
                                        final ApiSchema schema) throws IOException {
        final DtoImportsCollector imports = new DtoImportsCollector(ctx.basePackage(), ctx.clientName(), ctx.schemas());
        schema.getProperties().values().forEach(p -> imports.collectFromProperty(p, ctx.tag()));

        final List<Map<String, Object>> fields = schema.getProperties().values().stream()
                .map(FieldMapper::mapProperty).collect(Collectors.toList());

        final Map<String, Object> templateCtx = new LinkedHashMap<>();
        templateCtx.put("package", pkg);
        templateCtx.put("imports", new ArrayList<>(imports.getImports()));
        templateCtx.put("className", dtoName);
        templateCtx.put("fields", fields);
        templateCtx.put("apiModelImport", apiModelImport);

        writeJavaFile(dir, dtoName, TemplateRenderer.render(Templates.DTO, templateCtx));
        ctx.notifier().notifyFileGenerated(dtoName);
    }
}
