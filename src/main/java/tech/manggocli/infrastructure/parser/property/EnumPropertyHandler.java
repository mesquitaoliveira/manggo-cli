package tech.manggocli.infrastructure.parser.property;

import static tech.manggocli.core.domain.service.NamingService.toValidJavaClassName;

/**
 * Pattern: Chain of Responsibility (Concrete Handler)
 * Purpose: Detects enum-typed properties and registers synthetic enum schemas
 * Thread-safety: Stateless
 */
public final class EnumPropertyHandler extends PropertyTypeHandler {

    @Override
    protected String tryResolve(final PropertyResolutionContext ctx) {
        if (ctx.propSchema().getEnum() == null || ctx.propSchema().getEnum().isEmpty()) return null;
        final String syntheticName = ctx.parentName() + toValidJavaClassName(ctx.propName());
        ctx.registerSchema().accept(syntheticName, ctx.propSchema());
        return syntheticName;
    }
}
