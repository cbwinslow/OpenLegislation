package gov.nysenate.openleg.processors.law;

import gov.nysenate.openleg.config.annotation.UnitTest;
import gov.nysenate.openleg.legislation.law.LawChapterCode;
import gov.nysenate.openleg.legislation.law.LawDocumentType;
import gov.nysenate.openleg.legislation.law.LawTreeNode;
import gov.nysenate.openleg.legislation.law.LawVersionId;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import testing_utils.LawTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static gov.nysenate.openleg.legislation.law.LawChapterCode.CPL;
import static gov.nysenate.openleg.legislation.law.LawChapterCode.EDN;
import static gov.nysenate.openleg.legislation.law.LawDocumentType.*;
import static org.junit.Assert.*;

@Category(UnitTest.class)
public class HintBasedLawBuilderTest {
    private HintBasedLawBuilder builder;
    private LawChapterCode code;

    @Test
    public void CPLHierarchyTest() {
        init(CPL.name(), "-CH11-A");
        testHierarchy("P1", PART, "P1");
        testHierarchy("P1TA", TITLE, "TA");
        testHierarchy("A1", ARTICLE, "A1");
        testChildNode("1.10");
        testHierarchy("A2", ARTICLE, "A2");
        testChildNode("2.10");
        testChildren("P1TA", "A1", "A2");

        testHierarchy("P1TB", TITLE, "TB");
        testHierarchy("A10", ARTICLE, "A10");
        testChildNode("10.10");
        testChildren("P1TB", "A10");

        testChildren("P1", "P1TA", "P1TB");

        testHierarchy("P2", PART, "P2");
        testHierarchy("P2TH", TITLE, "TH");
        testHierarchy("A100", ARTICLE, "A100");
        testChildNode("100.10");
        // Tries to add a bad section.
        testChildNode("101.20");
        assertFalse(builder.rootNode.find("101.20").isPresent());
        assertFalse(builder.lawDocMap.containsKey("101.20"));
        testChildren("P2", "P2TH");
        testChildren("P2TH", "A100");
    }

    @Test
    public void EDNHierarchyTest() {
        init(EDN.name(), "-CH16");
        testHierarchy("T1", TITLE, "T1");
        testHierarchy("A1", ARTICLE, "A1");
        testHierarchy("A1P1", PART, "P1");
        testHierarchy("A1P1SP1", SUBPART, "SP1");
        testChildNode("1");
        testChildren("T1", "A1");
        testChildren("A1", "A1P1");
    }

    private void testChildNode(String locId) {
        LawBuilderTestHelper.testChildNode(locId, LawDocumentType.SECTION, code, builder);
    }

    private void testHierarchy(String locId, LawDocumentType type, String expected) {
        LawBuilderTestHelper.testHierarchy(locId, type, expected, code, builder);
    }

    /**
     * Tests that children are where expected.
     * @param parentLocId to find children within.
     * @param childLocIds to test for correct hierarchy.
     */
    private void testChildren(String parentLocId, String... childLocIds) {
        Optional<LawTreeNode> parent = builder.rootNode.findNode(code.name() + parentLocId, false);
        if (!parent.isPresent())
            fail();
        for (String childLocId : childLocIds)
            assertTrue(parent.get().find(code.name() + childLocId).isPresent());
    }

    /**
     * Helper method to initialize builder.
     * Does not initialize a root node if locId is an empty String.
     * @param lawId of the builder.
     */
    private void init(String lawId, String locId) {
        this.code = LawChapterCode.valueOf(lawId);
        this.builder = (HintBasedLawBuilder) AbstractLawBuilder.makeLawBuilder(new LawVersionId(lawId, LocalDate.now()), null);
        if (!locId.isEmpty()) {
            LawBlock root = LawTestUtils.getLawBlock(code, locId);
            this.builder.addInitialBlock(root, true, null);
        }
    }
}
