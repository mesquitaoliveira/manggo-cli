package tech.manggocli.infrastructure.codegen.mode.feignhc5;

import tech.manggocli.infrastructure.codegen.layout.Templates;
import tech.manggocli.infrastructure.codegen.mode.feign.AbstractFeignClientConfigurationGenerator;

import java.nio.file.Path;

public class FeignHc5ClientConfigurationGenerator extends AbstractFeignClientConfigurationGenerator {

    public FeignHc5ClientConfigurationGenerator(final String basePackage, final Path projectRoot) {
        super(basePackage, projectRoot);
    }

    @Override
    protected String getTemplateName() {
        return Templates.FEIGN_HC5_CLIENT_CONFIGURATION;
    }
}
