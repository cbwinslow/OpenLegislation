package gov.nysenate.openleg.api.legislation.bill.view;

import gov.nysenate.openleg.api.ViewObject;
import gov.nysenate.openleg.legislation.bill.BillInfo;

import java.time.LocalDateTime;

/**
 * Even simpler bill info view containing just the id, type, and title of the bill.
 */
public class SimpleBillInfoView extends BaseBillIdView implements ViewObject
{
    protected String printNo;
    protected BillTypeView billType;
    protected String title;
    protected String activeVersion;
    protected int year;
    protected LocalDateTime publishedDateTime;
    protected BaseBillIdView substitutedBy;
    protected SponsorView sponsor;
    protected BaseBillIdView reprintOf;


    public SimpleBillInfoView(BillInfo billInfo) {
        super(billInfo != null ? billInfo.getBillId() : null);
        if (billInfo != null) {
            title = billInfo.getTitle();
            activeVersion = billInfo.getActiveVersion() != null ? billInfo.getActiveVersion().toString() : null;
            printNo = basePrintNo + (activeVersion!=null ? activeVersion : "");
            year = billInfo.getYear();
            publishedDateTime = billInfo.getPublishedDateTime();
            substitutedBy = billInfo.getSubstitutedBy() != null ? new BaseBillIdView(billInfo.getSubstitutedBy()) : null;
            sponsor = billInfo.getSponsor() != null ? new SponsorView(billInfo.getSponsor()) : null;
            billType = billInfo.getBillId() != null && billInfo.getBillId().getBillType() != null
                    ? new BillTypeView(billInfo.getBillId().getBillType()) : null;
            reprintOf = billInfo.getReprintOf() != null ? new BaseBillIdView( billInfo.getReprintOf() ): null;
        }
    }

    protected SimpleBillInfoView(){
        super();
    }

    public String getPrintNo() {
        return printNo;
    }

    public BillTypeView getBillType() {
        return billType;
    }

    public String getTitle() {
        return title;
    }

    public String getActiveVersion() {
        return activeVersion;
    }

    public int getYear() {
        return year;
    }

    public LocalDateTime getPublishedDateTime() {
        return publishedDateTime;
    }

    public BaseBillIdView getSubstitutedBy() {
        return substitutedBy;
    }

    public SponsorView getSponsor() {
        return sponsor;
    }

    public BaseBillIdView getReprintOf() {
        return reprintOf;
    }

    /**
     * Use by jackson serialization
     * @param date the date in String form
     */
    public void setPublishedDateTime(String date){
        publishedDateTime = LocalDateTime.parse(date);
    }

    @Override
    public String getViewType() {
        return "simple-bill-info";
    }
}
