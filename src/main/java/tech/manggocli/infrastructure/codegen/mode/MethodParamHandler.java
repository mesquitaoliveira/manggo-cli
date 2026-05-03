package tech.manggocli.infrastructure.codegen.mode;

import tech.manggocli.core.domain.api.ApiOperation;

import java.util.List;
import java.util.Optional;

/**
 * Pattern: Chain of Responsibility
 * Purpose: Sequential resolution of method parameter list for generated client signatures
 * Thread-safety: Stateless
 */
public abstract class MethodParamHandler {

    protected MethodParamHandler next;

    /**
     * Returns the parameter strings if this handler applies, or empty if it should delegate.
     */
    protected abstract Optional<List<String>> tryBuild(ApiOperation operation, String requestType);

    public List<String> build(
            final ApiOperation operation,
            final String requestType
    ) {
        return tryBuild(operation, requestType)
                .orElseGet(() -> next != null ? next.build(operation, requestType) : List.of());
    }

    public static MethodParamHandler link(
            final MethodParamHandler first,
            final MethodParamHandler... rest
    ) {
        var head = first;
        for (final MethodParamHandler next : rest) {
            head.next = next;
            head = next;
        }
        return first;
    }
}
