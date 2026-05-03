package tech.manggocli.infrastructure.codegen.mode.httpclient;

import tech.manggocli.infrastructure.codegen.layout.MavenLayout;
import tech.manggocli.infrastructure.codegen.layout.Templates;
import tech.manggocli.infrastructure.codegen.shared.TemplateRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class NativeProjectPomGenerator {

    private static final Logger log = LoggerFactory.getLogger(NativeProjectPomGenerator.class);
    private static final String MAIN_RESOURCES_APPLICATION_PROPERTIES = "src/main/resources/application.properties";

    private final Path projectRoot;

    public NativeProjectPomGenerator(final Path projectRoot) {
        this.projectRoot = projectRoot;
    }

    public void generate(final String basePackage) throws IOException {
        final Path pomFile = projectRoot.resolve("pom.xml");
        if (!Files.exists(pomFile)) {
            Files.writeString(pomFile, buildPom(basePackage), StandardCharsets.UTF_8);
            log.info("  [OK] pom.xml");
        }
        MavenLayout.createMavenRoots(projectRoot);

        final Path appProps = projectRoot.resolve(MAIN_RESOURCES_APPLICATION_PROPERTIES);
        if (!Files.exists(appProps)) {
            Files.writeString(appProps,
                TemplateRenderer.render(Templates.APPLICATION_PROPERTIES, Map.of()),
                StandardCharsets.UTF_8);
        }
    }

    private String buildPom(final String basePackage) {
        return TemplateRenderer.render(Templates.NATIVE_POM, Map.of(
                "groupId",    MavenLayout.resolveGroupId(basePackage),
                "artifactId", MavenLayout.resolveArtifactId(basePackage, "-native-client", "native-client")));
    }
}
