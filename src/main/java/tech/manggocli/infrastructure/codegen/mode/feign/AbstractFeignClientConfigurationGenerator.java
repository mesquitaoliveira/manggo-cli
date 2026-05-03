package tech.manggocli.infrastructure.codegen.mode.feign;

import tech.manggocli.infrastructure.codegen.layout.PackageNames;
import tech.manggocli.infrastructure.codegen.mode.AbstractClientConfigurationGenerator;
import tech.manggocli.core.domain.api.ClientSpec;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static tech.manggocli.core.domain.service.NamingService.toPascalCase;

/**
 * Shared base for Feign and FeignHc5 client configuration generators.
 * Both modes use identical imports (ApiKeyInterceptor, FeignLogger, Rest, RestClient)
 * and identical context (importAnnotation). Only the template name differs.
 */
public abstract class AbstractFeignClientConfigurationGenerator extends AbstractClientConfigurationGenerator {

    protected AbstractFeignClientConfigurationGenerator(final String basePackage, final Path projectRoot) {
        super(basePackage, projectRoot);
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
