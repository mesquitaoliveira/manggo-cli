package tech.manggocli.infrastructure.codegen.layout;

public final class PackageNames {

    private static final String COMMON   = "common";
    private static final String CONFIG   = "config";
    private static final String REST     = "rest";
    private static final String CLIENT   = "client";
    private static final String DOMAIN   = "domain";
    private static final String REQUEST  = "request";
    private static final String RESPONSE = "response";
    private static final String ENUMS    = "enums";

    private PackageNames() {}

    private static String pkg(final String... parts) {
        return String.join(".", parts);
    }

    public static String common(final String basePackage) {
        return pkg(basePackage, COMMON);
    }

    public static String config(final String basePackage) {
        return pkg(basePackage, CONFIG);
    }

    public static String clientConfig(final String basePackage, final String clientName) {
        return pkg(basePackage, CONFIG, clientName);
    }

    public static String rest(final String basePackage, final String clientName, final String tag) {
        return pkg(basePackage, clientName, tag, REST);
    }

    public static String restClient(final String basePackage, final String clientName, final String tag) {
        return pkg(basePackage, clientName, tag, REST, CLIENT);
    }

    public static String domain(final String basePackage, final String clientName, final String tag) {
        return pkg(basePackage, clientName, tag, DOMAIN);
    }

    public static String domainRequest(final String basePackage, final String clientName, final String tag) {
        return pkg(basePackage, clientName, tag, DOMAIN, REQUEST);
    }

    public static String domainResponse(final String basePackage, final String clientName, final String tag) {
        return pkg(basePackage, clientName, tag, DOMAIN, RESPONSE);
    }

    public static String domainEnums(final String basePackage, final String clientName, final String tag) {
        return pkg(basePackage, clientName, tag, DOMAIN, ENUMS);
    }

    public static String importRest(final String basePackage, final String clientName, final String tag, final String pascal) {
        return "import " + rest(basePackage, clientName, tag) + "." + pascal + "Rest;";
    }

    public static String importRestClient(final String basePackage, final String clientName, final String tag, final String pascal) {
        return "import " + restClient(basePackage, clientName, tag) + "." + pascal + "RestClient;";
    }

    public static String importDomainRequest(final String basePackage, final String clientName, final String tag, final String typeName) {
        return "import " + domainRequest(basePackage, clientName, tag) + "." + typeName + ";";
    }

    public static String importDomainResponse(final String basePackage, final String clientName, final String tag, final String typeName) {
        return "import " + domainResponse(basePackage, clientName, tag) + "." + typeName + ";";
    }

    public static String importDomainEnums(final String basePackage, final String clientName, final String tag, final String typeName) {
        return "import " + domainEnums(basePackage, clientName, tag) + "." + typeName + ";";
    }

    public static String importApiKeyInterceptor(final String basePackage, final String client, final String pascal) {
        return "import " + clientConfig(basePackage, client) + "." + pascal + "ApiKeyInterceptor;";
    }

    public static String importFeignLogger(final String basePackage, final String client, final String pascal) {
        return "import " + clientConfig(basePackage, client) + "." + pascal + "FeignLogger;";
    }

    public static String importFeignClientConfig(final String basePackage) {
        return "import " + config(basePackage) + ".FeignClientConfig;";
    }
}
