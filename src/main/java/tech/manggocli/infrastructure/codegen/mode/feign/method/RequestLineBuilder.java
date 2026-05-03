package tech.manggocli.infrastructure.codegen.mode.feign.method;

import tech.manggocli.core.domain.api.ApiOperation;

import java.util.stream.Collectors;

public class RequestLineBuilder {

    public String build(final ApiOperation operation) {
        String path = operation.getPath();
        if (shouldIncludeQueryInPath(operation))
            path = path + buildQueryString(operation);
        return operation.getHttpMethod() + " " + path;
    }

    private boolean shouldIncludeQueryInPath(final ApiOperation operation) {
        return !operation.isUseQueryMap()
            && !operation.needsRequestObject()
            && !operation.getQueryParams().isEmpty();
    }

    private String buildQueryString(final ApiOperation operation) {
        return operation.getQueryParams().stream()
            .map(param -> param.getName() + "={" + param.getCamelCaseName() + "}")
            .collect(Collectors.joining("&", "?", ""));
    }
}
