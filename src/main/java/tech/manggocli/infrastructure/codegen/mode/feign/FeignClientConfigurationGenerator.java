package tech.manggocli.infrastructure.codegen.mode.feign;

import tech.manggocli.core.application.port.UserNotifier;
import tech.manggocli.core.domain.client.ClientSpec;
import tech.manggocli.infrastructure.codegen.layout.PackageNames;
import tech.manggocli.infrastructure.codegen.mode.AbstractClientConfigurationGenerator;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static tech.manggocli.core.domain.service.JavaNames.toPascalCase;

/**
 * Client configuration generator shared by Feign and FeignHc5 modes.
 * Both modes use identical imports (ApiKeyInterceptor, FeignLogger, Rest, RestClient)
 * and context (importAnnotation). The only variation is the template, injected at construction.
 */
public class FeignClientConfigurationGenerator extends AbstractClientConfigurationGenerator {

    private final String templateName;

    public FeignClientConfigurationGenerator(final String basePackage,
                                             final Path projectRoot,
                                             final UserNotifier notifier,
                                             final String templateName) {
        super(basePackage, projectRoot, notifier);
        this.templateName = templateName;
    }

    @Override
    protected String getTemplateName() {
        return templateName;
    }

    @Override
    protected Set<String> buildImports(final List<String> tags, final String client) {
        final String pascal = toPascalCase(client);
        final Set<String> imports = new LinkedHashSet<>();
        imports.add(PackageNames.importApiKeyInterceptor(basePackage, client, pascal));
        imports.add(PackageNames.importFeignLogger(basePackage, client, pascal));
        imports.add(PackageNames.importFeignClientConfig(basePackage));
        tags.forEach(tag -> {
            final String tagPascal = toPascalCase(tag);
            imports.add(PackageNames.importRest(basePackage, client, tag, tagPascal));
            imports.add(PackageNames.importRestClient(basePackage, client, tag, tagPascal));
        });
        return imports;
    }

    @Override
    protected void addModeSpecificContext(final Map<String, Object> context, final ClientSpec spec,
                                          final List<String> tags, final String pascal) {
        context.put("importAnnotation", "{" + pascal + "FeignLogger.class}");
    }
}
