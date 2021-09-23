package gov.nysenate.openleg.api.processor.view;

import gov.nysenate.openleg.api.response.ListViewResponse;
import gov.nysenate.openleg.api.ViewObject;
import gov.nysenate.openleg.common.dao.PaginatedList;
import gov.nysenate.openleg.processors.log.DataProcessRunInfo;
import gov.nysenate.openleg.processors.log.DataProcessUnit;

import static java.util.stream.Collectors.toList;

public class DataProcessRunDetailView extends DataProcessRunInfoView implements ViewObject
{
    protected ListViewResponse<DataProcessUnitView> details;

    /** --- Constructors --- */

    public DataProcessRunDetailView(DataProcessRunInfo runInfo, PaginatedList<DataProcessUnit> units) {
        super(runInfo);
        if (units != null) {
            this.details = ListViewResponse.of(
                units.getResults().stream().map(DataProcessUnitView::new).collect(toList()),
                units.getTotal(), units.getLimOff());
        }
    }

    @Override
    public String getViewType() {
        return "data-process-run-detail";
    }

    /** --- Basic Getters --- */

    public ListViewResponse<DataProcessUnitView> getDetails() {
        return details;
    }
}
