package tech.manggocli.infrastructure.codegen.mode.feignhc5;

import tech.manggocli.infrastructure.codegen.layout.Templates;
import tech.manggocli.infrastructure.codegen.mode.ProjectScaffoldingGenerator;
import tech.manggocli.infrastructure.codegen.mode.feign.AbstractFeignClientGenerator;

import java.io.IOException;
import java.nio.file.Path;

public class FeignHc5ClientGenerator extends AbstractFeignClientGenerator {

    public FeignHc5ClientGenerator() {
        super(Templates.FEIGN_HC5_SMOKE_TEST, Templates.FEIGN_HC5_CLIENT_CONFIGURATION);
    }

    @Override
    public String modeName() {
        return "feign-hc5";
    }

    @Override
    protected void generateProjectStructure(final String basePackage, final Path projectRoot) throws IOException {
        new ProjectScaffoldingGenerator(projectRoot, Templates.FEIGN_HC5_POM, "-hc5-client", "feign-hc5-client")
                .generate(basePackage);
    }
}
