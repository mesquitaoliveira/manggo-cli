package tech.manggocli.infrastructure.parser.property;

/**
 * Pattern: Chain of Responsibility
 * Purpose: Sequential property type detection — each handler checks one schema case and delegates on miss
 * Thread-safety: Stateless
 */
public abstract class PropertyTypeHandler {

    protected PropertyTypeHandler next;

    /**
     * Tries to resolve the property Java type. Returns null if this handler cannot handle the case.
     */
    protected abstract String tryResolve(PropertyResolutionContext ctx);

    /**
     * Resolves the property type: tries this handler first, then delegates to next in chain.
     */
    public String resolve(final PropertyResolutionContext ctx) {
        final String result = tryResolve(ctx);
        if (result != null) return result;
        return next != null ? next.resolve(ctx) : "Object";
    }

    /**
     * Builds the chain fluently: link(a, b, c) → a → b → c (c is the fallback)
     */
    public static PropertyTypeHandler link(final PropertyTypeHandler first, final PropertyTypeHandler... rest) {
        PropertyTypeHandler head = first;
        for (final PropertyTypeHandler next : rest) {
            head.next = next;
            head = next;
        }
        return first;
    }
}
