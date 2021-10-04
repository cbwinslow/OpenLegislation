package gov.nysenate.openleg.processors.bill.xml;

import gov.nysenate.openleg.config.annotation.IntegrationTest;
import gov.nysenate.openleg.legislation.bill.*;
import gov.nysenate.openleg.processors.BaseXmlProcessorTest;
import gov.nysenate.openleg.legislation.bill.dao.service.BillDataService;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.junit.Assert.assertTrue;

/**
 * Created by uros on 3/7/17.
 */
@Category(IntegrationTest.class)
public class XmlVetoMsgProcessorIT extends BaseXmlProcessorTest {

    @Autowired private BillDataService billDataService;

    /*
        Tested successfully, standard type of
        vetoMessage stored in the map with all its fields
    */
    @Test
    public void processStandardVetoMessage()  {
        String xmlPath = "processor/bill/vetomessage/2016-11-29-12.19.46.006100_VETOMSG_2016-00231.XML";
        processXmlFile(xmlPath);

        Bill bill = billDataService.getBill(new BaseBillId("A1984", 2016));

        VetoMessage vetoMessage = new VetoMessage();
        vetoMessage.setType(VetoType.STANDARD);
        vetoMessage.setMemoText("\n" +
                " \n" +
                "                         VETO MESSAGE - No. 231\n" +
                " \n" +
                "TO THE ASSEMBLY:\n" +
                " \n" +
                "I am returning herewith, without my approval, the following bill:\n" +
                " \n" +
                "Assembly Bill Number 1984, entitled:\n" +
                " \n" +
                "    \"AN  ACT to amend the executive law, in relation to requiring parole\n" +
                "      decisions to be published on a website\"\n" +
                " \n" +
                "    NOT APPROVED\n" +
                " \n" +
                "  This bill would require the Department of   Corrections and  Community\n" +
                "Supervision  (DOCCS) to publish redacted versions of Parole Board appeal\n" +
                "decisions on a searchable database, within sixty days  of  the  decision\n" +
                "being issued.\n" +
                " \n" +
                "  While  the intentions of this bill are laudable, compliance within the\n" +
                "bill's mandated time frame is not attainable.  The  bill  would  require\n" +
                "DOCCS  to  create an entirely new database and have it fully operational\n" +
                "within thirty days of becoming law. Yet there are multiple security  and\n" +
                "safety  concerns that would need to be addressed, including implementing\n" +
                "safeguards to ensure that victim identities are protected and access  is\n" +
                "appropriately  limited.  Moreover, no funds were appropriated to support\n" +
                "the new database. As such, this proposal is better suited for  consider-\n" +
                "ation during the annual State Budget negotiations.\n" +
                " \n" +
                "  Despite  my expressed interest in negotiating a workable solution that\n" +
                "would have extended the  effective  date  and  accommodated  appropriate\n" +
                "funding,  the  matter  was not resolved before the bill's delivery. I am\n" +
                "therefore constrained to veto this bill. However, I am  directing  DOCCS\n" +
                "and  the Board of Parole to work with the bill's sponsors and interested\n" +
                "stakeholders to develop a proposal that achieves the  policy  objectives\n" +
                "set forth in this bill.\n" +
                " \n" +
                "  The bill is disapproved.                    (signed) ANDREW M. CUOMO\n" +
                " \n" +
                "                               __________\n");
        vetoMessage.setBillId(new BaseBillId("A1984",2016));
        vetoMessage.setVetoNumber(231);
        vetoMessage.setSigner("ANDREW M. CUOMO");
        vetoMessage.setYear(2016);

        VetoId id = new VetoId(vetoMessage.getYear(), vetoMessage.getVetoNumber());

        VetoMessage storedMsgObject = bill.getVetoMessages().get(id);

        assertTrue(storedMsgObject.getMemoText().equals(vetoMessage.getMemoText()));
        assertTrue(storedMsgObject.getBillId().equals(vetoMessage.getBillId()));
        assertTrue(storedMsgObject.getSigner().equals(vetoMessage.getSigner()));
        assertTrue(storedMsgObject.getType().equals(vetoMessage.getType()));
        assertTrue(storedMsgObject.getYear() ==vetoMessage.getYear());
    }

