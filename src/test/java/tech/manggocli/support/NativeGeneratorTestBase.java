package tech.manggocli.support;

import tech.manggocli.core.application.port.UserNotifier;
import tech.manggocli.infrastructure.codegen.mode.nativeclient.NativeClientGenerator;
import tech.manggocli.core.domain.client.ClientSpec;
import tech.manggocli.infrastructure.parser.OpenApiParser;
import org.junit.jupiter.api.io.TempDir;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public abstract class NativeGeneratorTestBase {

    @TempDir protected Path projectRoot;

    protected final OpenApiParser parser = new OpenApiParser();

    protected Path javaRoot() { return projectRoot.resolve("src/main/java"); }

    protected Path testRoot() { return projectRoot.resolve("src/test/java"); }

    protected void assertTestJava(String rel) {
        Path f = testRoot().resolve(rel.replace("/", java.io.File.separator));
        assertTrue(Files.exists(f), "Not generated: src/test/java/" + rel);
    }

    protected String readTestJava(String rel) throws Exception {
        return Files.readString(testRoot().resolve(rel.replace("/", java.io.File.separator)));
    }

    protected String specPath(String filename) throws Exception {
        URL res = getClass().getClassLoader().getResource(filename);
        assertNotNull(res, filename + " not found in test/resources");
        return Paths.get(res.toURI()).toAbsolutePath().toString();
    }

    protected void generate(String specFile, String clientName) throws Exception {
        String path = specPath(specFile);
        new NativeClientGenerator()
            .generate(parser.parse(path), new ClientSpec(clientName, path), "com.example", projectRoot, UserNotifier.NOOP);
    }

    protected void assertJava(String rel) {
        Path f = javaRoot().resolve(rel.replace("/", java.io.File.separator));
        assertTrue(Files.exists(f), "Não gerado: src/main/java/" + rel);
    }

    protected String readJava(String rel) throws Exception {
        return Files.readString(javaRoot().resolve(rel.replace("/", java.io.File.separator)));
    }
}
