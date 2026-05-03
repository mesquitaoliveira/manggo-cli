package tech.manggocli.infrastructure.codegen.mode.feign;

import tech.manggocli.infrastructure.codegen.layout.PackageNames;
import tech.manggocli.infrastructure.codegen.layout.Templates;
import tech.manggocli.infrastructure.codegen.shared.TemplateRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static tech.manggocli.infrastructure.codegen.layout.MavenLayout.mainSourcePath;
import static tech.manggocli.infrastructure.codegen.layout.MavenLayout.writeJavaFile;

public class FeignConfigBeanGenerator {

    private static final Logger log = LoggerFactory.getLogger(FeignConfigBeanGenerator.class);
    private static final String FEIGN_CLIENT_CONFIG = "FeignClientConfig";

    private final String basePackage;
    private final Path   outputDir;

    public FeignConfigBeanGenerator(final String basePackage, final Path outputDir) {
        this.basePackage = basePackage;
        this.outputDir   = outputDir;
    }

    public void generate() throws IOException {
        final String pkg = PackageNames.config(basePackage);
        final Path   dir = mainSourcePath(outputDir, pkg);

        writeJavaFile(dir, FEIGN_CLIENT_CONFIG,
            TemplateRenderer.render(Templates.FEIGN_CLIENT_CONFIG, Map.of("package", pkg)));

        log.debug("Generated: {}/FeignClientConfig.java", dir);
    }
}
