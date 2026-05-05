package tech.manggocli.core.domain.api.schema;

/**
 * Schema with object properties. Generated as a regular DTO.
 */
public record ObjectNode(String name, ApiSchema schema, boolean isRequest) implements SchemaNode {
}
