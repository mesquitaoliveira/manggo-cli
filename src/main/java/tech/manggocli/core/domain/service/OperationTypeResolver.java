package tech.manggocli.core.domain.service;

import tech.manggocli.core.domain.api.ApiOperation;

/**
 * Domain service: resolves the Java request/response types for an API operation.
 *
 * Single source of truth — eliminates duplication across RestInterfaceGenerator,
 * RestClientGenerator, NativeRestClientGenerator, FeignSmokeTestGenerator,
 * NativeSmokeTestGenerator, and FeignHc5SmokeTestGenerator.
 */
public final class OperationTypeResolver {

    private OperationTypeResolver() {}

    /**
     * Returns the Java class name that represents the request payload for {@code op}.
     * Returns null if the operation has no input type.
     */
    public static String resolveRequestType(final String tag, final ApiOperation op) {
        if (op.needsRequestObject()) {
            return op.getRequestObjectClassName();
        }
        if (op.getRequestBodySchema() != null) {
            return op.getRequestBodySchema();
        }
        if (op.isUseQueryMap() && !op.getQueryParams().isEmpty()) {
            return NamingService.toPascalCase(tag) + NamingService.capitalize(op.getMethodName()) + "Request";
        }
        return null;
    }

    /**
     * Returns the Java class name (possibly List<X>) for the response, or null for void.
     */
    public static String resolveResponseType(final ApiOperation op) {
        return op.getResponseSchema();
    }
}
