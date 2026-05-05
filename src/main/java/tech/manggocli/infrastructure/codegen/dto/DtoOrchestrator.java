package tech.manggocli.infrastructure.codegen.dto;

import tech.manggocli.core.application.port.UserNotifier;
import tech.manggocli.infrastructure.codegen.dto.strategy.MergedRequestObjectStrategy;
import tech.manggocli.infrastructure.codegen.dto.strategy.EnumDtoStrategy;
import tech.manggocli.infrastructure.codegen.dto.strategy.QueryMapDtoStrategy;
import tech.manggocli.infrastructure.codegen.dto.strategy.RegularDtoStrategy;
import tech.manggocli.core.domain.api.operation.ApiOperation;
import tech.manggocli.core.domain.api.schema.ApiSchema;
import tech.manggocli.core.domain.api.schema.ApiSchemaProperty;
import tech.manggocli.core.domain.service.SchemaGraphResolver;
import tech.manggocli.core.domain.api.schema.ArrayAliasNode;
import tech.manggocli.core.domain.api.schema.EnumNode;
import tech.manggocli.core.domain.api.schema.MissingNode;
import tech.manggocli.core.domain.api.schema.ObjectNode;
import tech.manggocli.core.domain.api.schema.SchemaNode;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static tech.manggocli.core.domain.service.ImportResolver.extractListItemType;
import static tech.manggocli.core.domain.service.ImportResolver.isJavaPrimitive;
import static tech.manggocli.core.domain.service.JavaNames.capitalize;
import static tech.manggocli.core.domain.service.JavaNames.toPascalCase;

/**
 * Orchestrates DTO generation for all operations within a tag.
 * <p>
 * Mirrors AbstractClientGenerator's Template Method pattern at the DTO level:
 * - generate() defines the fixed loop over operations
 * - Each case (consolidated request, queryMap, schema-based) delegates to a dedicated strategy
 * - Transitive schema resolution is handled once, centrally, via SchemaGraphResolver
 * <p>
 * The generated Set is owned here and threaded through DtoGenerationContext so all
 * strategies share the same deduplication state within one generation run.
 */
public final class DtoOrchestrator {

    private final String basePackage;
    private final Path projectRoot;
    private final String clientName;
    private final UserNotifier notifier;

    private final Set<String> generated = new HashSet<>();
    private Map<String, ApiSchema> schemas;

    private final MergedRequestObjectStrategy consolidatedStrategy = new MergedRequestObjectStrategy();
    private final QueryMapDtoStrategy queryMapStrategy = new QueryMapDtoStrategy();
    private final EnumDtoStrategy enumStrategy = new EnumDtoStrategy();
    private final RegularDtoStrategy regularStrategy = new RegularDtoStrategy();

    public DtoOrchestrator(final String basePackage, final Path projectRoot, final String clientName, final UserNotifier notifier) {
        this.basePackage = basePackage;
        this.projectRoot = projectRoot;
        this.clientName = clientName;
        this.notifier = notifier;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Entry point
    // ─────────────────────────────────────────────────────────────────────────

    public void generate(final String tag, final List<ApiOperation> operations, final Map<String, ApiSchema> schemas)
            throws IOException {
        this.schemas = schemas;

        for (final ApiOperation op : operations) {

            // ── Request ──────────────────────────────────────────────────────
            if (op.needsRequestObject()) {
                consolidatedStrategy.generate(requestCtx(tag, op), op.getRequestObjectClassName());
                generateBodySchemaDeps(tag, op);

            } else if (op.getRequestBodySchema() != null) {
                generateDtoAndDeps(tag, op.getRequestBodySchema(), true);

            } else if (op.isUseQueryMap() && !op.getQueryParams().isEmpty()) {
                queryMapStrategy.generate(requestCtx(tag, op), resolveQueryMapDtoName(tag, op));
            }

            // ── Response ─────────────────────────────────────────────────────
            if (op.getResponseSchema() != null) {
                generateDtoAndDeps(tag, op.getResponseSchema(), false);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Transitive schema resolution
    // ─────────────────────────────────────────────────────────────────────────

    private void generateDtoAndDeps(final String tag, final String dtoName, final boolean isRequest) throws IOException {
        final SchemaGraphResolver walker = new SchemaGraphResolver(schemas, generated);
        for (final SchemaNode node : walker.collect(dtoName, tag, isRequest)) {
            writeSchemaNode(tag, node);
        }
    }

    private void writeSchemaNode(final String tag, final SchemaNode node) throws IOException {
        final DtoGenerationContext ctx = buildContext(tag, null, node.isRequest());
        switch (node) {
            case EnumNode e        -> enumStrategy.generate(ctx, e.name());
            case ArrayAliasNode a  -> { /* item dependencies already collected by walker */ }
            case ObjectNode o      -> regularStrategy.generate(ctx, o.name());
            case MissingNode m     -> regularStrategy.generate(ctx, m.name());
        }
    }

    /**
     * For consolidated requests, the body schema is folded into the RequestObject.
     * Only the types of its properties may need standalone DTOs (transitive deps).
     */
    private void generateBodySchemaDeps(final String tag, final ApiOperation op) throws IOException {
        if (op.getRequestBodySchema() == null) return;
        final ApiSchema body = schemas.get(op.getRequestBodySchema());
        if (body == null) return;

        for (final ApiSchemaProperty prop : body.getProperties().values()) {
            generateDepsForType(tag, prop.getJavaType(), true);
        }
    }

    private void generateDepsForType(final String tag, final String javaType, final boolean isRequest) throws IOException {
        if (javaType == null || isJavaPrimitive(javaType)) return;
        if (schemas.containsKey(javaType)) generateDtoAndDeps(tag, javaType, isRequest);
        final String inner = extractListItemType(javaType);
        if (inner != null && !isJavaPrimitive(inner) && schemas.containsKey(inner))
            generateDtoAndDeps(tag, inner, isRequest);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private DtoGenerationContext requestCtx(final String tag, final ApiOperation op) {
        return buildContext(tag, op, true);
    }

    private DtoGenerationContext buildContext(
            final String tag,
            final ApiOperation op,
            final boolean isRequest
    ) {
        return new DtoGenerationContext(
                tag,
                op,
                schemas,
                basePackage,
                clientName,
                projectRoot,
                notifier,
                generated,
                isRequest
        );
    }

    private String resolveQueryMapDtoName(final String tag, final ApiOperation op) {
        return toPascalCase(tag) + capitalize(op.getMethodName()) + "Request";
    }
}
