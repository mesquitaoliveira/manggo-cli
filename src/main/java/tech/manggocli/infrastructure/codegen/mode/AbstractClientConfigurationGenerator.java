package tech.manggocli.infrastructure.codegen.mode;

import tech.manggocli.infrastructure.codegen.layout.PackageNames;
import tech.manggocli.infrastructure.codegen.shared.TemplateRenderer;
import tech.manggocli.core.domain.api.ClientSpec;
import tech.manggocli.core.domain.api.ParsedApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static tech.manggocli.infrastructure.codegen.layout.MavenLayout.mainSourcePath;
import static tech.manggocli.infrastructure.codegen.layout.MavenLayout.writeJavaFile;
import static tech.manggocli.core.domain.service.NamingService.toPascalCase;

/**
 * Template Method for ClientConfiguration generation across different modes.
 * Common logic (identical for all modes):
 * - Path and class name construction
 * - Bean list construction per tag
 * - Template rendering
 * Mode-specific hooks:
 * - Template name
 * - Dynamic imports
 * - Additional context fields
 */
public abstract class AbstractClientConfigurationGenerator {

    private static final Logger log = LoggerFactory.getLogger(AbstractClientConfigurationGenerator.class);

    protected final String basePackage;
    protected final Path projectRoot;

    public AbstractClientConfigurationGenerator(
            String basePackage,
            Path projectRoot
    ) {
        this.basePackage = basePackage;
        this.projectRoot = projectRoot;
    }

    /**
     * Template Method: orchestrates client configuration generation.
     */
    public final void generate(final ParsedApi api, final ClientSpec spec) throws IOException {
        final String pascal = spec.getClientNamePascal();
        final String client = spec.getClientName();
        final String className = pascal + "ClientConfiguration";
        final String pkg = PackageNames.config(basePackage);
        final Path dir = mainSourcePath(projectRoot, pkg);

        final Map<String, Object> context = buildContext(api.getTags(), spec, pascal, client, pkg);

        writeJavaFile(dir, className, TemplateRenderer.render(
                getTemplateName(),
                context
        ));

        log.debug("Generated: {}/{}.java", pkg, className);
        notifyFileGenerated(className);
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // ── ABSTRACT HOOKS - Each mode implements as needed
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * Returns the Mustache template path for this mode.
     */
    protected abstract String getTemplateName();

    /**
     * Builds mode-specific dynamic imports.
     * Feign/FeignHc5: ApiKeyInterceptor, FeignLogger, Rest, RestClient.
     * Native: Rest, RestClient only.
     */
    protected abstract Set<String> buildImports(List<String> tags, String client);

    /**
     * Adds mode-specific fields to the template context.
     * Feign/FeignHc5: adds "importAnnotation".
     * Native: adds "commonPackage".
     */
    protected abstract void addModeSpecificContext(
            Map<String, Object> context,
            ClientSpec spec,
            List<String> tags,
            String pascal);

    /**
     * Notifies about the generated file. No-op by default; override in Native.
     */
    protected void notifyFileGenerated(final String className) {
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // ── COMMON LOGIC - Implemented once for all modes
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    private Map<String, Object> buildContext(
            final List<String> tags,
            final ClientSpec spec,
            final String pascal,
            final String client,
            final String pkg
    ) {
        final String clientLower = client.toLowerCase();

        final Set<String> imports = buildImports(tags, client);
        final List<Map<String, Object>> beans = buildBeans(tags, client, pascal);

        final Map<String, Object> ctx = new LinkedHashMap<>();
        ctx.put("package", pkg);
        ctx.put("imports", new ArrayList<>(imports));
        ctx.put("className", pascal + "ClientConfiguration");
        ctx.put("pascal", pascal);
        ctx.put("clientLower", clientLower);
        ctx.put("beans", beans);

        addModeSpecificContext(ctx, spec, tags, pascal);

        return ctx;
    }

    /**
     * Builds the bean list per tag. Identical across all modes.
     */
    private List<Map<String, Object>> buildBeans(
            final List<String> tags,
            final String client,
            final String pascal
    ) {
        final String clientLower = client.toLowerCase();

        return tags.stream()
                .map(tag -> {
                    String tagPascal = toPascalCase(tag);
                    Map<String, Object> bean = new LinkedHashMap<>();
                    bean.put("tagPascal", tagPascal);
                    bean.put("beanName", clientLower + tagPascal + "Rest");
                    bean.put("pascal", pascal);
                    return bean;
                })
                .collect(Collectors.toList());
    }
}
