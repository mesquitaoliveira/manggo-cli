package tech.manggocli.infrastructure.codegen.mode.feign;

import tech.manggocli.core.application.port.UserNotifier;
import tech.manggocli.core.domain.api.operation.ApiOperation;
import tech.manggocli.infrastructure.codegen.layout.PackageNames;
import tech.manggocli.infrastructure.codegen.mode.AbstractSmokeTestGenerator;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static java.util.Optional.ofNullable;

/**
 * Smoke test generator shared by Feign and FeignHc5 modes.
 * Both modes import the REST interface, support an auth header name, and disable
 * pure-body tests (Feign rejects null body at encode time). Template differs per mode
 * and is injected at construction.
 */
public class FeignSmokeTestGenerator extends AbstractSmokeTestGenerator {

    private final String authHeaderName;
    private final String templateName;

    public FeignSmokeTestGenerator(final String basePackage,
                                   final Path projectRoot,
                                   final String clientName,
                                   final String authHeaderName,
                                   final UserNotifier notifier,
                                   final String templateName) {
        super(basePackage, projectRoot, clientName, notifier);
        this.authHeaderName = authHeaderName != null ? authHeaderName : "Authorization";
        this.templateName = templateName;
    }

    @Override
    protected String templateName() {
        return templateName;
    }

    @Override
    protected void collectModeSpecificImports(final String tag, final String pascal, final Set<String> imports) {
        imports.add(PackageNames.importRest(basePackage, clientName, tag, pascal));
    }

    @Override
    protected void addModeSpecificContext(final Map<String, Object> ctx, final String tag, final String pascal) {
        ctx.put("restInterface", pascal + "Rest");
        ctx.put("authHeaderName", authHeaderName);
    }

    @Override
    protected boolean isDisabled(final ApiOperation op) {
        return ofNullable(op.getRequestBodySchema())
                .filter(s -> !op.needsRequestObject() && !op.isUseQueryMap())
                .isPresent();
    }
}
