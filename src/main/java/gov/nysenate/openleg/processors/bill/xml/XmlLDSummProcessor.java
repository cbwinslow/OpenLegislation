package gov.nysenate.openleg.processors.bill.xml;

import gov.nysenate.openleg.processors.AbstractLegDataProcessor;
import gov.nysenate.openleg.processors.bill.BillLawCodeParser;
import gov.nysenate.openleg.legislation.SessionYear;
import gov.nysenate.openleg.legislation.bill.Version;
import gov.nysenate.openleg.legislation.bill.Bill;
import gov.nysenate.openleg.legislation.bill.BillAmendment;
import gov.nysenate.openleg.legislation.bill.BillId;
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
 * Created by Chenguang He(gaoyike@gmail.com) on 2016/12/1.
 */
@Service
public class XmlLDSummProcessor extends AbstractLegDataProcessor {

    private static final Logger logger = LoggerFactory.getLogger(XmlLDSummProcessor.class);

    @Override
    public LegDataFragmentType getSupportedType() {
        return LegDataFragmentType.LDSUMM;
    }

    @Override
    public void process(LegDataFragment legDataFragment) {
        logger.info("Processing " + legDataFragment.getFragmentId() + " (xml file).");
        DataProcessUnit unit = createProcessUnit(legDataFragment);
        try {
            final Document doc = xmlHelper.parse(legDataFragment.getText());
            final Node billTextNode = xmlHelper.getNode("digestsummary", doc);
            final int sessionYear = xmlHelper.getInteger("@sessyr", billTextNode);
            final String billhse = xmlHelper.getString("@billhse", billTextNode);
            final String billno = xmlHelper.getString("@billno", billTextNode);
            final String action = xmlHelper.getString("@action", billTextNode);
            final String summary = xmlHelper.getNode("digestsummary/summary", doc) == null ? "" : xmlHelper.getNode("digestsummary/summary", doc).getTextContent().replaceAll("º","§").replaceAll("\n"," ").trim();
            final String amd = xmlHelper.getString("digestsummary/summaryamendment", doc);
            final Version version = Version.of(amd);
            final String lawCode = xmlHelper.getString("law", billTextNode).replaceAll("Â", "¶").replaceAll("º","§").replaceAll("([\n\t])"," ").replaceAll(" +"," ").trim();
            final Bill baseBill = getOrCreateBaseBill(new BillId(billhse + billno, new SessionYear(sessionYear), version), legDataFragment);
            baseBill.setSummary(summary);
            BillAmendment amendment = baseBill.getAmendment(version);
            amendment.setLawCode(lawCode);
            String json = BillLawCodeParser.parse(amendment.getLawCode(), baseBill.hasValidLaws(version));
            amendment.setRelatedLawsJson(json);

            if (action.equals("replace")) { //replace bill
                // add previous bills
                int totalNumsOfPreBills = xmlHelper.getNodeList("digestsummary/oldbill/oldyear", doc).getLength();
                for (int i = 1; i <= totalNumsOfPreBills; i++) {
                    int sess = xmlHelper.getInteger("digestsummary/oldbill/oldyear[" + i + "]", doc);
                    String oldhse = xmlHelper.getString("digestsummary/oldbill/oldhse[" + i + "]", doc).replaceAll("\n", "");
                    String oldno = xmlHelper.getString("digestsummary/oldbill/oldno[" + i + "]", doc).replaceAll("\n", "");
                    String oldamd = xmlHelper.getString("digestsummary/oldbill/oldamd[" + i + "]", doc).replaceAll("\n", "");
                    if (oldno.isEmpty() || oldhse.isEmpty())
                        break;
                    baseBill.setDirectPreviousVersion(new BillId(oldhse + oldno, SessionYear.of(sess), Version.of(oldamd)));
                }
            } else { //remove bill
                baseBill.getAllPreviousVersions().clear();
                baseBill.setDirectPreviousVersion(null);
            }
            baseBill.setModifiedDateTime(legDataFragment.getPublishedDateTime());
            billIngestCache.set(baseBill.getBaseBillId(), baseBill, legDataFragment);
            logger.info("Put base bill in the ingest cache.");

        } catch (IOException | SAXException | XPathExpressionException e) {
            unit.addException("XML LD Summ parsing error", e);
            throw new ParseError("Error While Parsing Bill Digest XML : " + legDataFragment.getFragmentId(), e);
        } finally {
            postDataUnitEvent(unit);
            checkIngestCache();
        }
    }
}
