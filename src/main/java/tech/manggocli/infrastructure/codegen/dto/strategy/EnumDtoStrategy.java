package tech.manggocli.infrastructure.codegen.dto.strategy;

import tech.manggocli.infrastructure.codegen.layout.PackageNames;
import tech.manggocli.infrastructure.codegen.layout.Templates;
import tech.manggocli.infrastructure.codegen.shared.TemplateRenderer;
import tech.manggocli.infrastructure.codegen.dto.DtoGenerationContext;
import tech.manggocli.infrastructure.codegen.dto.DtoGenerationStrategy;
import tech.manggocli.core.domain.api.schema.ApiSchema;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static tech.manggocli.infrastructure.codegen.layout.MavenLayout.mainSourcePath;
import static tech.manggocli.core.domain.service.JavaIdentifiers.sanitizeEnumConstant;
import static tech.manggocli.infrastructure.codegen.layout.MavenLayout.writeJavaFile;

public class EnumDtoStrategy implements DtoGenerationStrategy {

    @Override
    public void generate(final DtoGenerationContext ctx, final String dtoName) throws IOException {
        final var key = ctx.tag() + ":" + dtoName;
        if (ctx.generated().contains(key)) return;
        ctx.generated().add(key);

        final ApiSchema schema = ctx.schemas().get(dtoName);
        if (schema == null || !schema.isEnumType()) return;

        final String pkg = PackageNames.domainEnums(ctx.basePackage(), ctx.clientName(), ctx.tag());
        final Path dir = mainSourcePath(ctx.projectRoot(), pkg);

        final List<Map<String, Object>> constants = new ArrayList<>();
        final Set<String> seen = new HashSet<>();
        final List<String> values = schema.getEnumValues();

        for (int i = 0; i < values.size(); i++) {
            final String val = values.get(i);
            final String constant = sanitizeEnumConstant(val);
            String unique = constant;
            int suffix = 2;
            while (seen.contains(unique)) unique = constant + "_" + suffix++;
            seen.add(unique);

            final Map<String, Object> c = new LinkedHashMap<>();
            c.put("name", unique);
            c.put("value", val);
            c.put("last", i == values.size() - 1);
            constants.add(c);
        }

        final Map<String, Object> templateCtx = new LinkedHashMap<>();
        templateCtx.put("package", pkg);
        templateCtx.put("enumName", dtoName);
        templateCtx.put("constants", constants);

        writeJavaFile(dir, dtoName, TemplateRenderer.render(Templates.ENUM_DTO, templateCtx));
        ctx.notifier().notifyFileGenerated(dtoName);
    }
}
