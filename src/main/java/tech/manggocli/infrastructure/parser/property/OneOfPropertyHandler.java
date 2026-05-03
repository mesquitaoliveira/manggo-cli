package tech.manggocli.infrastructure.parser.property;

import io.swagger.v3.oas.models.media.Schema;

import java.util.List;

import static tech.manggocli.core.domain.service.NamingService.refToClassName;

/**
 * Pattern: Chain of Responsibility (Concrete Handler)
 * Purpose: Resolves OAS 3.1 oneOf nullable types — oneOf: [{$ref: T}, {type: null}] → T
 * Thread-safety: Stateless
 */
public final class OneOfPropertyHandler extends PropertyTypeHandler {

    @Override
    protected String tryResolve(final PropertyResolutionContext ctx) {
        final List<?> oneOf = ctx.propSchema().getOneOf();
        if (oneOf == null || oneOf.isEmpty()) return null;

        return oneOf.stream()
            .filter(s -> s instanceof Schema<?> schema && schema.get$ref() != null)
            .map(s -> refToClassName(((Schema<?>) s).get$ref()))
            .findFirst()
            .orElseGet(() -> oneOf.stream()
                .filter(s -> s instanceof Schema<?> schema
                        && schema.getType() != null
                        && !"null".equals(schema.getType()))
                .map(s -> ctx.resolveJavaType().apply((Schema<?>) s))
                .findFirst()
                .orElse("Object"));
    }
}
