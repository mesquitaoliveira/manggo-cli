package tech.manggocli.core.domain.api;

import tech.manggocli.core.domain.service.NamingService;

import static java.util.Optional.ofNullable;

/**
 * Represents a single operation parameter (query, path, or header).
 */
public class ApiParameter {
    private String name;
    private String javaType;
    private boolean required;
    private String description;

    public ApiParameter() {
    }

    public ApiParameter(
            final String name,
            final String javaType,
            final boolean required
    ) {
        this.name = name;
        this.javaType = javaType;
        this.required = required;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getJavaType() {
        return javaType;
    }

    public void setJavaType(final String javaType) {
        this.javaType = javaType;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(final boolean required) {
        this.required = required;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String d) {
        this.description = d;
    }

    public String getCamelCaseName() {
        return ofNullable(name)
                .map(NamingService::toCamelCase)
                .orElse("param");
    }
}
