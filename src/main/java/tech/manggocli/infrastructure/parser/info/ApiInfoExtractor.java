package tech.manggocli.infrastructure.parser.info;

import tech.manggocli.core.domain.api.ParsedApi;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Pattern: Single Responsibility
 * Purpose: Extracts API metadata (title, version, baseUrl, auth header) from OpenAPI document
 * Thread-safety: Stateless
 */
public final class ApiInfoExtractor {

    public void extract(final OpenAPI openAPI, final ParsedApi api) {
        if (openAPI.getInfo() != null) {
            api.setTitle(openAPI.getInfo().getTitle());
            api.setVersion(openAPI.getInfo().getVersion());
        }
        if (openAPI.getServers() != null && !openAPI.getServers().isEmpty()) {
            api.setBaseUrl(openAPI.getServers().getFirst().getUrl());
        }
        extractAuthHeader(openAPI, api);
    }

    private void extractAuthHeader(final OpenAPI openAPI, final ParsedApi api) {
        if (openAPI.getComponents() == null
                || openAPI.getComponents().getSecuritySchemes() == null) return;

        openAPI.getComponents().getSecuritySchemes().forEach((name, scheme) -> {
            if (SecurityScheme.Type.APIKEY == scheme.getType()
                    && SecurityScheme.In.HEADER == scheme.getIn()
                    && api.getAuthHeaderName() == null) {
                api.setAuthHeaderName(scheme.getName());
            }
        });
    }
}
