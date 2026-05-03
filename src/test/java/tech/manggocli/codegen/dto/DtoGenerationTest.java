package tech.manggocli.codegen.dto;

import tech.manggocli.support.FeignGeneratorTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class DtoGenerationTest extends FeignGeneratorTestBase {

    private static final String SPEC_FILE = "petstore-test.yaml";
    private static final String CLIENT    = "petstore";
    private static final String BASE      = "com/example/" + CLIENT + "/";

    @BeforeEach
    void setup() throws Exception {
        generate(SPEC_FILE, CLIENT);
    }

    @Test void dtoWithDateTimeShouldImportLocalDateTime() throws Exception {
        Path dto = javaRoot().resolve(BASE + "pets/domain/response/PetOrderResponse.java");
        if (Files.exists(dto))
            assertTrue(Files.readString(dto).contains("import java.time.LocalDateTime;"));
    }

    @Test void dtoWithUUIDShouldImportUUID() throws Exception {
        Path dto = javaRoot().resolve(BASE + "store/domain/response/StoreItemResponse.java");
        if (Files.exists(dto))
            assertTrue(Files.readString(dto).contains("import java.util.UUID;"));
    }

    @Test void dtoShouldHaveGettersSettersAndJsonProperty() throws Exception {
        Path dto = javaRoot().resolve(BASE + "pets/domain/request/CreatePetOrderRequest.java");
        if (Files.exists(dto)) {
            String c = Files.readString(dto);
            assertTrue(c.contains("private String fromPet;"));
            assertTrue(c.contains("public String getFromPet()"));
            assertTrue(c.contains("@JsonProperty(\"fromPet\")"));
        }
    }

    @Test void dtoShouldExtendApiModel() throws Exception {
        String src = readJava(BASE + "pets/domain/request/CreatePetOrderRequest.java");
        assertTrue(src.contains("extends ApiModel"),
            "DTO must extend ApiModel");
        assertTrue(src.contains("import com.example.common.ApiModel;"),
            "DTO must import ApiModel from common package");
    }

    @Test void requestObjectShouldExtendApiModel() throws Exception {
        String src = readJava(BASE + "pets/domain/request/GetPetOrderHistoryRequest.java");
        assertTrue(src.contains("extends ApiModel"),
            "RequestObject must extend ApiModel");
    }

    @Test void apiModelFileShouldBeGenerated() {
        assertJava("com/example/common/ApiModel.java");
    }

    @Test void multiWordTagShouldProducePascalCaseClassName() {
        assertJava(BASE + "order_management/rest/OrderManagementRest.java");
        assertJava(BASE + "order_management/rest/client/OrderManagementRestClient.java");
    }

    @Test void underscorePropertyNameShouldProducePascalCaseSyntheticEnumName() {
        assertJava(BASE + "order_management/domain/enums/OrderReportOrderType.java");
        assertJava(BASE + "order_management/domain/enums/OrderReportCurrencyUnit.java");
    }

    @Test void allOfSchemaShouldMergePropertiesFromRefAndInlineObject() throws Exception {
        String src = readJava(BASE + "pets/domain/response/PetDetails.java");
        assertTrue(src.contains("private UUID petId"),            "must inherit petId from PetBase");
        assertTrue(src.contains("private LocalDateTime createdAt"), "must inherit createdAt from PetBase");
        assertTrue(src.contains("private LocalDateTime expiresAt"), "must have expiresAt from inline object");
        assertTrue(src.contains("private Double price"),           "must have price from inline object");
    }

    @Test void deprecatedDescriptionShouldEmitAnnotation() throws Exception {
        String src = readJava(BASE + "pets/domain/response/PetDetails.java");
        assertTrue(src.contains("@Deprecated"), "field with Deprecated description must have @Deprecated");
    }

    @Test void topLevelEnumUsedAsResponseShouldImportFromEnumsPackage() throws Exception {
        String client = readJava(BASE + "pets/rest/client/PetsRestClient.java");
        assertTrue(client.contains("import com.example.petstore.pets.domain.enums.PetStatus;"),
            "response enum must be imported from enums package");
        assertFalse(client.contains("import com.example.petstore.pets.domain.response.PetStatus;"),
            "response enum must NOT be imported from response package");
    }

    @Test void sharedEnumMustBeGeneratedForEachTag() {
        assertJava(BASE + "pets/domain/enums/AnimalCategory.java");
        assertJava(BASE + "store/domain/enums/AnimalCategory.java");
    }

    @Test void booleanAliasSchemaClassShouldNotBeGenerated() {
        Path p = javaRoot().resolve(BASE + "pets/domain/response/PetAvailability.java");
        assertFalse(Files.exists(p),
            "PetAvailability is a boolean alias — must not generate a .java class");
    }

    @Test void stringAliasSchemaClassShouldNotBeGenerated() {
        Path p = javaRoot().resolve(BASE + "pets/domain/response/VetLicenseCode.java");
        assertFalse(Files.exists(p),
            "VetLicenseCode is a plain string alias — must not generate a .java class");
    }

    @Test void petInfoFieldTypeShouldResolveBooleanAliasToJavaPrimitive() throws Exception {
        String src = readJava(BASE + "pets/domain/response/PetInfo.java");
        assertFalse(src.contains("PetAvailability"),
            "PetInfo must not reference PetAvailability — boolean alias resolves to Boolean");
    }
}
