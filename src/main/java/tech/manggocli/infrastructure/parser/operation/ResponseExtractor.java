package tech.manggocli.infrastructure.parser.operation;

import tech.manggocli.core.domain.api.operation.ApiOperation;
import tech.manggocli.core.domain.api.schema.ApiSchema;
import tech.manggocli.infrastructure.parser.schema.ArrayAliasResolver;
import tech.manggocli.infrastructure.parser.schema.SchemaBuilder;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static tech.manggocli.core.domain.service.JavaNames.toPascalCase;

/**
 * Pattern: Single Responsibility
 * Purpose: Extracts 2xx response schema from an OAS operation.
 * Registers anonymous inline schemas with names derived from operationId.
 * Thread-safety: Stateful — writes to shared schemas map for inline schema registration
 */
public final class ResponseExtractor {

    private static final Logger log = LoggerFactory.getLogger(ResponseExtractor.class);

    private final Map<String, ApiSchema> schemas;
    private final SchemaBuilder builder;
    private final ArrayAliasResolver aliasResolver;

    public ResponseExtractor(final Map<String, ApiSchema> schemas,
                             final SchemaBuilder builder,
                             final ArrayAliasResolver aliasResolver) {
        this.schemas = schemas;
        this.builder = builder;
        this.aliasResolver = aliasResolver;
    }

    public void extract(final Operation operation, final ApiOperation op) {
        if (operation.getResponses() == null) return;

        for (final Map.Entry<String, ApiResponse> entry : operation.getResponses().entrySet()) {
            if (!entry.getKey().startsWith("2")) continue;
            final ApiResponse response = entry.getValue();
            if (response.getContent() == null) break;

            response.getContent().forEach((mediaType, content) -> {
                if (content.getSchema() == null || op.getResponseSchema() != null) return;

                String schemaName = RequestBodyExtractor.extractSchemaName(content.getSchema()).orElse(null);

                if (schemaName == null && content.getSchema().getType() != null) {
                    schemaName = registerInlineSchema(op, content.getSchema());
                }

                if (schemaName != null) {
                    schemaName = aliasResolver.resolveAlias(schemaName);
                    op.setResponseSchema(schemaName);
                    op.setProduces(mediaType);
                }
            });
            break;
        }
    }

    private String registerInlineSchema(final ApiOperation op, final Schema<?> schema) {
        final String name = toPascalCase(
                op.getOperationId() != null ? op.getOperationId() : "Unknown"
        ) + "Response";
        schemas.put(name, builder.buildApiSchema(name, schema));
        log.debug("Schema inline registrado: {}", name);
        return name;
    }
}
