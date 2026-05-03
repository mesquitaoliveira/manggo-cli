package tech.manggocli.infrastructure.parser.operation;

import tech.manggocli.core.domain.api.ApiOperation;
import tech.manggocli.core.domain.api.ParsedApi;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Pattern: Single Responsibility
 * Purpose: Extracts all API operations from OpenAPI paths, delegating each concern
 * (parameters, request body, response) to focused sub-extractors.
 * Thread-safety: Stateless (sub-extractors hold any state they need)
 */
public final class OperationExtractor {

    private static final Logger log = LoggerFactory.getLogger(OperationExtractor.class);
    private static final String DEFAULT_TAG = "default";
    private static final Pattern TAG_DELIMITERS = Pattern.compile("[&/|,]");
    private static final Pattern NON_TAG_CHARS = Pattern.compile("[^a-z0-9]+");
    private static final Pattern LEADING_TRAILING_UNDERSCORE = Pattern.compile("^_|_$");

    private final ParameterExtractor parameterExtractor;
    private final RequestBodyExtractor requestBodyExtractor;
    private final ResponseExtractor responseExtractor;

    public OperationExtractor(
            ParameterExtractor parameterExtractor,
            RequestBodyExtractor requestBodyExtractor,
            ResponseExtractor responseExtractor
    ) {
        this.parameterExtractor = parameterExtractor;
        this.requestBodyExtractor = requestBodyExtractor;
        this.responseExtractor = responseExtractor;
    }

    public void extract(final OpenAPI rawApi, final ParsedApi api) {
        if (rawApi.getPaths() == null) return;
        final Set<String> tagsSet = new LinkedHashSet<>();
        rawApi.getPaths().forEach((path, pathItem) ->
                extractFromPathItem(path, pathItem, api, tagsSet)
        );
        api.setTags(new ArrayList<>(tagsSet));
    }

    private void extractFromPathItem(
            final String path,
            final PathItem pathItem,
            final ParsedApi api,
            final Set<String> tagsSet
    ) {
        final Map<PathItem.HttpMethod, Operation> opsMap = pathItem.readOperationsMap();
        if (opsMap == null) return;

        opsMap.forEach((httpMethod, operation) -> {
            if (operation == null) return;
            final String tag = resolveTag(operation);
            tagsSet.add(tag);

            final ApiOperation op = buildOperation(tag, httpMethod, path, operation);
            parameterExtractor.extract(operation, op);
            requestBodyExtractor.extract(operation, op);
            responseExtractor.extract(operation, op);

            if (!op.getQueryParams().isEmpty()
                    && op.getPathParams().isEmpty()
                    && op.getRequestBodySchema() == null) {
                op.setUseQueryMap(true);
            }
            api.getOperations().add(op);

            log.debug("Op: {} {} [{}] tag={}", op.getHttpMethod(), path, op.getOperationId(), tag);
        });
    }

    private ApiOperation buildOperation(
            final String tag,
            final PathItem.HttpMethod httpMethod,
            final String path,
            final Operation operation
    ) {
        final ApiOperation op = new ApiOperation();
        op.setTag(tag);
        op.setHttpMethod(httpMethod.name().toUpperCase());
        op.setPath(path);
        op.setSummary(operation.getSummary());
        op.setOperationId(operation.getOperationId());
        return op;
    }

    private String resolveTag(final Operation operation) {
        if (operation.getTags() != null && !operation.getTags().isEmpty()) {
            final String raw = TAG_DELIMITERS.split(operation.getTags().getFirst())[0].trim();
            return LEADING_TRAILING_UNDERSCORE.matcher(
                    NON_TAG_CHARS.matcher(raw.toLowerCase()).replaceAll("_")
            ).replaceAll("");
        }
        return DEFAULT_TAG;
    }
}
