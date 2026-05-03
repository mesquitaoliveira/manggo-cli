package tech.manggocli.core.domain.api;

public final class SchemaNode {

    public enum Kind {ENUM, OBJECT, ARRAY_ALIAS, MISSING}

    private final String name;
    private final ApiSchema schema;
    private final boolean isRequest;
    private final Kind kind;

    private SchemaNode(
            final String name,
            final ApiSchema schema,
            final boolean isRequest,
            final Kind kind
    ) {
        this.name = name;
        this.schema = schema;
        this.isRequest = isRequest;
        this.kind = kind;
    }

    public static SchemaNode enumNode(final String name, final ApiSchema schema) {
        return new SchemaNode(name, schema, false, Kind.ENUM);
    }

    public static SchemaNode objectNode(final String name, final ApiSchema schema, final boolean isRequest) {
        return new SchemaNode(name, schema, isRequest, Kind.OBJECT);
    }

    public static SchemaNode arrayAlias(final String name, final ApiSchema schema, final boolean isRequest) {
        return new SchemaNode(name, schema, isRequest, Kind.ARRAY_ALIAS);
    }

public static SchemaNode missing(final String name, final boolean isRequest) {
        return new SchemaNode(name, null, isRequest, Kind.MISSING);
    }

    public String name() {
        return name;
    }

    public ApiSchema schema() {
        return schema;
    }

    public boolean isRequest() {
        return isRequest;
    }

    public Kind kind() {
        return kind;
    }

    public boolean isEnum() {
        return kind == Kind.ENUM;
    }

    public boolean isArrayAlias() {
        return kind == Kind.ARRAY_ALIAS;
    }

public boolean isMissing() {
        return kind == Kind.MISSING;
    }
}
