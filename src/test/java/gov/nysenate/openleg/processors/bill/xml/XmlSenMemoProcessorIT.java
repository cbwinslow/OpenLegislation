package gov.nysenate.openleg.processors.bill.xml;

import gov.nysenate.openleg.config.annotation.IntegrationTest;
import gov.nysenate.openleg.legislation.bill.BillId;
import gov.nysenate.openleg.processors.BaseXmlProcessorTest;
import gov.nysenate.openleg.legislation.bill.exception.BillAmendNotFoundEx;
import gov.nysenate.openleg.legislation.bill.exception.BillNotFoundEx;
import gov.nysenate.openleg.common.util.FileIOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;

import static org.junit.Assert.*;


/**
 * Created by uros on 3/23/17.
 */
@Category(IntegrationTest.class)
public class XmlSenMemoProcessorIT extends BaseXmlProcessorTest {

    @Test
    public void processSimpleSenMemo() throws IOException {
        final String xmlPath = "processor/bill/senmemo/2016-11-17-10.10.49.554307_SENMEMO_S00100.XML";
        final String expectedPath = "processor/bill/senmemo/S100_expected.txt";
        final String expected = FileIOUtils.getResourceFileContents(expectedPath);
        final BillId billId = new BillId("S100", 2015);

        assertNotEquals(expected, getMemoSafe(billId));
        processXmlFile(xmlPath);
        assertEquals(expected, getMemo(billId));
    }

    @Test
    public void processAmendmentSenMemo() throws IOException {
        final String xmlPath = "processor/bill/senmemo/2017-02-08-18.50.01.467654_SENMEMO_S00957A.XML";
        final String expectedPath = "processor/bill/senmemo/S957A_expected.txt";
        final String expected = FileIOUtils.getResourceFileContents(expectedPath);
        final BillId billId = new BillId("S957A", 2017);

        assertNotEquals(expected, getMemoSafe(billId));
        processXmlFile(xmlPath);
        assertEquals(expected, getMemo(billId));
    }

    @Test
    public void memoRemoveTest() {
        final String preXmlPath = "processor/bill/senmemo/2016-11-17-10.10.49.554307_SENMEMO_S00100.XML";
        final String xmlPath = "processor/bill/senmemo/2016-11-17-11.00.00.000000_SENMEMO_S00100.XML";
        final BillId billId = new BillId("S100", 2015);

        processXmlFile(preXmlPath);
        assertFalse(StringUtils.isBlank(getMemo(billId)));
        processXmlFile(xmlPath);
        assertTrue(StringUtils.isBlank(getMemo(billId)));
    }

    /* --- Internal Methods --- */

    private String getMemo(BillId billId) {
        return getAmendment(billId).getMemo();
    }

    private String getMemoSafe(BillId billId) {
        try {
            return getMemo(billId);
        } catch (BillNotFoundEx | BillAmendNotFoundEx ex) {
            return null;
        }
    }
}
