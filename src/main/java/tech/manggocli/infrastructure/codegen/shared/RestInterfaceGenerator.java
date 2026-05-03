package tech.manggocli.infrastructure.codegen.shared;

import tech.manggocli.infrastructure.codegen.layout.PackageNames;
import tech.manggocli.infrastructure.codegen.layout.Templates;
import tech.manggocli.core.domain.api.ApiOperation;
import tech.manggocli.core.domain.api.ApiSchema;
import tech.manggocli.core.application.port.UserNotifier;
import tech.manggocli.core.domain.service.OperationTypeResolver;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static tech.manggocli.infrastructure.codegen.layout.MavenLayout.mainSourcePath;
import static tech.manggocli.infrastructure.codegen.layout.MavenLayout.writeJavaFile;
import static tech.manggocli.core.domain.service.NamingService.toPascalCase;

public class RestInterfaceGenerator implements TagCodeGenerator {

    private final String basePackage;
    private final Path projectRoot;
    private final String clientName;
    private final UserNotifier notifier;

    public RestInterfaceGenerator(
            String basePackage,
            Path projectRoot,
            String clientName,
            UserNotifier notifier
    ) {
        this.basePackage = basePackage;
        this.projectRoot = projectRoot;
        this.clientName = clientName;
        this.notifier = notifier;
    }

    @Override
    public void generate(final String tag, final List<ApiOperation> operations, final Map<String, ApiSchema> schemas) throws IOException {
        final String pascal = toPascalCase(tag);
        final String className = pascal + "Rest";
        final String pkg = PackageNames.rest(basePackage, clientName, tag);
        final Path dir = mainSourcePath(projectRoot, pkg);

        writeJavaFile(dir, className, TemplateRenderer.render(Templates.REST_INTERFACE,
                buildContext(pkg, tag, pascal, className, operations, schemas)));
        notifier.notifyFileGenerated(className);
    }

    private Map<String, Object> buildContext(
            final String pkg,
            final String tag,
            final String pascal,
            final String className,
            final List<ApiOperation> operations,
            final Map<String, ApiSchema> schemas
    ) {
        final Set<String> imports = new LinkedHashSet<>();
        final List<Map<String, Object>> methods = new ArrayList<>();
        final ClientImportCollector importCollector = new ClientImportCollector(basePackage, clientName, schemas);

        for (final ApiOperation op : operations) {
            importCollector.collectFromOperation(op, tag, imports);
            importCollector.collectParameterImports(op, imports);
            methods.add(buildMethodContext(tag, op));
        }

        final Map<String, Object> ctx = new LinkedHashMap<>();
        ctx.put("package", pkg);
        ctx.put("className", className);
        ctx.put("imports", new ArrayList<>(imports));
        ctx.put("methods", methods);
        return ctx;
    }

    private Map<String, Object> buildMethodContext(final String tag, final ApiOperation op) {
        final String res = OperationTypeResolver.resolveResponseType(op);
        final Map<String, Object> m = new LinkedHashMap<>();
        m.put("summary", op.getSummary());
        m.put("returnType", Objects.requireNonNullElse(res, "void"));
        m.put("methodName", op.getMethodName());
        m.put("params", buildParams(tag, op));
        return m;
    }

    private String buildParams(final String tag, final ApiOperation op) {
        final String req = OperationTypeResolver.resolveRequestType(tag, op);
        final String base;
        if (req != null) {
            if (!op.needsRequestObject() && !op.getPathParams().isEmpty()) {
                final String pathPart = op.getPathParams().stream()
                        .map(p -> p.getJavaType() + " " + p.getCamelCaseName())
                        .collect(Collectors.joining(", "));
                base = pathPart + ", " + req + " request";
            } else {
                base = req + " request";
            }
        } else {
            base = op.getPathParams().stream()
                    .map(p -> p.getJavaType() + " " + p.getCamelCaseName())
                    .collect(Collectors.joining(", "));
        }
        if (op.getHeaderParams().isEmpty()) return base;
        final String headerPart = op.getHeaderParams().stream()
                .map(p -> p.getJavaType() + " " + p.getCamelCaseName())
                .collect(Collectors.joining(", "));
        return base.isEmpty() ? headerPart : base + ", " + headerPart;
    }
}
