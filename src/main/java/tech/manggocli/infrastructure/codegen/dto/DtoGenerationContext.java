package tech.manggocli.infrastructure.codegen.dto;

import tech.manggocli.core.application.port.UserNotifier;
import tech.manggocli.core.domain.api.ApiOperation;
import tech.manggocli.core.domain.api.ApiSchema;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public record DtoGenerationContext(
    String tag,
    ApiOperation operation,
    Map<String, ApiSchema> schemas,
    String basePackage,
    String clientName,
    Path projectRoot,
    UserNotifier notifier,
    Set<String> generated,
    boolean isRequest
) {}

