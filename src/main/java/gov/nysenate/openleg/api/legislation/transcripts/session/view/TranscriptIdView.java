package gov.nysenate.openleg.api.legislation.transcripts.session.view;

import gov.nysenate.openleg.api.ViewObject;
import gov.nysenate.openleg.legislation.transcripts.session.TranscriptId;

public class TranscriptIdView implements ViewObject
{
    protected String dateTime;

    public TranscriptIdView(TranscriptId transcriptId) {
        this.dateTime = transcriptId.getDateTime().toString();
    }

    public String getDateTime() {
        return dateTime;
    }

    @Override
    public String getViewType() {
        return "transcript-id";
    }
}
