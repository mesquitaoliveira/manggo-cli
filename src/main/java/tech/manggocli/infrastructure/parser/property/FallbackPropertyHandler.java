package tech.manggocli.infrastructure.parser.property;

/**
 * Pattern: Chain of Responsibility (Fallback Handler)
 * Purpose: Handles all remaining property types via standard Java type resolution
 * Thread-safety: Stateless
 */
public final class FallbackPropertyHandler extends PropertyTypeHandler {

    @Override
    protected String tryResolve(final PropertyResolutionContext ctx) {
        return ctx.resolveJavaType().apply(ctx.propSchema());
    }
}
