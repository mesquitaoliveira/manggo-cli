package tech.manggocli.infrastructure.codegen.mode.nullarg;

import tech.manggocli.core.domain.api.ApiOperation;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Pattern: Chain of Responsibility
 * Purpose: Sequential resolution of null-arg invocations for smoke test method calls
 * Thread-safety: Stateless
 */
public abstract class NullArgHandler {

    protected NullArgHandler next;

    /**
     * Returns the null-arg strings if this handler applies, or empty if it should delegate.
     */
    protected abstract Optional<List<String>> tryBuild(ApiOperation operation, String requestType);

    public List<String> build(final ApiOperation operation, final String requestType) {
        return tryBuild(operation, requestType)
                .orElseGet(() -> next != null ? next.build(operation, requestType) : List.of());
    }

    public static NullArgHandler link(final NullArgHandler first, final NullArgHandler... rest) {
        NullArgHandler head = first;
        for (final NullArgHandler next : rest) {
            head.next = next;
            head = next;
        }
        return first;
    }

    protected static String nulls(final int count) {
        return String.join(", ", Collections.nCopies(count, "null"));
    }
}
