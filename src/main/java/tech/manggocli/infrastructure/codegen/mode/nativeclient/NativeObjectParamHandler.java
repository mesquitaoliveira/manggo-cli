package tech.manggocli.infrastructure.codegen.mode.nativeclient;

import tech.manggocli.core.domain.api.operation.ApiOperation;
import tech.manggocli.infrastructure.codegen.mode.MethodParamHandler;

import java.util.List;
import java.util.Optional;

/**
 * Pattern: Chain of Responsibility (Concrete Handler)
 * Purpose: Handles RequestObject and standalone QueryMap — both emit a single @Nonnull parameter in native mode
 * Thread-safety: Stateless
 */
public final class NativeObjectParamHandler extends MethodParamHandler {

    @Override
    protected Optional<List<String>> tryBuild(final ApiOperation operation, final String requestType) {
        if (!operation.needsRequestObject() && (!operation.isUseQueryMap() || requestType == null)) {
            return Optional.empty();
        }
        return Optional.of(List.of("@Nonnull " + requestType + " request"));
    }
}
