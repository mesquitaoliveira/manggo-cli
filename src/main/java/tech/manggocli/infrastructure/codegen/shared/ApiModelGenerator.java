package tech.manggocli.infrastructure.codegen.shared;

import tech.manggocli.infrastructure.codegen.layout.PackageNames;
import tech.manggocli.infrastructure.codegen.layout.Templates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static tech.manggocli.infrastructure.codegen.layout.MavenLayout.mainSourcePath;
import static tech.manggocli.infrastructure.codegen.layout.MavenLayout.writeJavaFile;
import static tech.manggocli.infrastructure.codegen.shared.TemplateRenderer.render;

public class ApiModelGenerator {

    private static final Logger log = LoggerFactory.getLogger(ApiModelGenerator.class);
    private final String basePackage;
    private final Path   outputDir;

    public ApiModelGenerator(final String basePackage, final Path outputDir) {
        this.basePackage = basePackage;
        this.outputDir   = outputDir;
    }

    public void generate() throws IOException {
        final String pkg = PackageNames.common(basePackage);
        final Path   dir = mainSourcePath(outputDir, pkg);

        writeJavaFile(dir, "ApiModel",
            render(Templates.API_MODEL, Map.of("package", pkg)));

        log.debug("Generated: {}/ApiModel.java", dir);
    }
}
