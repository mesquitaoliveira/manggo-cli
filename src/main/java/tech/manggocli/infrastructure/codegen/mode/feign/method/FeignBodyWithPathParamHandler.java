package tech.manggocli.infrastructure.codegen.mode.feign.method;

import tech.manggocli.core.domain.api.ApiOperation;
import tech.manggocli.infrastructure.codegen.mode.MethodParamHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Pattern: Chain of Responsibility (Concrete Handler)
 * Purpose: Handles operations with a request body — includes @Param path params + @Nonnull body
 * Thread-safety: Stateless
 */
public final class FeignBodyWithPathParamHandler extends MethodParamHandler {

    @Override
    protected Optional<List<String>> tryBuild(final ApiOperation operation, final String requestType) {
        if (operation.getRequestBodySchema() == null || requestType == null) return Optional.empty();
        final List<String> params = new ArrayList<>();
        operation.getPathParams().stream()
                .map(p -> "@Param(\"" + p.getName() + "\") " + p.getJavaType() + " " + p.getCamelCaseName())
                .forEach(params::add);
        params.add("@Nonnull " + requestType + " request");
        return Optional.of(params);
    }
}
