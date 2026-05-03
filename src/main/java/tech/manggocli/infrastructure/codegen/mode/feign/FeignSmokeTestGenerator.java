package tech.manggocli.infrastructure.codegen.mode.feign;

import tech.manggocli.core.application.port.UserNotifier;
import tech.manggocli.infrastructure.codegen.layout.Templates;

import java.nio.file.Path;

public class FeignSmokeTestGenerator extends AbstractFeignSmokeTestGenerator {

    public FeignSmokeTestGenerator(final String basePackage, final Path projectRoot,
                                    final String clientName, final String authHeaderName,
                                    final UserNotifier notifier) {
        super(basePackage, projectRoot, clientName, authHeaderName, notifier);
    }

    @Override
    protected String templateName() {
        return Templates.FEIGN_SMOKE_TEST;
    }
}
