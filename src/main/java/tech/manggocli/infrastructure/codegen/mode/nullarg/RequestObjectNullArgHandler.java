package tech.manggocli.infrastructure.codegen.mode.nullarg;

import tech.manggocli.core.domain.api.ApiOperation;

import java.util.List;
import java.util.Optional;

/**
 * Pattern: Chain of Responsibility (Concrete Handler)
 * Purpose: Builds constructor call with nulls for RequestObject-consolidated operations
 * Thread-safety: Stateless
 */
public final class RequestObjectNullArgHandler extends NullArgHandler {

    @Override
    protected Optional<List<String>> tryBuild(final ApiOperation operation, final String requestType) {
        if (!operation.needsRequestObject() || requestType == null) return Optional.empty();
        int n = operation.getPathParams().size() + operation.getQueryParams().size();
        return Optional.of(List.of("new " + requestType + "(" + nulls(Math.max(n, 1)) + ")"));
    }
}
