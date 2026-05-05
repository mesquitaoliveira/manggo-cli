package tech.manggocli.infrastructure.codegen.mode.nullarg;

import tech.manggocli.core.domain.api.operation.ApiOperation;

import java.util.List;
import java.util.Optional;

/**
 * Pattern: Chain of Responsibility (Concrete Handler)
 * Purpose: Builds constructor call with nulls for QueryMap operations
 * Thread-safety: Stateless
 */
public final class QueryMapNullArgHandler extends NullArgHandler {

    @Override
    protected Optional<List<String>> tryBuild(final ApiOperation operation, final String requestType) {
        if (!operation.isUseQueryMap() || requestType == null || operation.getQueryParams().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(List.of("new " + requestType + "(" + nulls(operation.getQueryParams().size()) + ")"));
    }
}
