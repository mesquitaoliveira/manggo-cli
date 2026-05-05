package tech.manggocli.core.domain.api.schema;

/**
 * Schema declared as a top-level array alias ({@code type: array}). Item type already collected.
 */
public record ArrayAliasNode(String name, ApiSchema schema, boolean isRequest) implements SchemaNode {
}
