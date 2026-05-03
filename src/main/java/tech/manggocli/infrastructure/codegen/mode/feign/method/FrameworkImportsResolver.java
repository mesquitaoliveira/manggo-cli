package tech.manggocli.infrastructure.codegen.mode.feign.method;

import tech.manggocli.core.domain.api.ApiOperation;

import java.util.ArrayList;
import java.util.List;

public class FrameworkImportsResolver {

    private static final String IMPORT_QUERY_MAP = "import feign.QueryMap;";
    private static final String IMPORT_PARAM     = "import feign.Param;";

    private final boolean usesQueryMap;
    private final boolean usesIndividualParams;

    public FrameworkImportsResolver(final List<ApiOperation> operations) {
        this.usesQueryMap = operations.stream()
            .anyMatch(ApiOperation::isUseQueryMap);

        this.usesIndividualParams = operations.stream()
            .anyMatch(this::hasIndividualParameters);
    }

    public List<String> getFrameworkImports() {
        final List<String> imports = new ArrayList<>();
        if (usesQueryMap)          imports.add(IMPORT_QUERY_MAP);
        if (usesIndividualParams)  imports.add(IMPORT_PARAM);
        return imports;
    }

    private boolean hasIndividualParameters(final ApiOperation operation) {
        return !operation.getHeaderParams().isEmpty()
            || (!operation.needsRequestObject() && (
                !operation.getPathParams().isEmpty()
                || (!operation.isUseQueryMap() && !operation.getQueryParams().isEmpty())
            ));
    }
}
