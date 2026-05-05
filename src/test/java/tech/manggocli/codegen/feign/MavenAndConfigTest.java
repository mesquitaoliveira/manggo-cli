package tech.manggocli.codegen.feign;

import tech.manggocli.core.application.port.UserNotifier;
import tech.manggocli.infrastructure.codegen.mode.feign.FeignClientGenerator;
import tech.manggocli.core.domain.client.ClientSpec;
import tech.manggocli.core.domain.api.ParsedApi;
import tech.manggocli.support.FeignGeneratorTestBase;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class MavenAndConfigTest extends FeignGeneratorTestBase {

    private static final String SPEC_FILE = "petstore-test.yaml";
    private static final String CLIENT    = "petstore";
    private static final String BASE      = "com/example/" + CLIENT + "/";

    @Test void shouldGenerateMavenProjectStructure() throws Exception {
        generate(SPEC_FILE, CLIENT);

        assertTrue(Files.exists(projectRoot.resolve("pom.xml")), "pom.xml must be at root");
        assertFalse(Files.exists(projectRoot.resolve("com")), "Must not create 'com' directly at root");
        assertTrue(Files.isDirectory(javaRoot()));
        assertTrue(Files.isDirectory(projectRoot.resolve("src/main/resources")));
        assertTrue(Files.exists(projectRoot.resolve("src/main/resources/application.properties")));
    }

    @Test void pomShouldHaveAllRequiredDeps() throws Exception {
        generate(SPEC_FILE, CLIENT);
        String pom = Files.readString(projectRoot.resolve("pom.xml"));
        assertTrue(pom.contains("feign-core"));
        assertTrue(pom.contains("feign-jackson"));
        assertTrue(pom.contains("feign-httpclient"));
        assertTrue(pom.contains("spring-boot-starter"));
        assertTrue(pom.contains("commons-lang3"));
        assertTrue(pom.contains("<groupId>com.example</groupId>"));
    }

    @Test void clientConfigShouldHaveGenericProxyMethod() throws Exception {
        generate(SPEC_FILE, CLIENT);
        String c = readJava("com/example/config/PetstoreClientConfiguration.java");
        assertTrue(c.contains("public <T> T createPetstoreProxy(Class<T> clazz)"));
        assertEquals(1,
            c.lines().filter(l -> l.contains("FeignClientConfig.createProxy")).count(),
            "createProxy must appear only in the generic method");
        assertTrue(c.contains("return createPetstoreProxy("));
    }

    @Test void shouldGenerateTwoIsolatedClients() throws Exception {
        ParsedApi api = parser.parse(specPath(SPEC_FILE));
        FeignClientGenerator gen = new FeignClientGenerator();
        gen.generate(api, new ClientSpec("alpha", specPath(SPEC_FILE)), "com.example", projectRoot, UserNotifier.NOOP);
        gen.generate(api, new ClientSpec("beta",  specPath(SPEC_FILE)), "com.example", projectRoot, UserNotifier.NOOP);

        assertTrue(Files.exists(projectRoot.resolve("pom.xml")));
        assertJava("com/example/config/AlphaClientConfiguration.java");
        assertJava("com/example/config/BetaClientConfiguration.java");
        assertJava("com/example/alpha/pets/rest/PetsRest.java");
        assertJava("com/example/beta/pets/rest/PetsRest.java");

        assertTrue(readJava("com/example/config/AlphaClientConfiguration.java")
            .contains("public <T> T createAlphaProxy("));
        assertTrue(readJava("com/example/config/BetaClientConfiguration.java")
            .contains("public <T> T createBetaProxy("));
    }
}
