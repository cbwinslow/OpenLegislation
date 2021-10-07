package gov.nysenate.openleg.processors.bill.xml;

import gov.nysenate.openleg.legislation.SessionYear;
import gov.nysenate.openleg.legislation.bill.Version;
import gov.nysenate.openleg.legislation.bill.BaseBillId;
import gov.nysenate.openleg.legislation.bill.Bill;
import gov.nysenate.openleg.legislation.bill.BillId;
import gov.nysenate.openleg.processors.AbstractLegDataProcessor;
import gov.nysenate.openleg.processors.log.DataProcessUnit;
import gov.nysenate.openleg.processors.bill.LegDataFragment;
import gov.nysenate.openleg.processors.bill.LegDataFragmentType;
import gov.nysenate.openleg.processors.AbstractDataProcessor;
import gov.nysenate.openleg.processors.ParseError;
import gov.nysenate.openleg.processors.LegDataProcessor;
import gov.nysenate.openleg.common.util.XmlHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

/**
 * This class process the Anact Sobi fragments.
 * <p>
 * Created by Robert Bebber on 2/15/17.
 */
@Service
public class XmlAnActProcessor extends AbstractLegDataProcessor {

    private static final Logger logger = LoggerFactory.getLogger(XmlAnActProcessor.class);

    /**
     * This method allows for the retrieval of the LegDataFragmentType
     */
    @Override
    public LegDataFragmentType getSupportedType() {
        return LegDataFragmentType.ANACT;
    }

    /**
     * This method gets the specific identifiers for the fragment and then based between remove and replace, the
     * method will set the act clause to the appropriate text value.
     *
     * @param legDataFragment This is the fragment being worked on during this process
     */
    @Override
    public void process(LegDataFragment legDataFragment) {
        logger.info("Processing AnAct...");
        logger.info("Processing " + legDataFragment.getFragmentId() + " (xml file).");
        DataProcessUnit unit = createProcessUnit(legDataFragment);
        try {
            final Document doc = xmlHelper.parse(legDataFragment.getText());
            final Node billTextNode = xmlHelper.getNode("anact", doc);
            final Integer anactno = xmlHelper.getInteger("@billno", billTextNode);
            final String anacthse = xmlHelper.getString("@billhse", billTextNode).trim();
            final String anactamd = xmlHelper.getString("@billamd", billTextNode).trim();
            final Integer sessyr = xmlHelper.getInteger("@sessyr", billTextNode);
            final String action = xmlHelper.getString("@action", billTextNode).trim();
            final String anactClause = billTextNode.getTextContent().trim();
            final Version version = Version.of(anactamd);
            final Bill baseAnAct = getOrCreateBaseBill(new BillId(
                    new BaseBillId(anacthse + anactno, new SessionYear(sessyr)),
                    Version.of(anactamd)), legDataFragment);
            if (action.equals("replace")) {
                baseAnAct.getAmendment(version).setActClause(anactClause);
            } else if (action.equals("remove")) {
                baseAnAct.getAmendment(version).setActClause("");
            }
            baseAnAct.setModifiedDateTime(legDataFragment.getPublishedDateTime());
            billIngestCache.set(baseAnAct.getBaseBillId(), baseAnAct, legDataFragment);

        } catch (IOException | SAXException | XPathExpressionException e) {
            unit.addException("XML AnAct parsing error", e);
            throw new ParseError("Error While Parsing AnActXML", e);
        }
        finally {
            postDataUnitEvent(unit);
            checkIngestCache();
        }
    }
}
