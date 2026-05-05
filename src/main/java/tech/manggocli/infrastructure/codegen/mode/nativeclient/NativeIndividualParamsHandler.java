package tech.manggocli.infrastructure.codegen.mode.nativeclient;

import tech.manggocli.core.domain.api.operation.ApiOperation;
import tech.manggocli.infrastructure.codegen.mode.MethodParamHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Pattern: Chain of Responsibility (Fallback Handler)
 * Purpose: Emits plain type+name parameters for each path and query parameter
 * Thread-safety: Stateless
 */
public final class NativeIndividualParamsHandler extends MethodParamHandler {

    @Override
    protected Optional<List<String>> tryBuild(final ApiOperation operation, final String requestType) {
        final List<String> params = new ArrayList<>();
        operation.getPathParams().stream()
                .map(p -> p.getJavaType() + " " + p.getCamelCaseName())
                .forEach(params::add);
        operation.getQueryParams().stream()
                .map(p -> p.getJavaType() + " " + p.getCamelCaseName())
                .forEach(params::add);
        return Optional.of(params);
    }
}
