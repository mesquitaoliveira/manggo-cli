package tech.manggocli.infrastructure.codegen.mode.httpclient;

import tech.manggocli.core.application.port.UserNotifier;
import tech.manggocli.infrastructure.codegen.layout.PackageNames;
import tech.manggocli.infrastructure.codegen.layout.Templates;
import tech.manggocli.infrastructure.codegen.mode.AbstractClientConfigurationGenerator;
import tech.manggocli.core.domain.api.ClientSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.*;

import static tech.manggocli.core.domain.service.NamingService.toPascalCase;

/**
 * ClientConfiguration generator for Native mode (Java HTTP Client).
 *
 * Mode-specific hooks:
 * - Template: native/client-configuration.java.mustache
 * - Imports: Rest and RestClient only (no Interceptor/Logger)
 * - Context: adds "commonPackage" instead of "importAnnotation"
 * - Notification: via UserNotifier
 */
public class NativeClientConfigurationGenerator extends AbstractClientConfigurationGenerator {

    private static final Logger log = LoggerFactory.getLogger(NativeClientConfigurationGenerator.class);

    private final UserNotifier notifier;

    public NativeClientConfigurationGenerator(final String basePackage, final Path projectRoot, final UserNotifier notifier) {
        super(basePackage, projectRoot);
        this.notifier = notifier;
    }

    @Override
    protected String getTemplateName() {
        return Templates.NATIVE_CLIENT_CONFIGURATION;
    }

    @Override
    protected Set<String> buildImports(final List<String> tags, final String client) {
        final Set<String> imports = new LinkedHashSet<>();
        for (final String tag : tags) {
            final String tagPascal = toPascalCase(tag);
            imports.add(PackageNames.importRest(basePackage, client, tag, tagPascal));
            imports.add(PackageNames.importRestClient(basePackage, client, tag, tagPascal));
        }
        return imports;
    }

    @Override
    protected void addModeSpecificContext(
            final Map<String, Object> context,
            final ClientSpec spec,
            final List<String> tags,
            final String pascal) {
        context.put("commonPackage", PackageNames.common(basePackage));
        log.debug("Native configuration context built for client: {}", spec.getClientName());
    }

    @Override
    protected void notifyFileGenerated(final String className) {
        notifier.notifyFileGenerated(className);
    }
}
