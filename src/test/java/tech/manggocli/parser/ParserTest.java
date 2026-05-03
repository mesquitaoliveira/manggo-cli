package tech.manggocli.parser;

import tech.manggocli.core.domain.api.ParsedApi;
import tech.manggocli.support.FeignGeneratorTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest extends FeignGeneratorTestBase {

    private static final String SPEC_FILE = "petstore-test.yaml";

    private ParsedApi api;

    @BeforeEach
    void setup() throws Exception {
        api = parser.parse(specPath(SPEC_FILE));
    }

    @Test void shouldParseTitleAndVersion() {
        assertEquals("Petstore API", api.getTitle());
    }

    @Test void shouldParseTags() {
        assertTrue(api.getTags().contains("pets"));
        assertTrue(api.getTags().contains("store"));
    }

    @Test void shouldParseSchemas() {
        assertTrue(api.getSchemas().containsKey("CreatePetOrderRequest"));
        assertTrue(api.getSchemas().containsKey("PetListResponse"));
        assertTrue(api.getSchemas().containsKey("StoreItemResponse"));
        assertTrue(api.getSchemas().containsKey("CreatePetAccountRequest"));
    }

    @Test void listPetsShouldUseQueryMap() {
        var op = findOp(api, "listPets");
        assertTrue(op.isUseQueryMap(), "GET with query-only params must use @QueryMap");
        assertFalse(op.needsRequestObject());
    }

    @Test void getPetByIdShouldHavePathParam() {
        var op = findOp(api, "getPetById");
        assertEquals("id", op.getPathParams().get(0).getName());
        assertFalse(op.isUseQueryMap());
        assertFalse(op.needsRequestObject());
    }

    @Test void createPetOrderShouldHaveRequestBodySchemaName() {
        var op = findOp(api, "createPetOrder");
        assertEquals("CreatePetOrderRequest", op.getRequestBodySchema(),
            "requestBodySchema must be the $ref name, not null");
        assertEquals("PetOrderResponse", op.getResponseSchema());
    }

    @Test void listPetsShouldHaveCorrectResponseSchema() {
        var op = findOp(api, "listPets");
        assertEquals("PetListResponse", op.getResponseSchema(),
            "responseSchema must be 'PetListResponse', not an inline-generated name");
    }

    @Test void createPetAccountWithHeaderDoesNotNeedRequestObject() {
        var op = findOp(api, "createPetAccount");
        assertFalse(op.needsRequestObject(), "header-only → individual @Param, not RequestObject");
        assertEquals("CreatePetAccountRequest", op.getRequestBodySchema());
        assertEquals(1, op.getHeaderParams().size());
        assertEquals("idempotencyKey", op.getHeaderParams().get(0).getCamelCaseName());
    }

    @Test void createPetAccountAssetWithHeaderAndPathDoesNotNeedRequestObject() {
        var op = findOp(api, "createPetAccountAsset");
        assertFalse(op.needsRequestObject(), "path + header + body → headers as @Param, not consolidated");
        assertEquals(2, op.getPathParams().size());
        assertEquals(1, op.getHeaderParams().size());
    }

    @Test void getPetOrderHistoryWithPathAndQueryNeedsRequestObject() {
        var op = findOp(api, "getPetOrderHistory");
        assertTrue(op.needsRequestObject(), "path + query → consolidates into RequestObject");
    }

    @Test void updatePetOrderShouldHavePathParamAndBody() {
        var op = findOp(api, "updatePetOrder");
        assertFalse(op.needsRequestObject(), "path + body, no header → does not consolidate");
        assertFalse(op.getPathParams().isEmpty(), "must have path param 'id'");
        assertEquals("id", op.getPathParams().get(0).getName());
        assertEquals("UUID", op.getPathParams().get(0).getJavaType());
        assertEquals("CreatePetOrderRequest", op.getRequestBodySchema());
    }
}
