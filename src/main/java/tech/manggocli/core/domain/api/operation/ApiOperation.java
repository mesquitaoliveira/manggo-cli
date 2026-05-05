package tech.manggocli.core.domain.api.operation;

import tech.manggocli.core.domain.service.JavaNames;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Optional.ofNullable;

/**
 * A single HTTP operation parsed from an OpenAPI spec.
 *
 * <h3>Parameter strategy (decided by the parser, applied here)</h3>
 * <ul>
 *   <li><b>CASE 1</b> — query params only, no body, no path → {@link #useQueryMap}=true.
 *       A QueryMap DTO is generated.</li>
 *   <li><b>CASE 2</b> — body only, no mixed params → individual path params + body.</li>
 *   <li><b>CASE 3</b> — path + header + body, or path + header without body → {@link #needsRequestObject()}=true.
 *       A {OperationName}Request consolidates everything (avoids loose @Param overrides).</li>
 *   <li><b>CASE 4</b> — path params only, no header, normal body → individual @Param + separate body.</li>
 * </ul>
 */
public class ApiOperation {

    private static final Pattern PATH_PARAM_BRACES = Pattern.compile("\\{([^}]+)}");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-zA-Z0-9]+");
    private static final Pattern TRIM_UNDERSCORE = Pattern.compile("^_|_$");
    private static final String REQUEST_SUFFIX = "Request";

    private String tag;
    private String operationId;
    private String httpMethod;
    private String path;
    private String summary;

    private List<ApiParameter> queryParams = new ArrayList<>();
    private List<ApiParameter> pathParams = new ArrayList<>();
    private List<ApiParameter> headerParams = new ArrayList<>();

    private String requestBodySchema;
    private boolean useQueryMap;
    private String responseSchema;
    private String consumes = "application/json";
    private String produces = "application/json";

    public boolean needsRequestObject() {
        return !queryParams.isEmpty() && !pathParams.isEmpty();
    }

    public String getRequestObjectClassName() {
        final String camel = JavaNames.toCamelCase(operationIdOrFallback());
        return Character.toUpperCase(camel.charAt(0)) + camel.substring(1) + REQUEST_SUFFIX;
    }

    public String getMethodName() {
        return JavaNames.toCamelCase(operationIdOrFallback());
    }

    private String operationIdOrFallback() {
        return ofNullable(operationId).filter(s -> !s.isBlank()).orElseGet(this::buildFallbackName);
    }

    private String buildFallbackName() {
        String sanitized = PATH_PARAM_BRACES.matcher(path).replaceAll("$1");
        sanitized = NON_ALPHANUMERIC.matcher(sanitized).replaceAll("_");
        sanitized = TRIM_UNDERSCORE.matcher(sanitized).replaceAll("");
        return httpMethod.toLowerCase() + "_" + sanitized;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(final String tag) {
        this.tag = tag;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(final String id) {
        this.operationId = id;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(final String m) {
        this.httpMethod = m;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(final String s) {
        this.summary = s;
    }

    public List<ApiParameter> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(final List<ApiParameter> p) {
        this.queryParams = p;
    }

    public List<ApiParameter> getPathParams() {
        return pathParams;
    }

    public void setPathParams(final List<ApiParameter> p) {
        this.pathParams = p;
    }

    public List<ApiParameter> getHeaderParams() {
        return headerParams;
    }

    public void setHeaderParams(final List<ApiParameter> p) {
        this.headerParams = p;
    }

    public String getRequestBodySchema() {
        return requestBodySchema;
    }

    public void setRequestBodySchema(final String s) {
        this.requestBodySchema = s;
    }

    public boolean isUseQueryMap() {
        return useQueryMap;
    }

    public void setUseQueryMap(final boolean b) {
        this.useQueryMap = b;
    }

    public String getResponseSchema() {
        return responseSchema;
    }

    public void setResponseSchema(final String s) {
        this.responseSchema = s;
    }

    public String getConsumes() {
        return consumes;
    }

    public void setConsumes(final String c) {
        this.consumes = c;
    }

    public String getProduces() {
        return produces;
    }

    public void setProduces(final String p) {
        this.produces = p;
    }
}
