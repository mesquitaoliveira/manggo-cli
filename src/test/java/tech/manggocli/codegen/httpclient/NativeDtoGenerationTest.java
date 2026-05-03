package tech.manggocli.codegen.httpclient;

import tech.manggocli.support.NativeGeneratorTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NativeDtoGenerationTest extends NativeGeneratorTestBase {

    private static final String SPEC_FILE = "petstore-test.yaml";
    private static final String CLIENT    = "petstore";
    private static final String BASE      = "com/example/" + CLIENT + "/";

    @BeforeEach
    void setup() throws Exception {
        generate(SPEC_FILE, CLIENT);
    }

    @Test void apiModelFileShouldBeGenerated() {
        assertJava("com/example/common/ApiModel.java");
    }

    @Test void dtoShouldExtendApiModel() throws Exception {
        String src = readJava(BASE + "pets/domain/request/CreatePetOrderRequest.java");
        assertTrue(src.contains("extends ApiModel"));
        assertTrue(src.contains("import com.example.common.ApiModel;"));
    }

    @Test void dtoWithDateTimeShouldImportLocalDateTime() throws Exception {
        String src = readJava(BASE + "pets/domain/response/PetOrderResponse.java");
        assertTrue(src.contains("import java.time.LocalDateTime;"));
    }

    @Test void dtoWithUUIDShouldImportUUID() throws Exception {
        String src = readJava(BASE + "store/domain/response/StoreItemResponse.java");
        assertTrue(src.contains("import java.util.UUID;"));
    }

    @Test void allOfSchemaShouldMergeProperties() throws Exception {
        String src = readJava(BASE + "pets/domain/response/PetDetails.java");
        assertTrue(src.contains("private UUID petId"));
        assertTrue(src.contains("private LocalDateTime expiresAt"));
        assertTrue(src.contains("private Double price"));
    }

    @Test void multiWordTagShouldProducePascalCaseClassName() {
        assertJava(BASE + "order_management/rest/OrderManagementRest.java");
        assertJava(BASE + "order_management/rest/client/OrderManagementRestClient.java");
    }

    @Test void sharedEnumMustBeGeneratedForEachTag() {
        assertJava(BASE + "pets/domain/enums/AnimalCategory.java");
        assertJava(BASE + "store/domain/enums/AnimalCategory.java");
    }
}
