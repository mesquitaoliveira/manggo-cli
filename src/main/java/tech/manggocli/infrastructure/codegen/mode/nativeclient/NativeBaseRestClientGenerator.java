package tech.manggocli.infrastructure.codegen.mode.nativeclient;

import tech.manggocli.infrastructure.codegen.layout.PackageNames;
import tech.manggocli.infrastructure.codegen.layout.Templates;
import tech.manggocli.infrastructure.codegen.shared.TemplateRenderer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static tech.manggocli.infrastructure.codegen.layout.MavenLayout.mainSourcePath;
import static tech.manggocli.infrastructure.codegen.layout.MavenLayout.writeJavaFile;

public class NativeBaseRestClientGenerator {

    private final String basePackage;
    private final Path   projectRoot;

    public NativeBaseRestClientGenerator(final String basePackage, final Path projectRoot) {
        this.basePackage = basePackage;
        this.projectRoot = projectRoot;
    }

    public void generate() throws IOException {
        final String pkg = PackageNames.common(basePackage);
        final Path   dir = mainSourcePath(projectRoot, pkg);
        writeJavaFile(dir, "BaseRestClient",
            TemplateRenderer.render(Templates.NATIVE_BASE_REST_CLIENT, Map.of("package", pkg)));
    }
}
