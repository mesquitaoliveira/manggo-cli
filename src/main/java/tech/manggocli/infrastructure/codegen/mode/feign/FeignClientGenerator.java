package tech.manggocli.infrastructure.codegen.mode.feign;

import tech.manggocli.infrastructure.codegen.layout.Templates;
import tech.manggocli.infrastructure.codegen.mode.ProjectScaffoldingGenerator;

import java.io.IOException;
import java.nio.file.Path;

public class FeignClientGenerator extends AbstractFeignClientGenerator {

    public FeignClientGenerator() {
        super(Templates.FEIGN_SMOKE_TEST, Templates.FEIGN_CLIENT_CONFIGURATION);
    }

    @Override
    public String modeName() {
        return "feign";
    }

    @Override
    protected void generateProjectStructure(final String basePackage, final Path projectRoot) throws IOException {
        new ProjectScaffoldingGenerator(projectRoot, Templates.FEIGN_POM, "-client", "feign-client")
                .generate(basePackage);
    }
}
