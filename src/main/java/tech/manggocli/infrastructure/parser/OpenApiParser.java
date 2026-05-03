package tech.manggocli.infrastructure.parser;

import tech.manggocli.core.application.exception.OpenApiParseException;
import tech.manggocli.core.domain.api.ApiSchema;
import tech.manggocli.core.domain.api.ParsedApi;
import tech.manggocli.infrastructure.parser.info.ApiInfoExtractor;
import tech.manggocli.infrastructure.parser.operation.OperationExtractor;
import tech.manggocli.infrastructure.parser.operation.ParameterExtractor;
import tech.manggocli.infrastructure.parser.operation.RequestBodyExtractor;
import tech.manggocli.infrastructure.parser.operation.ResponseExtractor;
import tech.manggocli.infrastructure.parser.schema.ArrayAliasResolver;
import tech.manggocli.infrastructure.parser.schema.SchemaBuilder;
import tech.manggocli.infrastructure.parser.schema.SchemaExtractor;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Orchestrator — two-pass OpenAPI parsing pipeline:
 *
 * PASS 1 (resolve=true, resolveFully=false)
 *   Reads paths/operations preserving $ref names → used for schema/response name extraction
 *
 * PASS 2 (resolveFully=true) [implicit via extractSchemas from same raw model]
 *   Builds schema property maps from fully-resolved components
 *
 * Each stage is delegated to a focused extractor. This class owns only
 * the parse lifecycle and wires the extractors together.
 */
public class OpenApiParser {

    private static final Logger log = LoggerFactory.getLogger(OpenApiParser.class);

    public ParsedApi parse(final String filePath) {
        final String location = new java.io.File(filePath).toURI().toString();

        final ParseOptions opts = new ParseOptions();
        opts.setResolve(true);
        opts.setResolveFully(false);
        final OpenAPI rawApi = parseOpenAPI(location, opts);

        final Map<String, ApiSchema> schemas = new LinkedHashMap<>();
        final SchemaBuilder builder          = new SchemaBuilder(schemas);
        final ArrayAliasResolver aliases     = new ArrayAliasResolver(schemas);

        final ParsedApi api = new ParsedApi();
        new ApiInfoExtractor().extract(rawApi, api);
        new SchemaExtractor(schemas, builder).extract(rawApi);
        aliases.resolveInProperties();
        new OperationExtractor(
            new ParameterExtractor(builder::resolveJavaType),
            new RequestBodyExtractor(),
            new ResponseExtractor(schemas, builder, aliases)
        ).extract(rawApi, api);

        api.setSchemas(schemas);
        return api;
    }

    private OpenAPI parseOpenAPI(final String location, final ParseOptions options) {
        final SwaggerParseResult result = new OpenAPIV3Parser().readLocation(location, null, options);
        if (result.getMessages() != null && !result.getMessages().isEmpty()) {
            result.getMessages().forEach(msg -> log.warn("Parser warning: {}", msg));
        }
        final OpenAPI openAPI = result.getOpenAPI();
        if (openAPI == null) {
            throw new OpenApiParseException(location, result.getMessages());
        }
        return openAPI;
    }
}
