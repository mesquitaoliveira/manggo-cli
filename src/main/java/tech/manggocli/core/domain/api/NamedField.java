package tech.manggocli.core.domain.api;

import tech.manggocli.core.domain.service.JavaNames;

import static java.util.Optional.ofNullable;

/**
 * Common contract for any named, typed field of the parsed API model
 * (operation parameters, schema properties).
 */
public interface NamedField {

    String getName();

    String getJavaType();

    boolean isRequired();

    String getDescription();

    /**
     * Field name in {@code lowerCamelCase}, safe for use as a Java identifier.
     */
    default String getCamelCaseName() {
        return ofNullable(getName())
                .map(JavaNames::toCamelCase)
                .orElse(defaultFallbackName());
    }

    /**
     * Default identifier when {@link #getName()} is null/blank. Subtype-specific.
     */
    String defaultFallbackName();
}
