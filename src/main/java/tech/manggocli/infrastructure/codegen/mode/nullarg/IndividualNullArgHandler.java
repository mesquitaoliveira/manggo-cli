package tech.manggocli.infrastructure.codegen.mode.nullarg;

import tech.manggocli.core.domain.api.ApiOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Pattern: Chain of Responsibility (Fallback Handler)
 * Purpose: Emits one null per path and query parameter
 * Thread-safety: Stateless
 */
public final class IndividualNullArgHandler extends NullArgHandler {

    @Override
    protected Optional<List<String>> tryBuild(final ApiOperation operation, final String requestType) {
        final List<String> args = new ArrayList<>();
        operation.getPathParams().forEach(p -> args.add("null"));
        operation.getQueryParams().forEach(p -> args.add("null"));
        return Optional.of(args);
    }
}
