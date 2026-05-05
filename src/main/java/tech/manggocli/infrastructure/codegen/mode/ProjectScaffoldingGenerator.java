package tech.manggocli.infrastructure.codegen.mode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.manggocli.infrastructure.codegen.layout.MavenLayout;
import tech.manggocli.infrastructure.codegen.layout.Templates;
import tech.manggocli.infrastructure.codegen.shared.TemplateRenderer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Generic Maven project scaffolder. Replaces the per-mode POM generators.
 * Single point that knows: pom.xml, application.properties, Maven roots.
 * Mode-specific bits (POM template + artifact suffix/fallback) are constructor data.
 */
public final class ProjectScaffoldingGenerator {

    private static final Logger log = LoggerFactory.getLogger(ProjectScaffoldingGenerator.class);
    private static final String POM_XML = "pom.xml";
    private static final String APP_PROPERTIES = "src/main/resources/application.properties";

    private final Path projectRoot;
    private final String pomTemplate;
    private final String artifactSuffix;
    private final String artifactFallback;

    public ProjectScaffoldingGenerator(final Path projectRoot,
                                       final String pomTemplate,
                                       final String artifactSuffix,
                                       final String artifactFallback) {
        this.projectRoot = projectRoot;
        this.pomTemplate = pomTemplate;
        this.artifactSuffix = artifactSuffix;
        this.artifactFallback = artifactFallback;
    }

    public void generate(final String basePackage) throws IOException {
        writeIfMissing(projectRoot.resolve(POM_XML), renderPom(basePackage), POM_XML);
        MavenLayout.createMavenRoots(projectRoot);
        writeIfMissing(projectRoot.resolve(APP_PROPERTIES),
                TemplateRenderer.render(Templates.APPLICATION_PROPERTIES, Map.of()),
                APP_PROPERTIES);
    }

    private String renderPom(final String basePackage) {
        return TemplateRenderer.render(pomTemplate, Map.of(
                "groupId", MavenLayout.resolveGroupId(basePackage),
                "artifactId", MavenLayout.resolveArtifactId(basePackage, artifactSuffix, artifactFallback)));
    }

    private static void writeIfMissing(final Path file, final String content, final String label) throws IOException {
        if (!Files.exists(file)) {
            Files.writeString(file, content, StandardCharsets.UTF_8);
            log.info("  [OK] - [{}]", label);
        }
    }
}
