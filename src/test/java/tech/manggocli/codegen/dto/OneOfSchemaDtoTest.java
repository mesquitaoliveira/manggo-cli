package tech.manggocli.codegen.dto;

import tech.manggocli.support.FeignGeneratorTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for composed schema (oneOf/anyOf) handling across three passes.
 *
 * Pattern A — oneOf: [string-enum/wildcard, $ref]
 *   No inline object branch. Pass 3 merges properties from the $ref schema.
 *   Example: InitiatorConfigPattern = oneOf: ['*', InitiatorConfig]
 *   → generates class with userId + accountId from InitiatorConfig
 *
 * Pattern B — oneOf: [object-with-properties, primitive]
 *   Pass 1 extracts inline object branch. No Pass 3 needed (properties already populated).
 *   Example: OtaStatusResponse = oneOf: [{message: string}, string]
 *   → generates class with message field
 *
 * Pattern C — oneOf: [string-enum, $ref-to-object-with-properties]
 *   Mirrors ContractMethodPattern from real specs.
 *   Pass 3 merges all properties from the $ref.
 *   Example: ContractMethodPattern = oneOf: ['*', ContractMethodConfig]
 *   → generates class with methodCalls + operator
 *
 * Pattern D — anyOf: [$ref]
 *   Pass 3 merges properties from the $ref into the empty schema.
 *   Example: AnyOfPattern = anyOf: [AnyOfConfig]
 *   → generates class with currency + amount
 */
class OneOfSchemaDtoTest extends FeignGeneratorTestBase {

    private static final String SPEC_FILE = "oneof-schema-test-api.yml";
    private static final String CLIENT    = "patterns";
    private static final String BASE      = "com/example/" + CLIENT + "/";

    @BeforeEach
    void setup() throws Exception {
        generate(SPEC_FILE, CLIENT);
    }

    // ── Pattern A: oneOf [string-enum, $ref] → Pass 3 merges $ref properties ──

    @Test
    void patternAClassShouldBeGenerated() {
        assertJava(BASE + "patterns/domain/response/InitiatorConfigPattern.java");
    }

    @Test
    void patternAClassShouldHavePropertiesMergedFromRef() throws Exception {
        String src = readJava(BASE + "patterns/domain/response/InitiatorConfigPattern.java");
        assertTrue(src.contains("userId"),
            "InitiatorConfigPattern must have 'userId' merged from InitiatorConfig via Pass 3");
        assertTrue(src.contains("accountId"),
            "InitiatorConfigPattern must have 'accountId' merged from InitiatorConfig via Pass 3");
    }

    @Test
    void patternAFieldInWrapperShouldUseNamedType() throws Exception {
        String src = readJava(BASE + "patterns/domain/response/PatternWrapper.java");
        assertTrue(src.contains("InitiatorConfigPattern"),
            "PatternWrapper.pattern must reference InitiatorConfigPattern");
    }

    // ── Pattern B: object branch present → Pass 1 extracts inline properties ──

    @Test
    void patternBClassShouldBeGenerated() {
        assertJava(BASE + "patterns/domain/response/OtaStatusResponse.java");
    }

    @Test
    void patternBClassShouldHaveExtractedInlineFields() throws Exception {
        String src = readJava(BASE + "patterns/domain/response/OtaStatusResponse.java");
        assertTrue(src.contains("message"),
            "OtaStatusResponse must have field 'message' extracted from the inline object branch");
    }

    @Test
    void patternBFieldInWrapperShouldReferenceGeneratedClass() throws Exception {
        String src = readJava(BASE + "patterns/domain/response/PatternWrapper.java");
        assertTrue(src.contains("OtaStatusResponse"),
            "PatternWrapper.status must reference OtaStatusResponse");
    }

    // ── Pattern C: oneOf [string-enum, $ref-to-object] → Pass 3 merges $ref ──

    @Test
    void patternCClassShouldBeGenerated() {
        assertJava(BASE + "patterns/domain/response/ContractMethodPattern.java");
    }

    @Test
    void patternCClassShouldHavePropertiesMergedFromContractMethodConfig() throws Exception {
        String src = readJava(BASE + "patterns/domain/response/ContractMethodPattern.java");
        assertTrue(src.contains("methodCalls"),
            "ContractMethodPattern must have 'methodCalls' merged from ContractMethodConfig via Pass 3");
        assertTrue(src.contains("operator"),
            "ContractMethodPattern must have 'operator' merged from ContractMethodConfig via Pass 3");
    }

    @Test
    void patternCFieldInWrapperShouldReferenceContractMethodPattern() throws Exception {
        String src = readJava(BASE + "patterns/domain/response/PatternWrapper.java");
        assertTrue(src.contains("ContractMethodPattern"),
            "PatternWrapper.contractMethod must reference ContractMethodPattern");
    }

    // ── Pattern D: anyOf [$ref] → Pass 3 merges $ref properties ─────────────

    @Test
    void patternDClassShouldBeGenerated() {
        assertJava(BASE + "patterns/domain/response/AnyOfPattern.java");
    }

    @Test
    void patternDClassShouldHavePropertiesMergedFromAnyOfConfig() throws Exception {
        String src = readJava(BASE + "patterns/domain/response/AnyOfPattern.java");
        assertTrue(src.contains("currency"),
            "AnyOfPattern must have 'currency' merged from AnyOfConfig via Pass 3");
        assertTrue(src.contains("amount"),
            "AnyOfPattern must have 'amount' merged from AnyOfConfig via Pass 3");
    }

    @Test
    void patternDFieldInWrapperShouldReferenceAnyOfPattern() throws Exception {
        String src = readJava(BASE + "patterns/domain/response/PatternWrapper.java");
        assertTrue(src.contains("AnyOfPattern"),
            "PatternWrapper.anyOfValue must reference AnyOfPattern");
    }

    // ── General ───────────────────────────────────────────────────────────────

    @Test
    void patternWrapperShouldBeGenerated() {
        assertJava(BASE + "patterns/domain/response/PatternWrapper.java");
    }
}
