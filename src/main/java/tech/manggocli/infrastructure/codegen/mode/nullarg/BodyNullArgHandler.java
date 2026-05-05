package tech.manggocli.infrastructure.codegen.mode.nullarg;

import tech.manggocli.core.domain.api.operation.ApiOperation;

import java.util.List;
import java.util.Optional;

/**
 * Pattern: Chain of Responsibility (Concrete Handler)
 * Purpose: Emits a single null for operations with a request body (no RequestObject/QueryMap)
 * Thread-safety: Stateless
 */
public final class BodyNullArgHandler extends NullArgHandler {

    @Override
    protected Optional<List<String>> tryBuild(final ApiOperation operation, final String requestType) {
        if (operation.getRequestBodySchema() == null) return Optional.empty();
        return Optional.of(List.of("null"));
    }
}
