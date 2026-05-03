package tech.manggocli.infrastructure.codegen.mode.feign;

import tech.manggocli.core.application.port.UserNotifier;
import tech.manggocli.infrastructure.codegen.layout.PackageNames;
import tech.manggocli.infrastructure.codegen.mode.AbstractSmokeTestGenerator;
import tech.manggocli.core.domain.api.ApiOperation;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static java.util.Optional.ofNullable;

/**
 * Shared base for Feign and FeignHc5 smoke test generators.
 * <p>
 * Both modes use a REST interface (adds restInterface import + ctx key),
 * support authHeaderName, and disable pure-body tests (Feign rejects null body at encode time).
 * <p>
 * Subclasses only need to provide the template name.
 */
public abstract class AbstractFeignSmokeTestGenerator extends AbstractSmokeTestGenerator {

    protected final String authHeaderName;

    protected AbstractFeignSmokeTestGenerator(
            final String basePackage,
            final Path projectRoot,
            final String clientName, final String authHeaderName,
            final UserNotifier notifier
    ) {
        super(basePackage, projectRoot, clientName, notifier);
        this.authHeaderName = authHeaderName != null ? authHeaderName : "Authorization";
    }

    @Override
    protected void collectModeSpecificImports(
            final String tag,
            final String pascal,
            final Set<String> imports
    ) {
        imports.add(PackageNames.importRest(basePackage, clientName, tag, pascal));
    }

    @Override
    protected void addModeSpecificContext(
            final Map<String, Object> ctx,
            final String tag,
            final String pascal
    ) {
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
