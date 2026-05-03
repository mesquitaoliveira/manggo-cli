package tech.manggocli.infrastructure.codegen.mode.feign;

import tech.manggocli.core.application.port.UserNotifier;
import tech.manggocli.infrastructure.codegen.shared.TagCodeGenerator;
import tech.manggocli.core.domain.api.ClientSpec;
import tech.manggocli.core.domain.api.ParsedApi;

import java.io.IOException;
import java.nio.file.Path;

public class FeignClientGenerator extends AbstractFeignClientGenerator {

    @Override
    public String modeName() {
        return "feign";
    }

    @Override
    protected void generateProjectStructure(final String basePackage, final Path projectRoot) throws IOException {
        new FeignProjectPomGenerator(projectRoot).generate(basePackage);
    }

    @Override
    protected TagCodeGenerator createSmokeTestGenerator(
            final String basePackage, final Path projectRoot, final String clientName,
            final String authHeaderName, final UserNotifier notifier) {
        return new FeignSmokeTestGenerator(basePackage, projectRoot, clientName, authHeaderName, notifier);
    }

    @Override
    protected void generateClientConfiguration(
            final ParsedApi api,
            final ClientSpec spec,
            final String basePackage,
            final Path projectRoot,
            final UserNotifier notifier) throws IOException {

        new FeignClientConfigurationGenerator(basePackage, projectRoot).generate(api, spec);
        notifier.notifyFileGenerated(spec.getClientNamePascal() + "ClientConfiguration");
    }
}
