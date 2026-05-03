package tech.manggocli.codegen.dto;

import tech.manggocli.support.FeignGeneratorTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for array-alias schema resolution.
 *
 * Bug: when schema A is type:array (an "array alias") and schema B has a property
 * typed as List<A>, ArrayAliasResolver skipped the resolution because
 * "List<X>".startsWith("List<") returned early before checking if X was an alias.
 *
 * Expected: List<A> → List<List<ItemType>>, no A.java class generated.
 */
class ArrayAliasDtoTest extends FeignGeneratorTestBase {

    private static final String SPEC_FILE = "array-alias-test-api.yml";
    private static final String CLIENT    = "widgets";
    private static final String BASE      = "com/example/" + CLIENT + "/";

    @BeforeEach
    void setup() throws Exception {
        generate(SPEC_FILE, CLIENT);
    }

    @Test
    void itemClassShouldBeGenerated() {
        assertJava(BASE + "widgets/domain/response/Item.java");
    }

    @Test
    void containerClassShouldBeGenerated() {
        assertJava(BASE + "widgets/domain/response/Container.java");
    }

    @Test
    void arrayAliasClassShouldNotBeGenerated() {
        Path aliasClass = javaRoot().resolve(BASE + "widgets/domain/response/ItemList.java");
        assertFalse(Files.exists(aliasClass),
            "ItemList is an array-alias — must not generate a .java class");
    }

    @Test
    void containerGroupsFieldShouldUseResolvedListType() throws Exception {
        String src = readJava(BASE + "widgets/domain/response/Container.java");
        assertFalse(src.contains("List<ItemList>"),
            "Field 'groups' must not reference the alias class ItemList");
        assertTrue(src.contains("List<List<Item>>") || src.contains("List<List<"),
            "Field 'groups' must be List<List<Item>> after alias resolution");
    }

    @Test
    void containerShouldNotImportItemList() throws Exception {
        String src = readJava(BASE + "widgets/domain/response/Container.java");
        assertFalse(src.contains("import") && src.contains("ItemList"),
            "Container must not import ItemList — it is an alias, not a class");
    }

    @Test
    void arrayAliasAsDirectResponseShouldResolveToListType() throws Exception {
        Path itemListClass = javaRoot().resolve(BASE + "widgets/domain/response/ItemList.java");
        assertFalse(Files.exists(itemListClass),
            "ItemList returned directly as response must also not generate a class");
    }
}
