package tech.manggocli.infrastructure.codegen.mode.feign.method;

import tech.manggocli.core.domain.api.operation.ApiOperation;
import tech.manggocli.core.domain.api.operation.ApiParameter;
import tech.manggocli.infrastructure.codegen.mode.MethodParamHandler;

import java.util.ArrayList;
import java.util.List;

public class MethodParamsBuilder {

    private static final MethodParamHandler CHAIN = MethodParamHandler.link(
            new FeignRequestObjectParamHandler(),
            new FeignQueryMapParamHandler(),
            new FeignBodyWithPathParamHandler(),
            new FeignIndividualParamsHandler()
    );

    public String build(final ApiOperation operation, final String requestType) {
        final List<String> params = new ArrayList<>(CHAIN.build(operation, requestType));
        operation.getHeaderParams().stream()
                .map(this::formatHeaderParamAnnotation)
                .forEach(params::add);
        return String.join(", ", params);
    }

    private String formatHeaderParamAnnotation(final ApiParameter param) {
        return "@Param(\"" + param.getCamelCaseName() + "\") " + param.getJavaType() + " " + param.getCamelCaseName();
    }
}