    @Test
    public void lineTypeVetoMessage()   {
        String xmlPath = "processor/bill/vetomessage/2016-11-17-09.59.11.184200_VETOMSG_2016-00002.XML";
        processXmlFile(xmlPath);

        Bill bill = billDataService.getBill(new BaseBillId("A9000", 2016));

        VetoMessage vetoMessage = new VetoMessage();
        vetoMessage.setType(VetoType.LINE_ITEM);

        vetoMessage.setBillId(new BaseBillId("A9000",2016));

        vetoMessage.setMemoText("\n" +
                " \n" +
                "                  STATE OF NEW YORK--EXECUTIVE CHAMBER\n" +
                " \n" +
                "TO THE ASSEMBLY:                                        April 13, 2016\n" +
                " \n" +
                "     I  hereby transmit pursuant to the provisions of section 7 of Arti-\n" +
                "cle IV and section 4 of Article VII of the Constitution, a statement  of\n" +
                "items  to which I object and which I do not approve, contained in Assem-\n" +
                "bly Bill Number 9000--D, entitled:\n" +
                " \n" +
                "CHAPTER 50\n" +
                " \n" +
                "LINE VETO #2\n" +
                " \n" +
                "\"AN ACT making appropriations for the support of government\n" +
                " \n" +
                "                          STATE OPERATIONS BUDGET\"\n" +
                " \n" +
                "Bill Page 124, Line 31 through Line 35, inclusive\n" +
                " \n" +
                "NOT APPROVED\n" +
                "____________\n" +
                " \n" +
                "                          EDUCATION DEPARTMENT\n" +
                " \n" +
                " \"For services and expenses for the supervision of  institutions  regis-\n" +
                "    tered  pursuant  to  section  5001  of  the  education  law, and for\n" +
                "    services and expenses of supervisory programs and payment of associ-\n" +
                "    ated indirect costs and general state charges.\n" +
                "  Personal service--regular ... 1,747,000 ............... (re. $200,000)\"\n" +
                " \n" +
                "This item passed by the Legislature,  to  which  I  object  and  do  not\n" +
                "approve,  is  not needed because adequate funding for State agency oper-\n" +
                "ations is already provided for in the budget. Accordingly, this item  is\n" +
                "disapproved.\n" +
                " \n" +
                "                                              (signed) ANDREW M. CUOMO\n");
        vetoMessage.setVetoNumber(002);
        vetoMessage.setSigner("ANDREW M. CUOMO");
        vetoMessage.setYear(2016);
        vetoMessage.setSignedDate(LocalDate.of(2016,4,13));
        vetoMessage.setChapter(50);
        vetoMessage.setBillPage(124);
        vetoMessage.setLineStart(31);
        vetoMessage.setLineEnd(35);

        VetoId id = new VetoId(vetoMessage.getYear(), vetoMessage.getVetoNumber());

        VetoMessage storedMsgObject = bill.getVetoMessages().get(id);

        assertTrue(storedMsgObject.getMemoText().equals(vetoMessage.getMemoText()));
        assertTrue(storedMsgObject.getBillId().equals(vetoMessage.getBillId()));
        assertTrue(storedMsgObject.getSigner().equals(vetoMessage.getSigner()));
        assertTrue(storedMsgObject.getType().equals(vetoMessage.getType()));
        assertTrue(storedMsgObject.getSignedDate().equals(vetoMessage.getSignedDate()));
        assertTrue(storedMsgObject.getYear() ==vetoMessage.getYear());
        assertTrue(storedMsgObject.getChapter() == vetoMessage.getChapter());
        assertTrue(storedMsgObject.getBillPage() == vetoMessage.getBillPage());
        assertTrue(storedMsgObject.getLineEnd() == vetoMessage.getLineEnd());
        assertTrue(storedMsgObject.getLineStart() == vetoMessage.getLineStart());
    }

    @Test
    public void removeMessage() {
        //adding Veto message
        String xmlPath = "processor/bill/vetomessage/2016-11-17-09.59.11.184200_VETOMSG_2016-00002.XML";
        processXmlFile(xmlPath);

        Bill bill = billDataService.getBill(new BaseBillId("A9000", 2016));

        VetoMessage vetoMessage = new VetoMessage();
        vetoMessage.setBillId(new BaseBillId("A9000",2016));
        vetoMessage.setYear(2016);
        vetoMessage.setVetoNumber(002);


        VetoId id = new VetoId(vetoMessage.getYear(), vetoMessage.getVetoNumber());

        VetoMessage storedMsgObject = bill.getVetoMessages().get(id);

        //checking if the vetoMessage is in the map
        assertTrue(storedMsgObject.getBillId().equals(vetoMessage.getBillId()));

        //removing vetoMessage
        String xmlPath1 = "processor/bill/vetomessage/2016-11-28-23.56.28.935222_VETOMSG_2016-00233.XML";
        processXmlFile(xmlPath1);

        Bill bill1 = billDataService.getBill(new BaseBillId("A9000", 2016));
        VetoMessage removedMsgObject = bill1.getVetoMessages().get(id);

        assertTrue(removedMsgObject == null);
    }
}
