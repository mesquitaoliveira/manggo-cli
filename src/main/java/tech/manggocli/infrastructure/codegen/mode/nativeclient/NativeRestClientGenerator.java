package tech.manggocli.infrastructure.codegen.mode.nativeclient;

import tech.manggocli.core.application.port.UserNotifier;
import tech.manggocli.core.domain.api.operation.ApiOperation;
import tech.manggocli.core.domain.api.operation.ApiParameter;
import tech.manggocli.core.domain.api.schema.ApiSchema;
import tech.manggocli.core.domain.service.OperationTypeResolver;
import tech.manggocli.infrastructure.codegen.layout.PackageNames;
import tech.manggocli.infrastructure.codegen.layout.Templates;
import tech.manggocli.infrastructure.codegen.mode.MethodParamHandler;
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
import java.util.stream.IntStream;

import static tech.manggocli.core.domain.service.ImportResolver.extractListItemType;
import static tech.manggocli.core.domain.service.JavaNames.capitalize;
import static tech.manggocli.core.domain.service.JavaNames.toPascalCase;
import static tech.manggocli.infrastructure.codegen.layout.MavenLayout.mainSourcePath;
import static tech.manggocli.infrastructure.codegen.layout.MavenLayout.writeJavaFile;

public class NativeRestClientGenerator implements TagCodeGenerator {

    private final String basePackage;
    private final Path projectRoot;
    private final String clientName;
    private final String authHeaderName;
    private final UserNotifier notifier;

    public NativeRestClientGenerator(
            String basePackage,
            Path projectRoot,
            String clientName,
            String authHeaderName,
            UserNotifier notifier
    ) {
        this.basePackage = basePackage;
        this.projectRoot = projectRoot;
        this.clientName = clientName;
        this.authHeaderName = Objects.requireNonNullElse(authHeaderName, "Authorization");
        this.notifier = notifier;
    }

    private static final MethodParamHandler CHAIN = MethodParamHandler.link(
            new NativeObjectParamHandler(),
            new NativeBodyWithPathParamHandler(),
            new NativeIndividualParamsHandler()
    );


    @Override
    public void generate(final String tag, final List<ApiOperation> operations,
                         final Map<String, ApiSchema> schemas) throws IOException {
        final String pascal = toPascalCase(tag);
        final String className = pascal + "RestClient";
        final String pkg = PackageNames.restClient(basePackage, clientName, tag);
        final Path dir = mainSourcePath(projectRoot, pkg);

        writeJavaFile(dir, className, TemplateRenderer.render(Templates.NATIVE_REST_CLIENT,
                buildContext(pkg, tag, pascal, className, operations, schemas)));
        notifier.notifyFileGenerated(className);
    }

    // ── Context builder ───────────────────────────────────────────────────────

    private Map<String, Object> buildContext(
            final String pkg, final String tag,
            final String pascal,
            final String className,
            final List<ApiOperation> operations,
            final Map<String, ApiSchema> schemas
    ) {
        final Set<String> imports = new LinkedHashSet<>();
        imports.add(PackageNames.importRest(basePackage, clientName, tag, pascal));

        final ClientImportCollector importCollector = new ClientImportCollector(basePackage, clientName, schemas);
        final List<Map<String, Object>> methods = new ArrayList<>();
        for (final ApiOperation op : operations) {
            final String req = resolveRequestType(tag, op);
            final String res = resolveResponseType(op);
            importCollector.collectFromOperation(op, tag, imports);
            methods.add(buildMethodContext(op, req, res));
        }

        final Map<String, Object> ctx = new LinkedHashMap<>();
        ctx.put("package", pkg);
        ctx.put("commonPackage", PackageNames.common(basePackage));
        ctx.put("className", className);
        ctx.put("parentInterface", pascal + "Rest");
        ctx.put("authHeaderName", authHeaderName);
        ctx.put("imports", new ArrayList<>(imports));
        ctx.put("methods", methods);
        return ctx;
    }

    private Map<String, Object> buildMethodContext(final ApiOperation op, final String req, final String res) {
        final String ret = Objects.requireNonNullElse(res, "void");
        final boolean hasReturn = !ret.equals("void");
        final boolean isList = ret.startsWith("List<");
        final String listInner = isList ? extractListItemType(ret) : null;
        final boolean hasBody = op.getRequestBodySchema() != null;
        final boolean needsObj = op.needsRequestObject();
        final boolean useQMap = op.isUseQueryMap();

        final boolean isQueryMap = useQMap || needsObj;
        final List<Map<String, Object>> pathParams = buildPathParamMaps(op.getPathParams(), needsObj);
        final List<Map<String, Object>> queryParams = isQueryMap ? List.of() : buildQueryParamMaps(op.getQueryParams());
        final List<Map<String, Object>> headerParams = buildHeaderParamMaps(op.getHeaderParams());

        final Map<String, Object> m = new LinkedHashMap<>();
        m.put("methodName", op.getMethodName());
        m.put("returnType", ret);
        m.put("hasReturn", hasReturn);
        m.put("isList", isList);
        m.put("listInnerType", listInner);
        m.put("params", buildParamsList(op, req));
        m.put("path", op.getPath());
        m.put("httpMethod", op.getHttpMethod());
        m.put("hasBody", hasBody);
        m.put("pathParams", pathParams);
        m.put("hasPathParams", !pathParams.isEmpty());
        m.put("queryParams", queryParams);
        m.put("hasQueryParams", !queryParams.isEmpty());
        m.put("isQueryMap", isQueryMap && !op.getQueryParams().isEmpty());
        m.put("headerParams", headerParams);
        m.put("hasHeaderParams", !headerParams.isEmpty());
        return m;
    }

    private String buildParamsList(final ApiOperation op, final String requestType) {
        final List<String> parts = new ArrayList<>(CHAIN.build(op, requestType));
        op.getHeaderParams().stream()
                .map(p -> p.getJavaType() + " " + p.getCamelCaseName())
                .forEach(parts::add);
        return String.join(", ", parts);
    }

    private static List<Map<String, Object>> buildPathParamMaps(final List<ApiParameter> params, final boolean needsObj) {
        return params.stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("bracketName", "{" + p.getName() + "}");
            m.put("accessExpr", needsObj
                    ? "request.get" + capitalize(p.getCamelCaseName()) + "()"
                    : p.getCamelCaseName());
            return m;
        }).toList();
    }

    private static List<Map<String, Object>> buildQueryParamMaps(final List<ApiParameter> params) {
        return IntStream.range(0, params.size()).mapToObj(i -> {
            final ApiParameter p = params.get(i);
            final Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", p.getName());
            m.put("accessExpr", p.getCamelCaseName());
            m.put("first", i == 0);
            return m;
        }).toList();
    }

    private static List<Map<String, Object>> buildHeaderParamMaps(final List<ApiParameter> params) {
        return params.stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("headerName", p.getName());
            m.put("accessExpr", p.getCamelCaseName());
            return m;
        }).toList();
    }

    private String resolveRequestType(final String tag, final ApiOperation op) {
        return OperationTypeResolver.resolveRequestType(tag, op);
    }

    private String resolveResponseType(final ApiOperation op) {
        return OperationTypeResolver.resolveResponseType(op);
    }

}
