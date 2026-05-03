package tech.manggocli.domain;

import tech.manggocli.core.domain.service.NamingService;
import tech.manggocli.support.FeignGeneratorTestBase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CamelCaseTest extends FeignGeneratorTestBase {

    private static final String SPEC_FILE = "petstore-test.yaml";
    private static final String CLIENT    = "petstore";
    private static final String BASE      = "com/example/" + CLIENT + "/";

    @Test void snakeCaseToCamelCase() {
        assertEquals("getV1ApiBlockchain",     NamingService.toCamelCase("get_v1_api_blockchain"));
        assertEquals("getBlockchainById",       NamingService.toCamelCase("get_blockchain_by_id"));
        assertEquals("getAllTokens",            NamingService.toCamelCase("get-all-tokens"));
        assertEquals("getVaultById",            NamingService.toCamelCase("GetVaultById"));
        assertEquals("getTokens",               NamingService.toCamelCase("getTokens"));
        assertEquals("createVaultAccountAsset", NamingService.toCamelCase("createVaultAccountAsset"));
    }

    @Test void generatedInterfaceShouldHaveCamelCaseMethods() throws Exception {
        generate(SPEC_FILE, CLIENT);
        String c = readJava(BASE + "pets/rest/PetsRest.java");
        assertFalse(c.contains(" get_"), "Interface must not have snake_case methods");
        assertTrue(c.contains("listPets("),         "Must have listPets");
        assertTrue(c.contains("createPetOrder("),   "Must have createPetOrder");
        assertTrue(c.contains("getPetById("),       "Must have getPetById");
        assertTrue(c.contains("createPetAccount("), "Must have createPetAccount");
    }
}
