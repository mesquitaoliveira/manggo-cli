package tech.manggocli.infrastructure.codegen.layout;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.writeString;

/**
 * Maven standard directory layout constants and path resolution helpers.
 * Centralizes the MAIN_SOURCES / TEST_SOURCES roots previously hardcoded
 * in GeneratorUtils (only MAVEN_SOURCES_ROOT existed — no test root sibling).
 */
public final class MavenLayout {

    public static final String MAIN_SOURCES   = "src/main/java";
    public static final String TEST_SOURCES   = "src/test/java";
    public static final String MAIN_RESOURCES = "src/main/resources";
    public static final String TEST_RESOURCES = "src/test/resources";
    private static final Pattern PACKAGE_SEPARATOR = Pattern.compile("\\.");

    private MavenLayout() {
    }

    private static String[] splitPackage(final String pkg) {
        return PACKAGE_SEPARATOR.split(pkg);
    }

    /**
     * Resolves a package to its Maven main-sources directory.
     */
    public static Path mainSourcePath(final Path projectRoot, final String basePackage, final String... segments) {
        return resolvePath(projectRoot, MAIN_SOURCES, basePackage, segments);
    }

    /**
     * Resolves a package to its Maven test-sources directory.
     */
    public static Path testSourcePath(final Path projectRoot, final String basePackage, final String... segments) {
        return resolvePath(projectRoot, TEST_SOURCES, basePackage, segments);
    }

    public static void writeJavaFile(final Path dir, final String className, final String content) throws IOException {
        createDirectories(dir);
        writeString(dir.resolve(className + ".java"), content, StandardCharsets.UTF_8);
    }

    public static void createMavenRoots(final Path projectRoot) throws IOException {
        createDirectories(projectRoot.resolve(MAIN_SOURCES));
        createDirectories(projectRoot.resolve(MAIN_RESOURCES));
        createDirectories(projectRoot.resolve(TEST_SOURCES));
        createDirectories(projectRoot.resolve(TEST_RESOURCES));
    }

    public static String resolveGroupId(final String basePackage) {
        final var parts = splitPackage(basePackage);
        return parts.length >= 2 ? parts[0] + "." + parts[1] : basePackage;
    }

    public static String resolveArtifactId(
            final String basePackage,
            final String suffix,
            final String fallback
    ) {
        final var parts = splitPackage(basePackage);
        return parts.length >= 3 ? parts[parts.length - 1] + suffix : fallback;
    }

    private static Path resolvePath(
            final Path root,
            final String sourceRoot,
            final String basePackage,
            final String[] segments
    ) {
        Path path = root.resolve(sourceRoot);
        for (final String part : PACKAGE_SEPARATOR.split(basePackage)) path = path.resolve(part);
        for (final String seg : segments) path = path.resolve(seg);
        return path;
    }
}
