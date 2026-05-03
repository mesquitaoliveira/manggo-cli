package tech.manggocli.infrastructure.codegen.mode.feignhc5;

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

public class FeignHc5ProjectPomGenerator {

    private static final Logger log = LoggerFactory.getLogger(FeignHc5ProjectPomGenerator.class);

    private final Path projectRoot;

    public FeignHc5ProjectPomGenerator(final Path projectRoot) {
        this.projectRoot = projectRoot;
    }

    public void generate(final String basePackage) throws IOException {
        final Path pomFile = projectRoot.resolve("pom.xml");

        if (!Files.exists(pomFile)) {
            Files.writeString(pomFile, buildPom(basePackage), StandardCharsets.UTF_8);
            log.info("  [OK] pom.xml");
        }

        MavenLayout.createMavenRoots(projectRoot);

        final Path appProps = projectRoot.resolve("src/main/resources/application.properties");
        if (!Files.exists(appProps)) {
            Files.writeString(appProps,
                TemplateRenderer.render(Templates.APPLICATION_PROPERTIES, Map.of()),
                StandardCharsets.UTF_8);
        }
    }

    private String buildPom(final String basePackage) {
        return TemplateRenderer.render(Templates.FEIGN_HC5_POM, Map.of(
                "groupId",    MavenLayout.resolveGroupId(basePackage),
                "artifactId", MavenLayout.resolveArtifactId(basePackage, "-hc5-client", "feign-hc5-client")));
    }
}
