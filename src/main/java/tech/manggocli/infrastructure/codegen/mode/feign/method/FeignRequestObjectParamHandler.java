package tech.manggocli.infrastructure.codegen.mode.feign.method;

import tech.manggocli.core.domain.api.operation.ApiOperation;
import tech.manggocli.infrastructure.codegen.mode.MethodParamHandler;

import java.util.List;
import java.util.Optional;

/**
 * Pattern: Chain of Responsibility (Concrete Handler)
 * Purpose: Handles consolidated RequestObject — emits @Nonnull (+ @QueryMap prefix when applicable)
 * Thread-safety: Stateless
 */
public final class FeignRequestObjectParamHandler extends MethodParamHandler {

    @Override
    protected Optional<List<String>> tryBuild(final ApiOperation operation, final String requestType) {
        if (!operation.needsRequestObject()) return Optional.empty();
        final String param = "@Nonnull " + requestType + " request";
        return Optional.of(List.of(operation.isUseQueryMap() ? "@QueryMap " + param : param));
    }
}
