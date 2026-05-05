package tech.manggocli.infrastructure.codegen.mode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.manggocli.core.application.port.UserNotifier;
import tech.manggocli.core.domain.api.ParsedApi;
import tech.manggocli.core.domain.client.ClientSpec;
import tech.manggocli.infrastructure.codegen.layout.PackageNames;
import tech.manggocli.infrastructure.codegen.shared.TemplateRenderer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static tech.manggocli.core.domain.service.JavaNames.toPascalCase;
import static tech.manggocli.infrastructure.codegen.layout.MavenLayout.mainSourcePath;
import static tech.manggocli.infrastructure.codegen.layout.MavenLayout.writeJavaFile;

/**
 * Template Method for ClientConfiguration generation across modes.
 * Common: path/class names, bean list per tag, template render, notification.
 * Mode-specific hooks: template name, dynamic imports, extra context fields.
 */
public abstract class AbstractClientConfigurationGenerator {

    private static final Logger log = LoggerFactory.getLogger(AbstractClientConfigurationGenerator.class);

    protected final String basePackage;
    protected final Path projectRoot;
    protected final UserNotifier notifier;

    protected AbstractClientConfigurationGenerator(
            String basePackage,
            Path projectRoot,
            UserNotifier notifier
    ) {
        this.basePackage = basePackage;
        this.projectRoot = projectRoot;
        this.notifier = notifier;
    }

    public final void generate(final ParsedApi api, final ClientSpec spec) throws IOException {
        final String pascal = spec.getClientNamePascal();
        final String client = spec.getClientName();
        final String className = pascal + "ClientConfiguration";
        final String pkg = PackageNames.config(basePackage);
        final Path dir = mainSourcePath(projectRoot, pkg);

        final Map<String, Object> context = buildContext(api.getTags(), spec, pascal, client, pkg);

        writeJavaFile(dir, className, TemplateRenderer.render(getTemplateName(), context));

        log.debug("Generated: {}/{}.java", pkg, className);
        notifier.notifyFileGenerated(className);
    }

    protected abstract String getTemplateName();

    protected abstract Set<String> buildImports(List<String> tags, String client);

    protected abstract void addModeSpecificContext(
            Map<String, Object> context,
            ClientSpec spec,
            List<String> tags,
            String pascal
    );

    private Map<String, Object> buildContext(
            final List<String> tags,
            final ClientSpec spec,
            final String pascal,
            final String client,
            final String pkg
    ) {
        final String clientLower = client.toLowerCase();

        final Map<String, Object> ctx = new LinkedHashMap<>();
        ctx.put("package", pkg);
        ctx.put("imports", new ArrayList<>(buildImports(tags, client)));
        ctx.put("className", pascal + "ClientConfiguration");
        ctx.put("pascal", pascal);
        ctx.put("clientLower", clientLower);
        ctx.put("beans", buildBeans(tags, client, pascal));

        addModeSpecificContext(ctx, spec, tags, pascal);
        return ctx;
    }

    private static List<Map<String, Object>> buildBeans(
            final List<String> tags,
            final String client,
            final String pascal
    ) {
        final String clientLower = client.toLowerCase();
        return tags.stream().map(tag -> {
            final String tagPascal = toPascalCase(tag);
            final Map<String, Object> bean = new LinkedHashMap<>();
            bean.put("tagPascal", tagPascal);
            bean.put("beanName", clientLower + tagPascal + "Rest");
            bean.put("pascal", pascal);
            return bean;
        }).collect(toList());
    }
}
