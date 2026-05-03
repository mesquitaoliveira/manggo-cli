package tech.manggocli.infrastructure.codegen.mode.feign;

import tech.manggocli.core.application.port.UserNotifier;
import tech.manggocli.infrastructure.codegen.shared.ApiModelGenerator;
import tech.manggocli.infrastructure.codegen.shared.DtoGenerator;
import tech.manggocli.infrastructure.codegen.shared.RestInterfaceGenerator;
import tech.manggocli.infrastructure.codegen.shared.TagCodeGenerator;
import tech.manggocli.infrastructure.codegen.mode.AbstractClientGenerator;
import tech.manggocli.core.domain.api.ClientSpec;
import tech.manggocli.core.domain.api.ParsedApi;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public abstract class AbstractFeignClientGenerator extends AbstractClientGenerator {

    @Override
    protected final void generateSharedClasses(
            final String basePackage,
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
    protected final List<TagCodeGenerator> createTagGenerators(
            final String basePackage,
            final Path projectRoot,
            final ClientSpec spec,
            final ParsedApi api,
            final UserNotifier notifier) {

        return List.of(
                new DtoGenerator(basePackage, projectRoot, spec.getClientName(), notifier),
                new RestInterfaceGenerator(basePackage, projectRoot, spec.getClientName(), notifier),
                new FeignRestClientGenerator(basePackage, projectRoot, spec.getClientName(), notifier),
                createSmokeTestGenerator(basePackage, projectRoot, spec.getClientName(), api.getAuthHeaderName(), notifier)
        );
    }

    protected abstract TagCodeGenerator createSmokeTestGenerator(
            final String basePackage,
            final Path projectRoot,
            final String clientName,
            final String authHeaderName,
            final UserNotifier notifier);
}
