package tech.manggocli.infrastructure.codegen.shared;

import tech.manggocli.core.domain.api.ApiOperation;
import tech.manggocli.core.domain.api.ApiSchema;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface TagCodeGenerator {
    void generate(String tag, List<ApiOperation> operations, Map<String, ApiSchema> schemas) throws IOException;
}
