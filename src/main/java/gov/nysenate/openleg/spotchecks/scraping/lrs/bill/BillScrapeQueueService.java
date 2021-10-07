package gov.nysenate.openleg.spotchecks.scraping.lrs.bill;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import gov.nysenate.openleg.config.Environment;
import gov.nysenate.openleg.legislation.bill.BaseBillId;
import gov.nysenate.openleg.legislation.bill.BillId;
import gov.nysenate.openleg.legislation.bill.BillUpdateField;
import gov.nysenate.openleg.spotchecks.model.SpotCheckMismatchType;
import gov.nysenate.openleg.updates.bill.BillFieldUpdateEvent;
import gov.nysenate.openleg.spotchecks.base.SpotcheckMismatchEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/** Adds bills to the bill text scrape queue based on certain events */
@Service
public class BillScrapeQueueService {
    private static final Logger logger = LoggerFactory.getLogger(BillScrapeQueueService.class);

    @Autowired
    BillScrapeReferenceDao btrDao;

    @Autowired
    EventBus eventBus;

    @Autowired
    Environment env;

    @PostConstruct
    public void init() {
        eventBus.register(this);
    }

    /**
     * Adds a bill to the scrape queue in response to a full text update event
     * @param updateEvent BillFieldUpdateEvent
     */
    @Subscribe
    public void handleBillFullTextUpdate(BillFieldUpdateEvent updateEvent) {
        if (!env.isBillScrapeQueueEnabled()) {
            return;
        }
        if (BillUpdateField.FULLTEXT.equals(updateEvent.getUpdateField())) {
            logger.info("adding {} to bill scrape queue after full text update", updateEvent.getBillId());
            btrDao.addBillToScrapeQueue(updateEvent.getBillId(), ScrapeQueuePriority.UPDATE_TRIGGERED.getPriority());
        }
        if (BillUpdateField.VOTE.equals(updateEvent.getUpdateField())) {
            logger.info("adding {} to bill scrape queue after vote update", updateEvent.getBillId());
            btrDao.addBillToScrapeQueue(updateEvent.getBillId(), ScrapeQueuePriority.UPDATE_TRIGGERED.getPriority());
        }
    }

    /**
     * Adds a bill the the scrape queue in response to a page count spotcheck mismatch
     * @param mismatchEvent SpotcheckMismatchEvent<BaseBillId>
     */
    @Subscribe
    public void handlePageCountSpotcheckMismatch(SpotcheckMismatchEvent<BillId> mismatchEvent) {
        if (SpotCheckMismatchType.BILL_FULLTEXT_PAGE_COUNT.equals(mismatchEvent.getMismatch().getMismatchType()) &&
                env.isBillScrapeQueueEnabled()) {
            logger.info("adding {} to bill scrape queue after spotcheck", mismatchEvent.getContentId());
            btrDao.addBillToScrapeQueue(BaseBillId.of(mismatchEvent.getContentId()),
                    ScrapeQueuePriority.SPOTCHECK_TRIGGERED.getPriority());
        }
    }
}
