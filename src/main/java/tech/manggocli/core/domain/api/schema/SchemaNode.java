package tech.manggocli.core.domain.api.schema;

/**
 * A node in the schema dependency graph collected by {@code SchemaGraphResolver}.
 * Sealed: every variant is exhaustively pattern-matchable.
 */
public sealed interface SchemaNode permits EnumNode, ObjectNode, ArrayAliasNode, MissingNode {

    String name();

    /**
     * Whether this node was reached from a request type (vs a response type).
     */
    boolean isRequest();
}
