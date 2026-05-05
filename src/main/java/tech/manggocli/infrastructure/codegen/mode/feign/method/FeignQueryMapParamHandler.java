package tech.manggocli.infrastructure.codegen.mode.feign.method;

import tech.manggocli.core.domain.api.operation.ApiOperation;
import tech.manggocli.infrastructure.codegen.mode.MethodParamHandler;

import java.util.List;
import java.util.Optional;

/**
 * Pattern: Chain of Responsibility (Concrete Handler)
 * Purpose: Handles standalone QueryMap parameter (no RequestObject consolidation)
 * Thread-safety: Stateless
 */
public final class FeignQueryMapParamHandler extends MethodParamHandler {

    @Override
    protected Optional<List<String>> tryBuild(final ApiOperation operation, final String requestType) {
        if (!operation.isUseQueryMap() || requestType == null) return Optional.empty();
        return Optional.of(List.of("@Nonnull @QueryMap " + requestType + " request"));
    }
}
