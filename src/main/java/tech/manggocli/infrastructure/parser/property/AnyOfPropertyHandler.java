package tech.manggocli.infrastructure.parser.property;

import io.swagger.v3.oas.models.media.Schema;

import java.util.List;

import static tech.manggocli.core.domain.service.NamingService.refToClassName;

/**
 * Pattern: Chain of Responsibility (Concrete Handler)
 * Purpose: Resolves OAS anyOf — prefers $ref branches, falls back to first non-null typed branch
 * Thread-safety: Stateless
 */
public final class AnyOfPropertyHandler extends PropertyTypeHandler {

    @Override
    protected String tryResolve(final PropertyResolutionContext ctx) {
        final List<?> anyOf = ctx.propSchema().getAnyOf();
        if (anyOf == null || anyOf.isEmpty()) return null;

        return anyOf.stream()
            .filter(s -> s instanceof Schema<?> schema && schema.get$ref() != null)
            .map(s -> refToClassName(((Schema<?>) s).get$ref()))
            .findFirst()
            .orElseGet(() -> anyOf.stream()
                .filter(s -> s instanceof Schema<?> schema
                        && schema.getType() != null
                        && !"null".equals(schema.getType()))
                .map(s -> ctx.resolveJavaType().apply((Schema<?>) s))
                .findFirst()
                .orElse(null));
    }
}
