package gov.nysenate.openleg.updates.bill;

import com.google.common.collect.Range;
import gov.nysenate.openleg.BaseTests;
import gov.nysenate.openleg.config.annotation.SillyTest;
import gov.nysenate.openleg.common.dao.LimitOffset;
import gov.nysenate.openleg.common.dao.PaginatedList;
import gov.nysenate.openleg.common.dao.SortOrder;
import gov.nysenate.openleg.processors.sourcefile.SourceFileRefDao;
import gov.nysenate.openleg.processors.sourcefile.sobi.LegDataFragmentDao;
import gov.nysenate.openleg.legislation.bill.*;
import gov.nysenate.openleg.legislation.PublishStatus;
import gov.nysenate.openleg.legislation.bill.Version;
import gov.nysenate.openleg.processors.bill.sobi.SobiFile;
import gov.nysenate.openleg.processors.bill.LegDataFragment;
import gov.nysenate.openleg.processors.bill.LegDataFragmentType;
import gov.nysenate.openleg.updates.UpdateDigest;
import gov.nysenate.openleg.updates.UpdateType;
import gov.nysenate.openleg.legislation.bill.dao.service.BillDataService;
import gov.nysenate.openleg.common.util.OutputUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Category(SillyTest.class)
public class BillUpdatesTest extends BaseTests {

    private static final Logger logger = LoggerFactory.getLogger(BillUpdatesTest.class);

    @Autowired private BillDataService billDataService;
    @Autowired private BillUpdatesDao billUpdatesDao;
    @Autowired private SourceFileRefDao sourceFileRefDao;
    @Autowired private LegDataFragmentDao fragmentDao;

    private static final BaseBillId testBillId = new BaseBillId("S1537", 2017);
    private static final String testSobiFileName =
            LocalDateTime.now().format(DateTimeFormatter.ofPattern(SobiFile.sobiDateFullPattern));
    private static final File testFile = new File("/tmp/" + testSobiFileName);

    private Bill testBill;
    private LegDataFragment testFragment;
    private LocalDateTime testStart;

    @Before
    public void setup() throws IOException {
        testStart = LocalDateTime.now();
        boolean created = testFile.createNewFile();
        if (!created) {
            throw new IllegalStateException(testFile + " was not created as expected");
        }
        SobiFile testSobiFile = new SobiFile(testFile);
        sourceFileRefDao.updateSourceFile(testSobiFile);
        testFragment = new LegDataFragment(testSobiFile, LegDataFragmentType.BILL, "test", 1);
        fragmentDao.updateLegDataFragment(testFragment);
        testBill = billDataService.getBill(testBillId);
    }

    @After
    public void cleanup() {
        boolean deleted = testFile.delete();
        if (!deleted) {
            throw new IllegalStateException(testFile + " was not deleted as expected");
        }
    }

    @Test
    public void summaryUpdateTest() throws InterruptedException {
        testBill.setSummary("blorgatron");

        updateBillAndLogUpdates();
    }

    @Test
    public void newAmendTest() {
        Version nextVersion = Version.after(testBill.getActiveVersion()).get(0);
        BillAmendment amendment = new BillAmendment(testBillId, nextVersion);
        testBill.addAmendment(amendment);
        testBill.updatePublishStatus(nextVersion, new PublishStatus(false, LocalDateTime.now()));
        testBill.setActiveVersion(nextVersion);

        billDataService.saveBill(testBill, testFragment, false);
        testBill.updatePublishStatus(nextVersion, new PublishStatus(true, LocalDateTime.now()));
        updateBillAndLogUpdates();
    }

    @Test
    public void newSameAsTest() {
        BillAmendment activeAmendment = testBill.getActiveAmendment();
        Set<BillId> sameAs = new HashSet<>(activeAmendment.getSameAs());
        sameAs.add(new BillId("A100B", 2017));
        activeAmendment.setSameAs(sameAs);

        billDataService.saveBill(testBill, testFragment, false);

        sameAs = new HashSet<>(activeAmendment.getSameAs());
        sameAs.add(new BillId("A200B", 2017));
        activeAmendment.setSameAs(sameAs);

        billDataService.saveBill(testBill, testFragment, false);

        activeAmendment.setSameAs(Collections.emptySet());

        updateBillAndLogUpdates();
    }

    @Test
    public void newSubstitutionTest() {
        testBill.setSubstitutedBy(new BaseBillId("S99999", 2017));

        updateBillAndLogUpdates();
    }

    @Test
    public void newStatusTest() {
        testBill.setStatus(new BillStatus(BillStatusType.LOST, LocalDate.now()));

        updateBillAndLogUpdates();
    }

    @Test
    public void newPrevVersionTest() {
        testBill.setDirectPreviousVersion(new BaseBillId("S123", 2017));

        billDataService.saveBill(testBill, testFragment, false);
        testBill.setDirectPreviousVersion(new BaseBillId("S1234", 2017));

        updateBillAndLogUpdates();
    }

    /* --- Internal Methods --- */

    private PaginatedList<UpdateDigest<BaseBillId>> getUpdates() {
        Range<LocalDateTime> updateRange = Range.atLeast(testStart);
        return billUpdatesDao.getDetailedUpdates(
                updateRange, UpdateType.PROCESSED_DATE, null, SortOrder.ASC, LimitOffset.ALL);
    }

    private void updateBillAndLogUpdates() {
        billDataService.saveBill(testBill, testFragment, false);
        PaginatedList<UpdateDigest<BaseBillId>> detailedUpdates = getUpdates();

        logger.info(OutputUtils.toJson(detailedUpdates));
    }

}
