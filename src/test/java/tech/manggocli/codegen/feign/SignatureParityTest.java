package tech.manggocli.codegen.feign;

import tech.manggocli.support.FeignGeneratorTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SignatureParityTest extends FeignGeneratorTestBase {

    private static final String SPEC_FILE = "petstore-test.yaml";
    private static final String CLIENT    = "petstore";
    private static final String BASE      = "com/example/" + CLIENT + "/";

    @BeforeEach
    void setup() throws Exception {
        generate(SPEC_FILE, CLIENT);
    }

    @Test void petsRestAndClientMustHaveIdenticalSignatures() throws Exception {
        String rest = readJava(BASE + "pets/rest/PetsRest.java");
        String impl = readJava(BASE + "pets/rest/client/PetsRestClient.java");

        List<String> restSigs = extractMethodSignatures(rest);
        List<String> implSigs = extractMethodSignatures(impl);

        assertFalse(restSigs.isEmpty(), "Rest must have methods");
        for (String sig : restSigs) {
            assertTrue(implSigs.stream().anyMatch(s -> s.equals(sig)),
                "PetsRestClient must have @Override for: " + sig);
        }
    }

    @Test void everyClientMethodMustHaveOverride() throws Exception {
        String impl = readJava(BASE + "pets/rest/client/PetsRestClient.java");

        long overrides = impl.lines().filter(l -> l.trim().equals("@Override")).count();
        List<String> sigs = extractMethodSignatures(impl);

        assertTrue(overrides > 0, "RestClient must have @Override annotations");
        assertEquals(overrides, sigs.size(), "Every method must have @Override");
    }

    @Test void storeRestAndClientMustHaveIdenticalSignatures() throws Exception {
        String rest = readJava(BASE + "store/rest/StoreRest.java");
        String impl = readJava(BASE + "store/rest/client/StoreRestClient.java");

        List<String> restSigs = extractMethodSignatures(rest);
        List<String> implSigs = extractMethodSignatures(impl);

        for (String sig : restSigs) {
            assertTrue(implSigs.stream().anyMatch(s -> s.equals(sig)),
                "StoreRestClient must have: " + sig);
        }
    }

    @Test void pathParamPlusBodyOpMustIncludePathParamInInterfaceSignature() throws Exception {
        String rest = readJava(BASE + "pets/rest/PetsRest.java");
        assertTrue(rest.contains("updatePetOrder(UUID id, CreatePetOrderRequest request)"),
            "Rest interface must have signature 'updatePetOrder(UUID id, CreatePetOrderRequest request)'");
    }

    @Test void pathParamPlusBodyOpSignaturesMustMatchBetweenRestAndClient() throws Exception {
        String rest = readJava(BASE + "pets/rest/PetsRest.java");
        String impl = readJava(BASE + "pets/rest/client/PetsRestClient.java");

        List<String> restSigs = extractMethodSignatures(rest);
        List<String> implSigs = extractMethodSignatures(impl);

        String updateSig = restSigs.stream()
            .filter(s -> s.contains("updatePetOrder"))
            .findFirst()
            .orElseThrow(() -> new AssertionError("updatePetOrder not found in PetsRest"));

        assertTrue(implSigs.stream().anyMatch(s -> s.equals(updateSig)),
            "PetsRestClient must have @Override with same signature: " + updateSig);
    }
}
