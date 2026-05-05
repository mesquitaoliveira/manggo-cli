package tech.manggocli.core.domain.api.schema;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ApiSchema {
    private String name;
    private String type;
    private String itemsType;
    private String refName;
    private Map<String, ApiSchemaProperty> properties = new LinkedHashMap<>();
    private boolean enumType;
    private List<String> enumValues = new ArrayList<>();
    private final List<String> oneOfTypes = new ArrayList<>();
    private final List<String> anyOfTypes = new ArrayList<>();
    private String discriminatorProperty;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getItemsType() {
        return itemsType;
    }

    public void setItemsType(final String itemsType) {
        this.itemsType = itemsType;
    }

    public String getRefName() {
        return refName;
    }

    public void setRefName(final String refName) {
        this.refName = refName;
    }

    public Map<String, ApiSchemaProperty> getProperties() {
        return properties;
    }

    public void setProperties(final Map<String, ApiSchemaProperty> p) {
        this.properties = p;
    }

    public boolean isEnumType() {
        return enumType;
    }

    public void setEnumType(final boolean enumType) {
        this.enumType = enumType;
    }

    public List<String> getEnumValues() {
        return enumValues;
    }

    public void setEnumValues(final List<String> v) {
        this.enumValues = v;
    }

    public List<String> getOneOfTypes() {
        return oneOfTypes;
    }

    public List<String> getAnyOfTypes() {
        return anyOfTypes;
    }

    public String getDiscriminatorProperty() {
        return discriminatorProperty;
    }

    public void setDiscriminatorProperty(final String d) {
        this.discriminatorProperty = d;
    }
}
