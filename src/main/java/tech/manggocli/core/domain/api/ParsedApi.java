package tech.manggocli.core.domain.api;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ParsedApi {
    private String title;
    private String version;
    private String baseUrl;
    private List<String> tags = new ArrayList<>();
    private List<ApiOperation> operations = new ArrayList<>();
    private Map<String, ApiSchema> schemas = new LinkedHashMap<>();

    private String authHeaderName;

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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(final List<String> tags) {
        this.tags = tags;
    }

    public List<ApiOperation> getOperations() {
        return operations;
    }

    public void setOperations(final List<ApiOperation> o) {
        this.operations = o;
    }

    public Map<String, ApiSchema> getSchemas() {
        return schemas;
    }

    public void setSchemas(final Map<String, ApiSchema> s) {
        this.schemas = s;
    }

    public String getAuthHeaderName() {
        return authHeaderName;
    }

    public void setAuthHeaderName(final String n) {
        this.authHeaderName = n;
    }
}
