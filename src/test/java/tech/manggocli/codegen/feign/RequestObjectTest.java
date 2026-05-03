package tech.manggocli.codegen.feign;

import tech.manggocli.support.FeignGeneratorTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class RequestObjectTest extends FeignGeneratorTestBase {

    private static final String SPEC_FILE = "petstore-test.yaml";
    private static final String CLIENT    = "petstore";
    private static final String BASE      = "com/example/" + CLIENT + "/";

    @BeforeEach
    void setup() throws Exception {
        generate(SPEC_FILE, CLIENT);
    }

    // ── Header params as @Param (new behavior) ────────────────────────────────

    @Test void headerParamMustAppearInMethodSignature() throws Exception {
        String client = readJava(BASE + "pets/rest/client/PetsRestClient.java");
        assertTrue(client.contains("@Param(\"idempotencyKey\") String idempotencyKey"),
            "Header param must be @Param in method signature");
    }

    @Test void headerParamMustAppearInHeadersAnnotation() throws Exception {
        String client = readJava(BASE + "pets/rest/client/PetsRestClient.java");
        assertTrue(client.contains("Idempotency-Key: {idempotencyKey}"),
            "Header param must have placeholder in @Headers");
    }

    @Test void headerParamMustNotBeJsonIgnoreFieldInBodyDto() throws Exception {
        Path dto = javaRoot().resolve(BASE + "pets/domain/request/CreatePetAccountRequest.java");
        if (Files.exists(dto)) {
            String c = Files.readString(dto);
            assertFalse(c.contains("@JsonIgnore"),
                "Header param must not appear in body DTO as @JsonIgnore");
            assertTrue(c.contains("@JsonProperty(\"name\")"),
                "Body field must still have @JsonProperty");
        }
    }

    @Test void restInterfaceAlsoReceivesHeaderParamInSignature() throws Exception {
        String rest = readJava(BASE + "pets/rest/PetsRest.java");
        assertTrue(rest.contains("String idempotencyKey"),
            "Rest interface must declare header param in signature");
    }

    // ── query + path still consolidates (needsRequestObject = true) ───────────

    @Test void queryPlusPathGeneratesConsolidatedRequestObject() {
        assertJava(BASE + "pets/domain/request/GetPetOrderHistoryRequest.java");
    }

    @Test void consolidatedRequestObjectHasPathAndQueryFields() throws Exception {
        String src = readJava(BASE + "pets/domain/request/GetPetOrderHistoryRequest.java");
        assertTrue(src.contains("@JsonIgnore"),          "Path/query fields must have @JsonIgnore");
        assertTrue(src.contains("private String id"),    "Must have path param 'id'");
        assertTrue(src.contains("private Integer page"), "Must have query param 'page'");
        assertFalse(src.contains("idempotencyKey"),      "Must not have header params");
    }

    @Test void consolidatedRequestObjectExtendsApiModel() throws Exception {
        String src = readJava(BASE + "pets/domain/request/GetPetOrderHistoryRequest.java");
        assertTrue(src.contains("extends ApiModel"), "RequestObject must extend ApiModel");
    }
}
