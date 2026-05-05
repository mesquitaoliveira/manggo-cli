package tech.manggocli.infrastructure.codegen.dto.builder;

import tech.manggocli.core.domain.api.operation.ApiParameter;
import tech.manggocli.core.domain.api.schema.ApiSchemaProperty;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps parameters and schema properties to Mustache template contexts.
 * Centralizes camelCase-to-capitalized name conversion.
 */
public class FieldMapper {

    /**
     * Maps an API parameter to a template context map.
     */
    public static Map<String, Object> mapParameter(final ApiParameter param) {
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("javaType", param.getJavaType());
        map.put("fieldName", param.getCamelCaseName());
        map.put("capitalizedName", capitalize(param.getCamelCaseName()));
        map.put("name", param.getName());
        map.put("description", param.getDescription());
        return map;
    }

    /**
     * Maps a schema property to a template context map.
     */
    public static Map<String, Object> mapProperty(final ApiSchemaProperty prop) {
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("javaType", prop.getJavaType());
        map.put("fieldName", prop.getCamelCaseName());
        map.put("capitalizedName", prop.getCapitalizedName());
        map.put("name", prop.getName());
        map.put("description", prop.getDescription());
        map.put("deprecated", prop.getDescription() != null
            && prop.getDescription().toLowerCase().contains("deprecated"));
        return map;
    }

    /**
     * Capitalizes the first letter of a name.
     * E.g. "userId" → "UserId", "" → ""
     */
    public static String capitalize(final String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
