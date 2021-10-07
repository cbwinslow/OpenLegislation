package gov.nysenate.openleg.api.legislation.agenda.view;

import gov.nysenate.openleg.api.ViewObject;
import gov.nysenate.openleg.api.legislation.member.view.MemberView;
import gov.nysenate.openleg.legislation.agenda.AgendaVoteAttendance;

public class AgendaAttendanceView implements ViewObject
{
    private MemberView member;
    private int rank;
    private String party;
    private String attend;

    public AgendaAttendanceView(AgendaVoteAttendance attendance) {
        if (attendance != null) {
            this.member = new MemberView(attendance.getMember());
            this.rank = attendance.getRank();
            this.party = attendance.getParty();
            this.attend = attendance.getAttendStatus();
        }
    }

    //Added for Json Deserialization
    protected AgendaAttendanceView() {}

    public MemberView getMember() {
        return member;
    }

    public int getRank() {
        return rank;
    }

    public String getParty() {
        return party;
    }

    public String getAttend() {
        return attend;
    }

    @Override
    public String getViewType() {
        return "agenda-attendance";
    }
}
