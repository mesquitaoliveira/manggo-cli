package tech.manggocli.core.domain.api;

import tech.manggocli.core.domain.api.operation.ApiOperation;
import tech.manggocli.core.domain.api.schema.ApiSchema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregate root: the OpenAPI spec normalized into the project's domain model.
 * Collection getters return read-only views — mutation must go through setters
 * (used exclusively by the parser during construction).
 */
public class ParsedApi {

    private String title;
    private String version;
    private String baseUrl;
    private String authHeaderName;

    private List<String> tags = new ArrayList<>();
    private List<ApiOperation> operations = new ArrayList<>();
    private Map<String, ApiSchema> schemas = new LinkedHashMap<>();

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(final String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getAuthHeaderName() {
        return authHeaderName;
    }

    public void setAuthHeaderName(final String n) {
        this.authHeaderName = n;
    }

    public List<String> getTags() {
        return Collections.unmodifiableList(tags);
    }

    public void setTags(final List<String> tags) {
        this.tags = new ArrayList<>(tags);
    }

    public List<ApiOperation> getOperations() {
        return Collections.unmodifiableList(operations);
    }

    public void setOperations(final List<ApiOperation> o) {
        this.operations = new ArrayList<>(o);
    }

    public void addOperation(final ApiOperation op) {
        this.operations.add(op);
    }

    public Map<String, ApiSchema> getSchemas() {
        return Collections.unmodifiableMap(schemas);
    }

    public void setSchemas(final Map<String, ApiSchema> s) {
        this.schemas = new LinkedHashMap<>(s);
    }
}
