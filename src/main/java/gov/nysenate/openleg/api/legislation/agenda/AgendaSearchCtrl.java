package gov.nysenate.openleg.api.legislation.agenda;

import gov.nysenate.openleg.api.response.BaseResponse;
import gov.nysenate.openleg.api.response.ListViewResponse;
import gov.nysenate.openleg.api.legislation.agenda.view.AgendaCommFlatView;
import gov.nysenate.openleg.api.legislation.agenda.view.CommAgendaSummaryView;
import gov.nysenate.openleg.api.search.view.SearchResultView;
import gov.nysenate.openleg.api.BaseCtrl;
import gov.nysenate.openleg.common.dao.LimitOffset;
import gov.nysenate.openleg.legislation.agenda.CommitteeAgendaId;
import gov.nysenate.openleg.search.SearchException;
import gov.nysenate.openleg.search.SearchResults;
import gov.nysenate.openleg.legislation.agenda.dao.AgendaDataService;
import gov.nysenate.openleg.search.agenda.AgendaSearchService;
import gov.nysenate.openleg.legislation.bill.dao.service.BillDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.stream.Collectors;

import static gov.nysenate.openleg.api.BaseCtrl.BASE_API_PATH;

@RestController
@RequestMapping(value = BASE_API_PATH + "/agendas/", method = RequestMethod.GET)
public class AgendaSearchCtrl extends BaseCtrl
{
    private static final Logger logger = LoggerFactory.getLogger(AgendaSearchCtrl.class);

    @Autowired private AgendaDataService agendaData;
    @Autowired private AgendaSearchService agendaSearch;
    @Autowired private BillDataService billData;

    /**
     * Agenda Search API
     * -----------------
     *
     * Search agendas across all year: (GET) /api/3/agendas/search
     * @see #searchAgendas(int, String, String, boolean, WebRequest)
     * for query parameter details.
     */
    @RequestMapping(value = "/search")
    public BaseResponse searchAgendas(@RequestParam(required = true) String term,
                                      @RequestParam(defaultValue = "") String sort,
                                      @RequestParam(defaultValue = "false") boolean full,
                                      WebRequest webRequest) throws SearchException {
        LimitOffset limOff = getLimitOffset(webRequest, 25);
        SearchResults<CommitteeAgendaId> results = agendaSearch.searchCommitteeAgendas(term, sort, limOff);
        return getAgendaSearchResponse(full, limOff, results);
    }

    /**
     * Agenda Search API
     * -----------------
     *
     * Search agendas for a given year: (GET) /api/3/agendas/{year}/search
     * Request Parameters:              term - The lucene query string
     *                                  sort - The lucene sort string
     *                                  full - Receive full agenda committee views
     *                                  limit - Limit the number of results
     *                                  offset - Start the results from offset
     */
    @RequestMapping(value = "/{year:[\\d]{4}}/search")
    public BaseResponse searchAgendas(@PathVariable int year,
                                      @RequestParam(required = true) String term,
                                      @RequestParam(defaultValue = "") String sort,
                                      @RequestParam(defaultValue = "false") boolean full,
                                      WebRequest webRequest) throws SearchException {
        LimitOffset limOff = getLimitOffset(webRequest, 25);
        SearchResults<CommitteeAgendaId> results = agendaSearch.searchCommitteeAgendas(term, year, sort, limOff);
        return getAgendaSearchResponse(full, limOff, results);
    }

    private BaseResponse getAgendaSearchResponse(boolean full, LimitOffset limOff, SearchResults<CommitteeAgendaId> results) {
        return ListViewResponse.of(
            results.getResults().stream()
                .map(r ->
                    new SearchResultView((full)
                        ? new AgendaCommFlatView(agendaData.getAgenda(r.getResult().getAgendaId()),
                                                 r.getResult().getCommitteeId(), billData)
                        : new CommAgendaSummaryView(r.getResult(), agendaData.getAgenda(r.getResult().getAgendaId())),
                    r.getRank()))
                .collect(Collectors.toList()), results.getTotalResults(), limOff);
    }
}
