package tech.manggocli.infrastructure.codegen.mode.feign;

import tech.manggocli.infrastructure.codegen.layout.Templates;

import java.nio.file.Path;

public class FeignClientConfigurationGenerator extends AbstractFeignClientConfigurationGenerator {

    public FeignClientConfigurationGenerator(final String basePackage, final Path projectRoot) {
        super(basePackage, projectRoot);
    }

    @Override
    protected String getTemplateName() {
        return Templates.FEIGN_CLIENT_CONFIGURATION;
    }
}
