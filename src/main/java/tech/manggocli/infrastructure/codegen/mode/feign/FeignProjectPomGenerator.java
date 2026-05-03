package tech.manggocli.infrastructure.codegen.mode.feign;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.manggocli.infrastructure.codegen.layout.Templates;
import tech.manggocli.infrastructure.codegen.shared.TemplateRenderer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static tech.manggocli.infrastructure.codegen.layout.MavenLayout.createMavenRoots;
import static tech.manggocli.infrastructure.codegen.layout.MavenLayout.resolveArtifactId;
import static tech.manggocli.infrastructure.codegen.layout.MavenLayout.resolveGroupId;

public class FeignProjectPomGenerator {

    private static final Logger log = LoggerFactory.getLogger(FeignProjectPomGenerator.class);
    private static final String SRC_MAIN_RESOURCES_APPLICATION_PROPERTIES = "src/main/resources/application.properties";
    private static final String POM_XML = "pom.xml";

    private final Path projectRoot;

    public FeignProjectPomGenerator(final Path projectRoot) {
        this.projectRoot = projectRoot;
    }

    public void generate(final String basePackage) throws IOException {
        final Path pomFile = projectRoot.resolve(POM_XML);

        if (!Files.exists(pomFile)) {
            Files.writeString(pomFile, buildPom(basePackage), StandardCharsets.UTF_8);
            log.info("  [OK] - [{}]", POM_XML);
        }

        createMavenRoots(projectRoot);

        final Path appProps = projectRoot.resolve(SRC_MAIN_RESOURCES_APPLICATION_PROPERTIES);
        if (!Files.exists(appProps)) {
            Files.writeString(appProps, buildApplicationProperties(), StandardCharsets.UTF_8);
            log.info("  [OK] - [{}]", SRC_MAIN_RESOURCES_APPLICATION_PROPERTIES);
        }
    }

    private String buildPom(final String basePackage) {
        return TemplateRenderer.render(Templates.FEIGN_POM, Map.of(
                "groupId",    resolveGroupId(basePackage),
                "artifactId", resolveArtifactId(basePackage, "-client", "feign-client")));
    }

    private String buildApplicationProperties() {
        return TemplateRenderer.render(Templates.APPLICATION_PROPERTIES, Map.of());
    }
}
