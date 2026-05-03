package tech.manggocli.infrastructure.codegen.mode.feign;

import tech.manggocli.core.application.port.UserNotifier;
import tech.manggocli.infrastructure.codegen.layout.PackageNames;
import tech.manggocli.infrastructure.codegen.layout.Templates;
import tech.manggocli.infrastructure.codegen.shared.ClientImportCollector;
import tech.manggocli.infrastructure.codegen.shared.TagCodeGenerator;
import tech.manggocli.infrastructure.codegen.shared.TemplateRenderer;
import tech.manggocli.infrastructure.codegen.mode.feign.method.FrameworkImportsResolver;
import tech.manggocli.infrastructure.codegen.mode.feign.method.HeadersBuilder;
import tech.manggocli.infrastructure.codegen.mode.feign.method.MethodContext;
import tech.manggocli.infrastructure.codegen.mode.feign.method.MethodParamsBuilder;
import tech.manggocli.infrastructure.codegen.mode.feign.method.RequestLineBuilder;
import tech.manggocli.core.domain.api.ApiOperation;
import tech.manggocli.core.domain.api.ApiSchema;
import tech.manggocli.core.domain.service.OperationTypeResolver;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static tech.manggocli.infrastructure.codegen.layout.MavenLayout.mainSourcePath;
import static tech.manggocli.core.domain.service.NamingService.toPascalCase;
import static tech.manggocli.infrastructure.codegen.layout.MavenLayout.writeJavaFile;

public class FeignRestClientGenerator implements TagCodeGenerator {

    private final String basePackage;
    private final Path projectRoot;
    private final String clientName;
    private final UserNotifier notifier;

    public FeignRestClientGenerator(
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
    public void generate(final String tag, final List<ApiOperation> operations,
                         final Map<String, ApiSchema> schemas) throws IOException {
        final String pascal = toPascalCase(tag);
        final String className = pascal + "RestClient";
        final String pkg = PackageNames.restClient(basePackage, clientName, tag);
        final Path dir = mainSourcePath(projectRoot, pkg);

        writeJavaFile(dir, className, TemplateRenderer.render(Templates.FEIGN_REST_CLIENT,
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
        final var frameworkImports = new FrameworkImportsResolver(operations);
        final ClientImportCollector importCollector = new ClientImportCollector(basePackage, clientName, schemas);
        final Set<String> dtoImports = new LinkedHashSet<>();
        final RequestLineBuilder requestLineBuilder = new RequestLineBuilder();
        final HeadersBuilder headersBuilder = new HeadersBuilder();
        final MethodParamsBuilder paramsBuilder = new MethodParamsBuilder();

        final var methodContexts = operations.stream()
                .map(op -> {
                    final String requestType = OperationTypeResolver.resolveRequestType(tag, op);
                    final String responseType = OperationTypeResolver.resolveResponseType(op);
                    importCollector.addRequestImport(requestType, tag, dtoImports);
                    importCollector.addResponseImports(responseType, tag, dtoImports);
                    importCollector.collectParameterImports(op, dtoImports);
                    return new MethodContext(
                            requestLineBuilder.build(op),
                            headersBuilder.build(op),
                            responseType != null && !responseType.equals("void"),
                            Objects.requireNonNullElse(responseType, "void"),
                            op.getMethodName(),
                            paramsBuilder.build(op, requestType)
                    );
                })
                .collect(Collectors.toList());

        final List<String> allImports = new ArrayList<>(frameworkImports.getFrameworkImports());
        allImports.addAll(dtoImports);
        allImports.add(PackageNames.importRest(basePackage, clientName, tag, pascal));

        final Map<String, Object> ctx = new LinkedHashMap<>();
        ctx.put("package", pkg);
        ctx.put("className", className);
        ctx.put("parentInterface", pascal + "Rest");
        ctx.put("imports", allImports);
        ctx.put("methods", toMaps(methodContexts));
        return ctx;
    }

    private List<Map<String, Object>> toMaps(final List<MethodContext> contexts) {
        return contexts.stream().map(c -> {
            final Map<String, Object> m = new LinkedHashMap<>();
            m.put("requestLine", c.requestLine());
            m.put("headers", c.headers());
            m.put("hasReturn", c.hasReturn());
            m.put("returnType", c.returnType());
            m.put("methodName", c.methodName());
            m.put("params", c.params());
            return m;
        }).collect(Collectors.toList());
    }
}
