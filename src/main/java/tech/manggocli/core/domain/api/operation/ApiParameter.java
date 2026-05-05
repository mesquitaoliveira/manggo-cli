package tech.manggocli.core.domain.api.operation;

import tech.manggocli.core.domain.api.NamedField;

/**
 * A single operation parameter (query, path, or header).
 */
public class ApiParameter implements NamedField {

    private String name;
    private String javaType;
    private boolean required;
    private String description;

    public ApiParameter() {
    }

    public ApiParameter(final String name, final String javaType, final boolean required) {
        this.name = name;
        this.javaType = javaType;
        this.required = required;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getJavaType() {
        return javaType;
    }

    public void setJavaType(final String javaType) {
        this.javaType = javaType;
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    public void setRequired(final boolean required) {
        this.required = required;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(final String d) {
        this.description = d;
    }

    @Override
    public String defaultFallbackName() {
        return "param";
    }
}
