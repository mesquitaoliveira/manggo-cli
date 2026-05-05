package tech.manggocli.domain;

import tech.manggocli.core.domain.service.JavaIdentifiers;
import tech.manggocli.support.FeignGeneratorTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JavaIdentifierTest extends FeignGeneratorTestBase {

    private static final String SPEC_FILE = "identifier-test-api.yml";
    private static final String CLIENT    = "test";
    private static final String BASE      = "com/example/" + CLIENT + "/";

    // ── toValidJavaClassName (unit) ───────────────────────────────────────────

    @Test void hyphenInSchemaNameBecomesCapitalizedChar() {
        assertEquals("ItemInfo",                JavaIdentifiers.toValidJavaClassName("Item-Info"));
        assertEquals("PolicyVerdictActionEnum2", JavaIdentifiers.toValidJavaClassName("PolicyVerdictActionEnum-2"));
        assertEquals("MySchema",                JavaIdentifiers.toValidJavaClassName("my schema"));
    }

    @Test void schemaNameStartingWithDigitGetsPrefixed() {
        assertEquals("S2FaAlgo", JavaIdentifiers.toValidJavaClassName("2FaAlgo"));
    }

    @Test void validSchemaNameIsUnchanged() {
        assertEquals("VaultAccount", JavaIdentifiers.toValidJavaClassName("VaultAccount"));
        assertEquals("CreateVaultAccountRequest",
            JavaIdentifiers.toValidJavaClassName("CreateVaultAccountRequest"));
    }

    @Test void nullOrBlankSchemaNameReturnsUnknown() {
        assertEquals("Unknown", JavaIdentifiers.toValidJavaClassName(null));
        assertEquals("Unknown", JavaIdentifiers.toValidJavaClassName("   "));
        assertEquals("Unknown", JavaIdentifiers.toValidJavaClassName("---"));
    }

    // ── Enum constant sanitization (integration) ──────────────────────────────

    @BeforeEach
    void generate() throws Exception {
        generate(SPEC_FILE, CLIENT);
    }

    @Test void schemaWithHyphenInNameGeneratesValidClassName() {
        assertJava(BASE + "items/domain/response/ItemInfo.java");
    }

    @Test void enumConstantStartingWithDigitGetsPrefixedUnderscore() throws Exception {
        String src = readJava(BASE + "items/domain/enums/ItemStatus.java");
        assertTrue(src.contains("_2_PHASE(\"2-PHASE\")"),
            "Constant '2-PHASE' must become '_2_PHASE' — cannot start with digit");
    }

    @Test void enumConstantWithHyphenBecomesUnderscore() throws Exception {
        String src = readJava(BASE + "items/domain/enums/ItemStatus.java");
        assertTrue(src.contains("PENDING_DONE(\"PENDING-DONE\")"),
            "Constant 'PENDING-DONE' must become 'PENDING_DONE'");
        assertTrue(src.contains("ALHUMA_2(\"ALHUMA-2\")"),
            "Constant 'ALHUMA-2' must become 'ALHUMA_2'");
    }

    @Test void validEnumConstantIsUnchanged() throws Exception {
        String src = readJava(BASE + "items/domain/enums/ItemStatus.java");
        assertTrue(src.contains("ACTIVE(\"ACTIVE\")"), "Valid constant must not be modified");
    }

    @Test void enumShouldHaveJsonCreatorAndJsonValue() throws Exception {
        String src = readJava(BASE + "items/domain/enums/ItemStatus.java");
        assertTrue(src.contains("@JsonValue"),   "Enum must have @JsonValue on getValue()");
        assertTrue(src.contains("@JsonCreator"), "Enum must have @JsonCreator on fromValue()");
        assertTrue(src.contains("import com.fasterxml.jackson.annotation.JsonCreator;"));
        assertTrue(src.contains("import com.fasterxml.jackson.annotation.JsonValue;"));
    }

    // ── Inline object in property (integration) ──────────────────────────────

    @Test void inlineObjectOnPropertyGeneratesSeparateDtoClass() {
        assertJava(BASE + "items/domain/response/InitiatorConfigOperators.java");
    }

    @Test void inlineObjectPropertyTypeIsGeneratedClass() throws Exception {
        String src = readJava(BASE + "items/domain/response/InitiatorConfig.java");
        assertTrue(src.contains("InitiatorConfigOperators operators"),
            "Field 'operators' must have type 'InitiatorConfigOperators', not 'Object'");
    }

    @Test void nestedInlineEnumInsideInlineObjectIsAlsoGenerated() {
        assertJava(BASE + "items/domain/enums/InitiatorConfigOperatorsWildcard.java");
    }

    @Test void deeplyNestedInlineEnumHasCorrectStarConstant() throws Exception {
        String src = readJava(BASE + "items/domain/enums/InitiatorConfigOperatorsWildcard.java");
        assertTrue(src.contains("STAR(\"*\")"),
            "Enum generated inside inline object must have constant 'STAR' for '*'");
    }

    // ── sanitizeEnumConstant (unit) ───────────────────────────────────────────

    @Test void starSymbolBecomesStar() {
        assertEquals("STAR", JavaIdentifiers.sanitizeEnumConstant("*"));
    }

    @Test void plusSymbolBecomesPlus() {
        assertEquals("PLUS", JavaIdentifiers.sanitizeEnumConstant("+"));
    }

    @Test void regularEnumValueIsUnchanged() {
        assertEquals("ALLOW",            JavaIdentifiers.sanitizeEnumConstant("ALLOW"));
        assertEquals("REQUIRE_APPROVAL", JavaIdentifiers.sanitizeEnumConstant("REQUIRE_APPROVAL"));
        assertEquals("PENDING_DONE",     JavaIdentifiers.sanitizeEnumConstant("PENDING-DONE"));
    }

    @Test void nullOrBlankEnumValueReturnsUnknown() {
        assertEquals("UNKNOWN", JavaIdentifiers.sanitizeEnumConstant(null));
        assertEquals("UNKNOWN", JavaIdentifiers.sanitizeEnumConstant("   "));
    }

    // ── Inline enum in property (integration) ────────────────────────────────

    @Test void inlineEnumOnPropertyGeneratesSeparateEnumClass() {
        assertJava(BASE + "items/domain/enums/PolicyRuleAction.java");
    }

    @Test void inlineEnumPropertyTypeIsEnumClass() throws Exception {
        String src = readJava(BASE + "items/domain/response/PolicyRule.java");
        assertTrue(src.contains("PolicyRuleAction action"),
            "Field 'action' must have type 'PolicyRuleAction', not 'String'");
    }

    @Test void wildcardEnumConstantBecomesStar() throws Exception {
        String src = readJava(BASE + "items/domain/enums/PolicyRuleWildcardField.java");
        assertTrue(src.contains("STAR(\"*\")"),
            "Value '*' must generate constant 'STAR'");
    }

    @Test void generatedEnumConstantsAreValidJavaIdentifiers() throws Exception {
        String src = readJava(BASE + "items/domain/enums/ItemStatus.java");
        src.lines()
            .map(String::trim)
            .filter(l -> l.matches("[A-Za-z_][A-Za-z0-9_]*\\(\".*\"\\)[,;]"))
            .forEach(l -> {
                String constant = l.substring(0, l.indexOf('('));
                assertTrue(Character.isLetter(constant.charAt(0)) || constant.charAt(0) == '_',
                    "Constant '" + constant + "' is not a valid Java identifier");
                assertTrue(constant.matches("[A-Za-z_][A-Za-z0-9_]*"),
                    "Constant '" + constant + "' contains invalid characters");
            });
    }
}
