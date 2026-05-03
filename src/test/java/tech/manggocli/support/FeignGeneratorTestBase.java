package tech.manggocli.support;

import tech.manggocli.core.application.port.UserNotifier;
import tech.manggocli.infrastructure.codegen.mode.feign.FeignClientGenerator;
import tech.manggocli.core.domain.api.ApiOperation;
import tech.manggocli.core.domain.api.ClientSpec;
import tech.manggocli.core.domain.api.ParsedApi;
import tech.manggocli.infrastructure.parser.OpenApiParser;
import org.junit.jupiter.api.io.TempDir;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class FeignGeneratorTestBase {

    @TempDir protected Path projectRoot;

    protected final OpenApiParser parser = new OpenApiParser();

    protected Path javaRoot() { return projectRoot.resolve("src/main/java"); }

    protected String specPath(String filename) throws Exception {
        URL res = getClass().getClassLoader().getResource(filename);
        assertNotNull(res, filename + " not found in test/resources");
        return Paths.get(res.toURI()).toAbsolutePath().toString();
    }

    protected void generate(String specFile, String clientName) throws Exception {
        String path = specPath(specFile);
        new FeignClientGenerator()
            .generate(parser.parse(path), new ClientSpec(clientName, path), "com.example", projectRoot, UserNotifier.NOOP);
    }

    protected void assertJava(String rel) {
        Path f = javaRoot().resolve(rel.replace("/", java.io.File.separator));
        assertTrue(Files.exists(f), "Not generated: src/main/java/" + rel);
    }

    protected String readJava(String rel) throws Exception {
        return Files.readString(javaRoot().resolve(rel.replace("/", java.io.File.separator)));
    }

    protected ApiOperation findOp(ParsedApi api, String operationId) {
        return api.getOperations().stream()
            .filter(o -> operationId.equals(o.getOperationId()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Operation not found: " + operationId));
    }

    protected List<String> extractMethodSignatures(String javaSource) {
        List<String> sigs = new ArrayList<>();
        for (String line : javaSource.split("\n")) {
            String t = line.trim();
            if (t.endsWith(");") && t.contains("(")
                    && !t.startsWith("@") && !t.startsWith("import")
                    && !t.startsWith("//") && !t.startsWith("*")
                    && !t.contains("=") && !t.contains("class ")
                    && !t.contains("interface ")) {
                String sig = t
                    .replace("public ", "")
                    .replace(";", "")
                    .replaceAll("@Nonnull\\s+", "")
                    .replaceAll("@QueryMap\\s+", "")
                    .replaceAll("@Param\\(\"[^\"]+\"\\)\\s+", "")
                    .trim();
                if (!sig.isEmpty()) sigs.add(sig);
            }
        }
        return sigs;
    }
}
