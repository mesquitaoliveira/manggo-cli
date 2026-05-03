package tech.manggocli.infrastructure.codegen.mode.httpclient;

import tech.manggocli.core.application.port.UserNotifier;
import tech.manggocli.infrastructure.codegen.shared.RestInterfaceGenerator;
import tech.manggocli.infrastructure.codegen.shared.TagCodeGenerator;
import tech.manggocli.infrastructure.codegen.mode.AbstractClientGenerator;
import tech.manggocli.infrastructure.codegen.shared.ApiModelGenerator;
import tech.manggocli.infrastructure.codegen.shared.DtoGenerator;
import tech.manggocli.core.domain.api.ClientSpec;
import tech.manggocli.core.domain.api.ParsedApi;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Generator for native HTTP clients (Java HTTP Client).
 * Implements mode-specific hooks for "native":
 * - POM with native dependencies
 * - DTO, Interface, RestClient generators for native mode
 * - Native-specific configuration
 */
public class NativeClientGenerator extends AbstractClientGenerator {

    @Override
    public String modeName() {
        return "native";
    }

    @Override
    protected void generateProjectStructure(final String basePackage, final Path projectRoot) throws IOException {
        new NativeProjectPomGenerator(projectRoot).generate(basePackage);
    }

    @Override
    protected void generateSharedClasses(
            final String basePackage,
            final Path projectRoot,
            final ClientSpec spec,
            final UserNotifier notifier) throws IOException {

        new ApiModelGenerator(basePackage, projectRoot).generate();
        new NativeBaseRestClientGenerator(basePackage, projectRoot).generate();
    }

    @Override
    protected List<TagCodeGenerator> createTagGenerators(
            final String basePackage,
            final Path projectRoot,
            final ClientSpec spec,
            final ParsedApi api,
            final UserNotifier notifier) {

        return List.of(
                new DtoGenerator(basePackage, projectRoot, spec.getClientName(), notifier),
                new RestInterfaceGenerator(basePackage, projectRoot, spec.getClientName(), notifier),
                new NativeRestClientGenerator(basePackage, projectRoot, spec.getClientName(), api.getAuthHeaderName(), notifier),
                new NativeSmokeTestGenerator(basePackage, projectRoot, spec.getClientName(), notifier)
        );
    }

    @Override
    protected void generateClientConfiguration(
            final ParsedApi api,
            final ClientSpec spec,
            final String basePackage,
            final Path projectRoot,
            final UserNotifier notifier) throws IOException {

        new NativeClientConfigurationGenerator(basePackage, projectRoot, notifier).generate(api, spec);
        notifier.notifyFileGenerated(spec.getClientNamePascal() + "ClientConfiguration");
    }
}
