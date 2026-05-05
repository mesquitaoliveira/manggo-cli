package tech.manggocli.infrastructure.codegen.mode.nativeclient;

import tech.manggocli.core.application.port.UserNotifier;
import tech.manggocli.infrastructure.codegen.layout.PackageNames;
import tech.manggocli.infrastructure.codegen.layout.Templates;
import tech.manggocli.infrastructure.codegen.mode.AbstractClientConfigurationGenerator;
import tech.manggocli.core.domain.client.ClientSpec;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static tech.manggocli.core.domain.service.JavaNames.toPascalCase;

/**
 * ClientConfiguration generator for Native mode.
 * Mode-specific: Rest+RestClient imports only (no Interceptor/Logger),
 * adds "commonPackage" to context.
 */
public class NativeClientConfigurationGenerator extends AbstractClientConfigurationGenerator {

    public NativeClientConfigurationGenerator(final String basePackage, final Path projectRoot,
                                              final UserNotifier notifier) {
        super(basePackage, projectRoot, notifier);
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
    protected void addModeSpecificContext(final Map<String, Object> context,
                                          final ClientSpec spec,
                                          final List<String> tags,
                                          final String pascal) {
        context.put("commonPackage", PackageNames.common(basePackage));
    }
}
