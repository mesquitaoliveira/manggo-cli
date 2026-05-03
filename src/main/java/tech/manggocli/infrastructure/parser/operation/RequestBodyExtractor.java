package tech.manggocli.infrastructure.parser.operation;

import tech.manggocli.core.domain.api.ApiOperation;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static tech.manggocli.core.domain.service.NamingService.refToClassName;
import static tech.manggocli.core.domain.service.NamingService.toValidJavaClassName;

/**
 * Pattern: Single Responsibility
 * Purpose: Extracts request body schema reference from an OAS operation
 * Thread-safety: Stateless
 */
public final class RequestBodyExtractor {

    private static final Logger log = LoggerFactory.getLogger(RequestBodyExtractor.class);

    public void extract(final Operation operation, final ApiOperation op) {
        final RequestBody requestBody = operation.getRequestBody();
        if (requestBody == null || requestBody.getContent() == null) return;

        requestBody.getContent().forEach((mediaType, content) -> {
            if (content.getSchema() == null) return;
            extractSchemaName(content.getSchema()).ifPresent(name -> {
                log.debug("RequestBody schema: {}", name);
                op.setRequestBodySchema(name);
            });
            op.setConsumes(mediaType);
        });
    }

    static Optional<String> extractSchemaName(final Schema<?> schema) {
        if (schema.get$ref() != null) {
            final String ref = schema.get$ref();
            return Optional.of(refToClassName(ref));
        }
        if (schema.getName() != null) return Optional.of(toValidJavaClassName(schema.getName()));
        if (schema.getExtensions() != null
                && schema.getExtensions().containsKey("x-schema-name")) {
            return Optional.ofNullable((String) schema.getExtensions().get("x-schema-name"));
        }
        return Optional.empty();
    }
}
