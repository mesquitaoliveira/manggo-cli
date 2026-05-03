package tech.manggocli.infrastructure.codegen.mode;

import tech.manggocli.core.application.port.UserNotifier;
import tech.manggocli.core.domain.api.ApiOperation;
import tech.manggocli.core.domain.api.ApiSchema;
import tech.manggocli.core.domain.service.OperationTypeResolver;
import tech.manggocli.infrastructure.codegen.layout.PackageNames;
import tech.manggocli.infrastructure.codegen.mode.nullarg.BodyNullArgHandler;
import tech.manggocli.infrastructure.codegen.mode.nullarg.IndividualNullArgHandler;
import tech.manggocli.infrastructure.codegen.mode.nullarg.NullArgHandler;
import tech.manggocli.infrastructure.codegen.mode.nullarg.QueryMapNullArgHandler;
import tech.manggocli.infrastructure.codegen.mode.nullarg.RequestObjectNullArgHandler;
import tech.manggocli.infrastructure.codegen.shared.ClientImportCollector;
import tech.manggocli.infrastructure.codegen.shared.TagCodeGenerator;
import tech.manggocli.infrastructure.codegen.shared.TemplateRenderer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static tech.manggocli.core.domain.service.NamingService.toPascalCase;
import static tech.manggocli.infrastructure.codegen.layout.MavenLayout.testSourcePath;
import static tech.manggocli.infrastructure.codegen.layout.MavenLayout.writeJavaFile;

/**
 * Template Method base for smoke test generators.
 * <p>
 * Common logic (shared by all modes): package path resolution, import collection,
 * method context building, null-arg generation.
 * <p>
 * Hooks for mode-specific variation:
 * - templateName()              — Mustache template path
 * - collectModeSpecificImports  — e.g. Feign adds restInterface import
 * - addModeSpecificContext      — e.g. Feign adds authHeaderName, restInterface keys
 * - isDisabled(op)              — e.g. Feign disables pure-body tests (null body encoder)
 */
public abstract class AbstractSmokeTestGenerator implements TagCodeGenerator {

    private static final NullArgHandler NULL_ARG_CHAIN = NullArgHandler.link(
            new RequestObjectNullArgHandler(),
            new QueryMapNullArgHandler(),
            new BodyNullArgHandler(),
            new IndividualNullArgHandler()
    );

    protected final String basePackage;
    protected final Path projectRoot;
    protected final String clientName;
    protected final UserNotifier notifier;

    protected AbstractSmokeTestGenerator(final String basePackage, final Path projectRoot,
                                         final String clientName, final UserNotifier notifier) {
        this.basePackage = basePackage;
        this.projectRoot = projectRoot;
        this.clientName = clientName;
        this.notifier = notifier;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Template Method — fixed generation sequence
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public final void generate(final String tag, final List<ApiOperation> operations,
                               final Map<String, ApiSchema> schemas) throws IOException {
        final String pascal = toPascalCase(tag);
        final String clientClass = pascal + "RestClient";
        final String className = pascal + "RestClientSmokeTest";
        final String pkg = PackageNames.restClient(basePackage, clientName, tag);
        final Path dir = testSourcePath(projectRoot, pkg);

        final Set<String> imports = new LinkedHashSet<>();
        collectModeSpecificImports(tag, pascal, imports);

        final ClientImportCollector importCollector = new ClientImportCollector(basePackage, clientName, schemas);
        final List<Map<String, Object>> methods = new ArrayList<>();
        for (final ApiOperation op : operations) {
            final String req = OperationTypeResolver.resolveRequestType(tag, op);
            final String res = op.getResponseSchema();
            importCollector.addRequestImport(req, tag, imports);
            importCollector.addResponseImports(res, tag, imports);
            methods.add(buildMethodContext(op, req, res));
        }

        final Map<String, Object> ctx = new LinkedHashMap<>();
        ctx.put("package", pkg);
        ctx.put("className", className);
        ctx.put("clientClassName", clientClass);
        ctx.put("imports", new ArrayList<>(imports));
        ctx.put("methods", methods);
        addModeSpecificContext(ctx, tag, pascal);

        writeJavaFile(dir, className, TemplateRenderer.render(templateName(), ctx));
        notifier.notifyFileGenerated(className);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Hooks
    // ─────────────────────────────────────────────────────────────────────────

    protected abstract String templateName();

    /**
     * Add mode-specific imports before the operation loop (e.g. restInterface import).
     */
    protected void collectModeSpecificImports(final String tag, final String pascal, final Set<String> imports) {
    }

    /**
     * Add mode-specific context keys (e.g. authHeaderName, restInterface).
     */
    protected void addModeSpecificContext(final Map<String, Object> ctx, final String tag, final String pascal) {
    }

    /**
     * Whether this operation's smoke test should be disabled (e.g. Feign rejects null body).
     */
    protected boolean isDisabled(final ApiOperation op) {
        return false;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Common private logic
    // ─────────────────────────────────────────────────────────────────────────

    private Map<String, Object> buildMethodContext(final ApiOperation op, final String req, final String res) {
        final String ret = Objects.requireNonNullElse(res, "void");
        final boolean hasReturn = !ret.equals("void");

        final List<String> args = new ArrayList<>(NULL_ARG_CHAIN.build(op, req));
        op.getHeaderParams().forEach(p -> args.add("null"));

        final Map<String, Object> m = new LinkedHashMap<>();
        m.put("methodName", op.getMethodName());
        m.put("returnType", ret);
        m.put("hasReturn", hasReturn);
        m.put("callArgs", String.join(", ", args));
        m.put("disabled", isDisabled(op));
        return m;
    }
}
