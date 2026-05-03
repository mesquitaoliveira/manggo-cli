package tech.manggocli.infrastructure.codegen.dto;

import java.io.IOException;

public interface DtoGenerationStrategy {
    void generate(DtoGenerationContext ctx, String dtoName) throws IOException;
}
