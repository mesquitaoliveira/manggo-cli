package tech.manggocli.core.domain.api;

import tech.manggocli.core.domain.service.NamingService;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Optional.ofNullable;
import static java.util.regex.Pattern.compile;

/**
 * Represents a single HTTP operation from the OpenAPI spec.
 * <p>
 * Parameter strategy:
 * <p>
 * CASE 1 — Query params only, no body, no path (headers are separate @Param)
 * → useQueryMap=true  → generates QueryMap DTO with constructor
 * <p>
 * CASE 2 — Body only (requestBodySchema), no mixed params
 * → individual path params + body as argument
 * <p>
 * CASE 3 — Mix: path + header + body  OR  path + header without body
 * → needsRequestObject=true
 * → generates {OperationName}Request consolidating all params
 * with @JsonProperty for body and constructors for ease of use
 * → AVOIDS multiple loose @Param that cause "Method does not override"
 * <p>
 * CASE 4 — Path params only + no header + normal body
 * → individual @Param path params + separate body (safe)
 */
public class ApiOperation {

    private String tag;
    private String operationId;
    private String httpMethod;
    private String path;
    private String summary;

    private static final Pattern PATH_PARAM = compile("\\{([^}]+)}");
    private static final Pattern NON_ALPHANUMERIC_PATTERN = compile("[^a-zA-Z0-9]+");

    private static final Pattern TRIM_UNDERSCORE = compile("^_|_$");

    private List<ApiParameter> queryParams = new ArrayList<>();
    private List<ApiParameter> pathParams = new ArrayList<>();
    private List<ApiParameter> headerParams = new ArrayList<>();

    private String requestBodySchema;
    private boolean useQueryMap;       // true: query params → @QueryMap com RequestObject
    private String responseSchema;
    private String consumes = "application/json";
    private String produces = "application/json";

    // ─── Getters/Setters ─────────────────────────────────────────────────────

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

    public boolean needsRequestObject() {
        return !queryParams.isEmpty() && !pathParams.isEmpty();
    }

    public String getRequestObjectClassName() {
        final var base = ofNullable(operationId)
                .filter(s -> !s.isBlank())
                .orElseGet(this::buildFallbackName);
        final var camel = NamingService.toCamelCase(base);
        return Character.toUpperCase(camel.charAt(0)) + camel.substring(1) + "Request";
    }

    public String getMethodName() {
        final var raw = ofNullable(operationId)
                .filter(s -> !s.isBlank())
                .orElseGet(this::buildFallbackName);
        return NamingService.toCamelCase(raw);
    }

    private String buildFallbackName() {
        var sanitized = PATH_PARAM.matcher(path).replaceAll("$1");
        sanitized = NON_ALPHANUMERIC_PATTERN.matcher(sanitized).replaceAll("_");
        sanitized = TRIM_UNDERSCORE.matcher(sanitized).replaceAll("");
        return httpMethod.toLowerCase() + "_" + sanitized;
    }
}
