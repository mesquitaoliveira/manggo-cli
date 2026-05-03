package tech.manggocli.infrastructure.codegen.mode.feignhc5;

import tech.manggocli.core.application.port.UserNotifier;
import tech.manggocli.infrastructure.codegen.shared.TagCodeGenerator;
import tech.manggocli.infrastructure.codegen.mode.feign.AbstractFeignClientGenerator;
import tech.manggocli.core.domain.api.ClientSpec;
import tech.manggocli.core.domain.api.ParsedApi;

import java.io.IOException;
import java.nio.file.Path;

public class FeignHc5ClientGenerator extends AbstractFeignClientGenerator {

    @Override
    public String modeName() {
        return "feign-hc5";
    }

    @Override
    protected void generateProjectStructure(final String basePackage, final Path projectRoot) throws IOException {
        new FeignHc5ProjectPomGenerator(projectRoot).generate(basePackage);
    }

    @Override
    protected TagCodeGenerator createSmokeTestGenerator(
            final String basePackage, final Path projectRoot, final String clientName,
            final String authHeaderName, final UserNotifier notifier) {
        return new FeignHc5SmokeTestGenerator(basePackage, projectRoot, clientName, authHeaderName, notifier);
    }

    @Override
    protected void generateClientConfiguration(
            final ParsedApi api,
            final ClientSpec spec,
            final String basePackage,
            final Path projectRoot,
            final UserNotifier notifier) throws IOException {

        new FeignHc5ClientConfigurationGenerator(basePackage, projectRoot).generate(api, spec);
        notifier.notifyFileGenerated(spec.getClientNamePascal() + "ClientConfiguration");
    }
}
