package tech.manggocli.infrastructure.codegen.mode.feignhc5;

import tech.manggocli.core.application.port.UserNotifier;
import tech.manggocli.infrastructure.codegen.layout.Templates;
import tech.manggocli.infrastructure.codegen.mode.feign.AbstractFeignSmokeTestGenerator;

import java.nio.file.Path;

public class FeignHc5SmokeTestGenerator extends AbstractFeignSmokeTestGenerator {

    public FeignHc5SmokeTestGenerator(final String basePackage, final Path projectRoot,
                                       final String clientName, final String authHeaderName,
                                       final UserNotifier notifier) {
        super(basePackage, projectRoot, clientName, authHeaderName, notifier);
    }

    @Override
    protected String templateName() {
        return Templates.FEIGN_HC5_SMOKE_TEST;
    }
}
