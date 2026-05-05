package tech.manggocli.infrastructure.codegen.mode.feign;

import tech.manggocli.core.application.port.UserNotifier;
import tech.manggocli.infrastructure.codegen.shared.ApiModelGenerator;
import tech.manggocli.infrastructure.codegen.shared.DtoGenerator;
import tech.manggocli.infrastructure.codegen.shared.RestInterfaceGenerator;
import tech.manggocli.infrastructure.codegen.shared.TagCodeGenerator;
import tech.manggocli.infrastructure.codegen.mode.AbstractClientGenerator;
import tech.manggocli.core.domain.client.ClientSpec;
import tech.manggocli.core.domain.api.ParsedApi;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Shared scaffold for Feign-family modes. Subclasses provide:
 *   • modeName + project structure (POM template),
 *   • smoke-test template name,
 *   • client-configuration template name.
 * Tag generators and shared classes are identical across Feign variants.
 */
public abstract class AbstractFeignClientGenerator extends AbstractClientGenerator {

    private final String smokeTestTemplate;
    private final String clientConfigTemplate;

    protected AbstractFeignClientGenerator(final String smokeTestTemplate,
                                           final String clientConfigTemplate) {
        this.smokeTestTemplate = smokeTestTemplate;
        this.clientConfigTemplate = clientConfigTemplate;
    }

    @Override
    protected final void generateSharedClasses(final String basePackage,
                                               final Path projectRoot,
                                               final ClientSpec spec,
                                               final UserNotifier notifier) throws IOException {

        new FeignConfigBeanGenerator(basePackage, projectRoot).generate();
        new ApiModelGenerator(basePackage, projectRoot).generate();
        new FeignApiKeyInterceptorGenerator(basePackage, projectRoot).generate(spec);
        notifier.notifyFileGenerated(spec.getClientNamePascal() + "ApiKeyInterceptor");
        new FeignLoggerGenerator(basePackage, projectRoot).generate(spec);
        notifier.notifyFileGenerated(spec.getClientNamePascal() + "FeignLogger");
    }

    @Override
    protected final List<TagCodeGenerator> createTagGenerators(final String basePackage,
                                                               final Path projectRoot,
                                                               final ClientSpec spec,
                                                               final ParsedApi api,
                                                               final UserNotifier notifier) {
        return List.of(
                new DtoGenerator(basePackage, projectRoot, spec.getClientName(), notifier),
                new RestInterfaceGenerator(basePackage, projectRoot, spec.getClientName(), notifier),
                new FeignRestClientGenerator(basePackage, projectRoot, spec.getClientName(), notifier),
                new FeignSmokeTestGenerator(basePackage, projectRoot, spec.getClientName(),
                        api.getAuthHeaderName(), notifier, smokeTestTemplate)
        );
    }

    @Override
    protected final void generateClientConfiguration(final ParsedApi api,
                                                     final ClientSpec spec,
                                                     final String basePackage,
                                                     final Path projectRoot,
                                                     final UserNotifier notifier) throws IOException {
        new FeignClientConfigurationGenerator(basePackage, projectRoot, notifier, clientConfigTemplate)
                .generate(api, spec);
    }
}
