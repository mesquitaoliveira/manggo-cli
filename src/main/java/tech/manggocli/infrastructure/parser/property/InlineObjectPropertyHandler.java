package tech.manggocli.infrastructure.parser.property;

import static tech.manggocli.core.domain.service.JavaIdentifiers.toValidJavaClassName;

/**
 * Pattern: Chain of Responsibility (Concrete Handler)
 * Purpose: Detects inline object properties and registers synthetic object schemas
 * Thread-safety: Stateless
 */
public final class InlineObjectPropertyHandler extends PropertyTypeHandler {

    @Override
    protected String tryResolve(final PropertyResolutionContext ctx) {
        final var schema = ctx.propSchema();
        if (!"object".equals(schema.getType())
                || schema.getProperties() == null
                || schema.getProperties().isEmpty()) return null;
        final String syntheticName = ctx.parentName() + toValidJavaClassName(ctx.propName());
        ctx.registerSchema().accept(syntheticName, schema);
        return syntheticName;
    }
}
