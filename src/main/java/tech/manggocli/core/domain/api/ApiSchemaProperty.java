package tech.manggocli.core.domain.api;

import tech.manggocli.core.domain.service.NamingService;

import static java.util.Optional.ofNullable;

public class ApiSchemaProperty {
    private String name;
    private String javaType;
    private boolean required;
    private String description;
    private String format;

    public ApiSchemaProperty() {
    }

    public ApiSchemaProperty(final String name, final String javaType, final boolean required) {
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

    public String getFormat() {
        return format;
    }

    public void setFormat(final String format) {
        this.format = format;
    }

    public String getCamelCaseName() {
        return ofNullable(name)
                .map(NamingService::toCamelCase)
                .orElse("field");
    }

    public String getCapitalizedName() {
        final String camel = getCamelCaseName();
        return camel.isEmpty() ? camel : Character.toUpperCase(camel.charAt(0)) + camel.substring(1);
    }
}
