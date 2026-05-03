package tech.manggocli.infrastructure.codegen.mode.feign.method;

import tech.manggocli.core.domain.api.ApiOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class HeadersBuilder {

    private static final String DEFAULT_CONTENT_TYPE = "application/json";

    public String build(final ApiOperation operation) {
        final List<String> headerList = new ArrayList<>();

        final String contentType = Optional.ofNullable(operation.getConsumes())
            .orElse(DEFAULT_CONTENT_TYPE);
        headerList.add("Content-Type: " + contentType);

        Optional.ofNullable(operation.getProduces())
            .filter(produces -> !produces.equals(contentType))
            .ifPresent(produces -> headerList.add("Accept: " + produces));

        operation.getHeaderParams().stream()
            .map(param -> param.getName() + ": {" + param.getCamelCaseName() + "}")
            .forEach(headerList::add);

        return headerList.stream()
            .map(h -> "\"" + h + "\"")
            .collect(Collectors.joining(", ", "{", "}"));
    }
}
