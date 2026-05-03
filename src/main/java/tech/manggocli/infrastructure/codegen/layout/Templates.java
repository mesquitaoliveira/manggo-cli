package tech.manggocli.infrastructure.codegen.layout;

public final class Templates {

    private Templates() {}

    // ── Common ────────────────────────────────────────────────────────────────
    public static final String API_MODEL              = "common/api-model.java.mustache";
    public static final String REST_INTERFACE         = "common/rest-interface.java.mustache";
    public static final String APPLICATION_PROPERTIES = "common/application.properties.mustache";
    public static final String DTO                    = "common/dto.java.mustache";
    public static final String STUB_DTO               = "common/stub-dto.java.mustache";
    public static final String EMPTY_DTO              = "common/empty-dto.java.mustache";
    public static final String REQUEST_OBJECT         = "common/request-object.java.mustache";
    public static final String QUERY_MAP_DTO          = "common/query-map-dto.java.mustache";
    public static final String ENUM_DTO               = "common/enum.java.mustache";

    // ── Feign ─────────────────────────────────────────────────────────────────
    public static final String FEIGN_POM                  = "feign/pom.xml.mustache";
    public static final String FEIGN_CLIENT_CONFIGURATION = "feign/client-configuration.java.mustache";
    public static final String FEIGN_CLIENT_CONFIG        = "feign/FeignClientConfig.java.mustache";
    public static final String FEIGN_LOGGER               = "feign/FeignLogger.java.mustache";
    public static final String FEIGN_API_KEY_INTERCEPTOR  = "feign/ApiKeyInterceptor.java.mustache";
    public static final String FEIGN_REST_CLIENT          = "feign/rest-client.java.mustache";
    public static final String FEIGN_SMOKE_TEST           = "feign/smoke-test.java.mustache";

    // ── FeignHc5 ──────────────────────────────────────────────────────────────
    public static final String FEIGN_HC5_POM                  = "feign-hc5/pom.xml.mustache";
    public static final String FEIGN_HC5_CLIENT_CONFIGURATION = "feign-hc5/client-configuration.java.mustache";
    public static final String FEIGN_HC5_SMOKE_TEST           = "feign-hc5/smoke-test.java.mustache";

    // ── Native ────────────────────────────────────────────────────────────────
    public static final String NATIVE_POM                  = "native/pom.xml.mustache";
    public static final String NATIVE_BASE_REST_CLIENT     = "native/base-rest-client.java.mustache";
    public static final String NATIVE_REST_CLIENT          = "native/rest-client.java.mustache";
    public static final String NATIVE_CLIENT_CONFIGURATION = "native/client-configuration.java.mustache";
    public static final String NATIVE_SMOKE_TEST           = "native/smoke-test.java.mustache";
}
