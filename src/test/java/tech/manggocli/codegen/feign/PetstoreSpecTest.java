package tech.manggocli.codegen.feign;

import tech.manggocli.core.domain.api.ParsedApi;
import tech.manggocli.support.FeignGeneratorTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PetstoreSpecTest extends FeignGeneratorTestBase {

    private static final String SPEC_FILE = "petstore-test.yaml";
    private static final String CLIENT    = "petstore";
    private static final String BASE      = "com/example/" + CLIENT + "/";

    @BeforeEach
    void setup() throws Exception {
        generate(SPEC_FILE, CLIENT);
    }

    @Test void shouldGenerateMavenStructure() {
        assertTrue(Files.exists(projectRoot.resolve("pom.xml")));
        assertTrue(Files.isDirectory(javaRoot()));
    }

    @Test void shouldGeneratePetsTag() {
        assertJava(BASE + "pets/rest/PetsRest.java");
        assertJava(BASE + "pets/rest/client/PetsRestClient.java");
    }

    @Test void shouldGenerateStoreTag() {
        assertJava(BASE + "store/rest/StoreRest.java");
        assertJava(BASE + "store/rest/client/StoreRestClient.java");
    }

    @Test void shouldGenerateVetsTag() {
        assertJava(BASE + "vets/rest/VetsRest.java");
        assertJava(BASE + "vets/rest/client/VetsRestClient.java");
    }

    @Test void getVetAppointmentsShouldUseQueryMap() throws Exception {
        ParsedApi api = parser.parse(specPath(SPEC_FILE));
        assertTrue(findOp(api, "getVetAppointments").isUseQueryMap());
    }

    @Test void listVetProceduresWithQueryAndHeaderShouldUseQueryMap() throws Exception {
        ParsedApi api = parser.parse(specPath(SPEC_FILE));
        var op = findOp(api, "listVetProcedures");
        assertTrue(op.isUseQueryMap(), "query + header → must still use @QueryMap");
        assertFalse(op.needsRequestObject(), "no path params → must not consolidate into RequestObject");
        assertEquals(1, op.getHeaderParams().size(), "must have 1 header param (Idempotency-Key)");
        assertTrue(op.getQueryParams().size() > 1, "must have multiple query params");
    }

    @Test void listVetProceduresQueryMapDtoShouldHaveConstructor() throws Exception {
        String dto = readJava(BASE + "vets/domain/request/VetsListVetProceduresRequest.java");
        assertTrue(dto.contains("private VetsListVetProceduresRequest()"), "must have private no-arg constructor");
        assertTrue(dto.contains("public VetsListVetProceduresRequest("), "must have public all-args constructor");
        assertTrue(dto.contains("private String specialtyId"), "must have field specialtyId");
    }

    @Test void listVetProceduresRestClientShouldUseQueryMapNotIndividualParams() throws Exception {
        String client = readJava(BASE + "vets/rest/client/VetsRestClient.java");
        assertTrue(client.contains("@QueryMap"), "must use @QueryMap for listVetProcedures");
        assertTrue(client.contains("@Param(\"idempotencyKey\") String idempotencyKey"),
            "header param must be individual @Param");
        assertFalse(client.contains("?specialtyId={specialtyId}"),
            "query params must not appear in @RequestLine when @QueryMap is used");
    }

    @Test void scheduleAppointmentShouldNotNeedRequestObject() throws Exception {
        ParsedApi api = parser.parse(specPath(SPEC_FILE));
        var op = findOp(api, "scheduleAppointment");
        assertFalse(op.needsRequestObject(), "header + body → individual @Param, not RequestObject");
        assertEquals("ScheduleAppointmentRequest", op.getRequestBodySchema());
        assertFalse(op.getHeaderParams().isEmpty(), "must have header param Idempotency-Key");
    }

    @Test void scheduleCheckupShouldNotNeedRequestObject() throws Exception {
        ParsedApi api = parser.parse(specPath(SPEC_FILE));
        var op = findOp(api, "scheduleCheckup");
        assertFalse(op.needsRequestObject(), "path + header + body → headers as @Param, not consolidated");
        assertFalse(op.getPathParams().isEmpty(), "must have path params vetId and petId");
        assertFalse(op.getHeaderParams().isEmpty(), "must have header params Idempotency-Key and X-Vet-License-Id");
    }

    @Test void shouldGenerateTransitiveSchemas() throws Exception {
        Path page    = javaRoot().resolve(BASE + "vets/domain/response/AppointmentPage.java");
        Path summary = javaRoot().resolve(BASE + "vets/domain/response/AppointmentSummary.java");

        assertTrue(Files.exists(page),    "AppointmentPage must be generated");
        assertTrue(Files.exists(summary), "AppointmentSummary must be generated (transitive schema)");
        assertTrue(Files.readString(page).contains("AppointmentSummary"));
    }

    @Test void shouldGenerateNestedSchemas() {
        assertJava(BASE + "vets/domain/response/VetProfile.java");
    }

    @Test void petsRestAndClientSignaturesShouldMatch() throws Exception {
        String rest = readJava(BASE + "pets/rest/PetsRest.java");
        String impl = readJava(BASE + "pets/rest/client/PetsRestClient.java");

        List<String> restSigs = extractMethodSignatures(rest);
        assertFalse(restSigs.isEmpty(), "PetsRest must have methods");
        List<String> implSigs = extractMethodSignatures(impl);
        for (String sig : restSigs)
            assertTrue(implSigs.stream().anyMatch(s -> s.equals(sig)),
                "PetsRestClient must have @Override for: " + sig);
    }

    @Test void vetsRestAndClientSignaturesShouldMatch() throws Exception {
        String rest = readJava(BASE + "vets/rest/VetsRest.java");
        String impl = readJava(BASE + "vets/rest/client/VetsRestClient.java");

        List<String> restSigs = extractMethodSignatures(rest);
        List<String> implSigs = extractMethodSignatures(impl);
        for (String sig : restSigs)
            assertTrue(implSigs.stream().anyMatch(s -> s.equals(sig)),
                "VetsRestClient must have: " + sig);
    }

    @Test void configShouldHaveGenericProxy() throws Exception {
        String c = readJava("com/example/config/PetstoreClientConfiguration.java");
        assertTrue(c.contains("public <T> T createPetstoreProxy(Class<T> clazz)"));
        assertTrue(c.contains("return createPetstoreProxy(PetsRestClient.class)"));
        assertTrue(c.contains("return createPetstoreProxy(StoreRestClient.class)"));
        assertTrue(c.contains("return createPetstoreProxy(VetsRestClient.class)"));
    }

    @Test void scheduleCheckupShouldHaveHeaderParamsAndBodyDto() throws Exception {
        ParsedApi api = parser.parse(specPath(SPEC_FILE));
        var op = findOp(api, "scheduleCheckup");
        assertFalse(op.needsRequestObject(), "header + body → individual @Param, not RequestObject");
        assertEquals("CheckupRequest", op.getRequestBodySchema());
        assertFalse(op.getHeaderParams().isEmpty(), "must have header params (Idempotency-Key, X-Vet-License-Id)");

        Path f = javaRoot().resolve(BASE + "vets/domain/request/CheckupRequest.java");
        assertTrue(Files.exists(f), "body DTO must be generated");
        assertTrue(Files.readString(f).contains("@JsonProperty(\"procedureCode\")"));
    }

    @Test void appointmentResponseShouldImportUUID() throws Exception {
        Path dto = javaRoot().resolve(BASE + "vets/domain/response/AppointmentResponse.java");
        if (Files.exists(dto))
            assertTrue(Files.readString(dto).contains("import java.util.UUID;"));
    }

    @Test void responseDtosShouldBeGeneratedForVetsTag() {
        assertJava(BASE + "vets/domain/response/MedicalRecord.java");
        assertJava(BASE + "vets/domain/response/Prescription.java");
    }
}
