package tech.manggocli.infrastructure.codegen.mode.feign;

import tech.manggocli.infrastructure.codegen.layout.PackageNames;
import tech.manggocli.infrastructure.codegen.layout.Templates;
import tech.manggocli.infrastructure.codegen.shared.TemplateRenderer;
import tech.manggocli.core.domain.client.ClientSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static tech.manggocli.infrastructure.codegen.layout.MavenLayout.mainSourcePath;
import static tech.manggocli.infrastructure.codegen.layout.MavenLayout.writeJavaFile;

public class FeignLoggerGenerator {

    private static final Logger log = LoggerFactory.getLogger(FeignLoggerGenerator.class);
    private final String basePackage;
    private final Path   outputDir;

    public FeignLoggerGenerator(final String basePackage, final Path outputDir) {
        this.basePackage = basePackage;
        this.outputDir   = outputDir;
    }

    public void generate(final ClientSpec spec) throws IOException {
        final String pascal    = spec.getClientNamePascal();
        final String className = pascal + "FeignLogger";
        final String pkg       = PackageNames.clientConfig(basePackage, spec.getClientName());
        final Path   dir       = mainSourcePath(outputDir, pkg);

        writeJavaFile(dir, className, TemplateRenderer.render(Templates.FEIGN_LOGGER, Map.of(
            "package",     pkg,
            "className",   className,
            "pascalUpper", pascal.toUpperCase()
        )));

        log.debug("Generated: {}/{}.java", pkg, className);
    }
}
