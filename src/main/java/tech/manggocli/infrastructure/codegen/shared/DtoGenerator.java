package tech.manggocli.infrastructure.codegen.shared;

import tech.manggocli.core.application.port.UserNotifier;
import tech.manggocli.infrastructure.codegen.dto.DtoOrchestrator;
import tech.manggocli.core.domain.api.operation.ApiOperation;
import tech.manggocli.core.domain.api.schema.ApiSchema;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class DtoGenerator implements TagCodeGenerator {

    private final DtoOrchestrator orchestrator;

    public DtoGenerator(final String basePackage, final Path projectRoot, final String clientName, final UserNotifier notifier) {
        this.orchestrator = new DtoOrchestrator(basePackage, projectRoot, clientName, notifier);
    }

    @Override
    public void generate(final String tag, final List<ApiOperation> operations, final Map<String, ApiSchema> schemas)
            throws IOException {
        orchestrator.generate(tag, operations, schemas);
    }
}
