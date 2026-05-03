package tech.manggocli.codegen.httpclient;

import tech.manggocli.support.NativeGeneratorTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NativeClientGenerationTest extends NativeGeneratorTestBase {

    private static final String SPEC_FILE = "petstore-test.yaml";
    private static final String CLIENT    = "petstore";
    private static final String BASE      = "com/example/" + CLIENT + "/";

    @BeforeEach
    void setup() throws Exception {
        generate(SPEC_FILE, CLIENT);
    }

    @Test void nativePomXmlShouldBeGenerated() {
        assertTrue(projectRoot.resolve("pom.xml").toFile().exists(), "pom.xml must be generated");
    }

    @Test void nativePomShouldNotContainFeignDependency() throws Exception {
        String pom = java.nio.file.Files.readString(projectRoot.resolve("pom.xml"));
        assertFalse(pom.contains("feign-core"),    "native pom must not have feign-core");
        assertFalse(pom.contains("feign-jackson"), "native pom must not have feign-jackson");
        assertTrue(pom.contains("jackson-databind"), "native pom must have jackson-databind");
    }

    @Test void restInterfaceShouldBeGenerated() {
        assertJava(BASE + "pets/rest/PetsRest.java");
        assertJava(BASE + "store/rest/StoreRest.java");
    }

    @Test void restInterfaceShouldNotContainFeignAnnotations() throws Exception {
        String src = readJava(BASE + "pets/rest/PetsRest.java");
        assertFalse(src.contains("import feign."), "interface must not import feign");
        assertFalse(src.contains("@RequestLine"), "interface must not have @RequestLine");
    }

    @Test void nativeRestClientShouldBeGenerated() {
        assertJava(BASE + "pets/rest/client/PetsRestClient.java");
        assertJava(BASE + "store/rest/client/StoreRestClient.java");
    }

    @Test void baseRestClientShouldBeGenerated() {
        assertJava("com/example/common/BaseRestClient.java");
    }

    @Test void nativeRestClientShouldExtendBaseRestClient() throws Exception {
        String src = readJava(BASE + "pets/rest/client/PetsRestClient.java");
        assertTrue(src.contains("extends BaseRestClient"), "client must extend BaseRestClient");
    }

    @Test void nativeRestClientShouldImplementInterface() throws Exception {
        String src = readJava(BASE + "pets/rest/client/PetsRestClient.java");
        assertTrue(src.contains("implements PetsRest"), "client must implement interface");
        assertTrue(src.contains("HttpClient"),   "client must use java.net.http.HttpClient");
        assertTrue(src.contains("ObjectMapper"), "client must use ObjectMapper");
    }

    @Test void nativeRestClientShouldNotContainFeignAnnotations() throws Exception {
        String src = readJava(BASE + "pets/rest/client/PetsRestClient.java");
        assertFalse(src.contains("import feign."), "native client must not import feign");
        assertFalse(src.contains("@RequestLine"), "native client must not have @RequestLine");
        assertFalse(src.contains("@Headers"),     "native client must not have @Headers");
    }

    @Test void nativeRestClientShouldHaveConstructorWithHttpClientAndMapper() throws Exception {
        String src = readJava(BASE + "pets/rest/client/PetsRestClient.java");
        assertTrue(src.contains("public PetsRestClient(HttpClient http, ObjectMapper mapper"));
    }

    @Test void nativeRestClientShouldSendPostWithBody() throws Exception {
        String src = readJava("com/example/common/BaseRestClient.java");
        assertTrue(src.contains("writeValueAsString("),    "POST must serialize body");
        assertTrue(src.contains("BodyPublishers.ofString"), "POST must send body");
    }

    @Test void nativeRestClientShouldDeserializeResponse() throws Exception {
        String src = readJava("com/example/common/BaseRestClient.java");
        assertTrue(src.contains("mapper.readValue"), "must deserialize response with Jackson");
    }

    @Test void nativeRestClientShouldReplacePathParams() throws Exception {
        String src = readJava(BASE + "pets/rest/client/PetsRestClient.java");
        assertTrue(src.contains(".replace(\"{id}\""), "must replace path params in URL");
    }

    @Test void clientConfigurationShouldBeGenerated() {
        assertJava("com/example/config/PetstoreClientConfiguration.java");
    }

    @Test void clientConfigurationShouldUseHttpClientBean() throws Exception {
        String src = readJava("com/example/config/PetstoreClientConfiguration.java");
        assertTrue(src.contains("HttpClient"),   "configuration must declare HttpClient bean");
        assertTrue(src.contains("ObjectMapper"), "configuration must declare ObjectMapper bean");
        assertFalse(src.contains("FeignClientConfig"), "native configuration must not reference FeignClientConfig");
    }

    @Test void clientConfigurationShouldWireRestClientBeans() throws Exception {
        String src = readJava("com/example/config/PetstoreClientConfiguration.java");
        assertTrue(src.contains("PetsRestClient"), "configuration must instantiate PetsRestClient");
    }

    @Test void smokeTestsShouldBeGenerated() {
        assertTestJava(BASE + "pets/rest/client/PetsRestClientSmokeTest.java");
        assertTestJava(BASE + "store/rest/client/StoreRestClientSmokeTest.java");
    }

    @Test void smokeTestShouldUseRestClientAndJUnit5() throws Exception {
        String src = readTestJava(BASE + "pets/rest/client/PetsRestClientSmokeTest.java");
        assertTrue(src.contains("@Tag(\"integration\")"), "must have @Tag(integration)");
        assertTrue(src.contains("PetsRestClient client"), "must declare client field");
        assertTrue(src.contains("@BeforeEach"), "must have @BeforeEach");
        assertTrue(src.contains("@Test"),       "must have @Test");
        assertTrue(src.contains("HttpClient.newBuilder()"), "must create HttpClient");
        assertTrue(src.contains("new ObjectMapper()"),      "must create ObjectMapper");
    }

    @Test void smokeTestShouldBeInCorrectPackage() throws Exception {
        String src = readTestJava(BASE + "pets/rest/client/PetsRestClientSmokeTest.java");
        assertTrue(src.contains("package com.example.petstore.pets.rest.client;"));
    }

    @Test void topLevelEnumShouldBeImportedFromEnumsPackage() throws Exception {
        String src = readJava(BASE + "pets/rest/client/PetsRestClient.java");
        assertTrue(src.contains("import com.example.petstore.pets.domain.enums.PetStatus;"),
            "response enum must be imported from enums package");
        assertFalse(src.contains("import com.example.petstore.pets.domain.response.PetStatus;"),
            "response enum must NOT be imported from response package");
    }
}
