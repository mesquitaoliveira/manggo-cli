package tech.manggocli.core.domain.api.schema;

/**
 * Schema with {@code enum:} values. Always treated as response-side (deduped globally).
 */
public record EnumNode(String name, ApiSchema schema) implements SchemaNode {

    @Override
    public boolean isRequest() {
        return false;
    }
}
