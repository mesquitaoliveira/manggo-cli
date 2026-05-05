package tech.manggocli.infrastructure.parser.operation;

import tech.manggocli.core.domain.api.operation.ApiOperation;
import tech.manggocli.core.domain.api.operation.ApiParameter;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

/**
 * Pattern: Single Responsibility
 * Purpose: Extracts query, path, and header parameters from an OAS operation
 * Thread-safety: Stateless
 */
public final class ParameterExtractor {

    private static final Logger log = LoggerFactory.getLogger(ParameterExtractor.class);

    private final Function<Schema<?>, String> resolveJavaType;

    public ParameterExtractor(final Function<Schema<?>, String> resolveJavaType) {
        this.resolveJavaType = resolveJavaType;
    }

    public void extract(final Operation operation, final ApiOperation op) {
        if (operation.getParameters() == null) return;

        for (final Parameter param : operation.getParameters()) {
            final ApiParameter apiParam = buildParam(param);
            switch (param.getIn()) {
                case "query"  -> op.getQueryParams().add(apiParam);
                case "path"   -> op.getPathParams().add(apiParam);
                case "header" -> op.getHeaderParams().add(apiParam);
                default -> log.warn("Param in desconhecido: {} para {}", param.getIn(), param.getName());
            }
        }
    }

    private ApiParameter buildParam(final Parameter param) {
        final ApiParameter apiParam = new ApiParameter();
        apiParam.setName(param.getName());
        apiParam.setRequired(Boolean.TRUE.equals(param.getRequired()));
        apiParam.setDescription(param.getDescription());
        apiParam.setJavaType(param.getSchema() != null
            ? resolveJavaType.apply(param.getSchema()) : "String");
        return apiParam;
    }
}
