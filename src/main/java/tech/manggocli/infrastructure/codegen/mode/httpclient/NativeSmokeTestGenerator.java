package tech.manggocli.infrastructure.codegen.mode.httpclient;

import tech.manggocli.core.application.port.UserNotifier;
import tech.manggocli.infrastructure.codegen.layout.Templates;
import tech.manggocli.infrastructure.codegen.mode.AbstractSmokeTestGenerator;

import java.nio.file.Path;

public class NativeSmokeTestGenerator extends AbstractSmokeTestGenerator {

    public NativeSmokeTestGenerator(final String basePackage, final Path projectRoot,
                                    final String clientName, final UserNotifier notifier) {
        super(basePackage, projectRoot, clientName, notifier);
    }

    @Override
    protected String templateName() {
        return Templates.NATIVE_SMOKE_TEST;
    }
}
