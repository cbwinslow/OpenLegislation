package gov.nysenate.openleg.api.legislation.calendar.view;

import gov.nysenate.openleg.api.MapView;
import gov.nysenate.openleg.legislation.bill.Version;
import gov.nysenate.openleg.legislation.calendar.Calendar;
import gov.nysenate.openleg.legislation.bill.dao.service.BillDataService;

import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CalendarView extends CalendarIdView {

    protected CalendarSupView floorCalendar;
    protected MapView<String, CalendarSupView> supplementalCalendars;
    protected MapView<Integer, ActiveListView> activeLists;
    protected LocalDate calDate;

    public CalendarView(Calendar calendar, BillDataService billDataService) {
        super(calendar.getId());
        if (calendar.getSupplemental(Version.ORIGINAL) != null) {
            this.floorCalendar = new CalendarSupView(calendar.getSupplemental(Version.ORIGINAL), billDataService);
        }
        this.supplementalCalendars = MapView.of((Map<String, CalendarSupView>)
                calendar.getSupplementalMap().values().stream()
                        .filter((calSup) -> !calSup.getVersion().equals(Version.ORIGINAL))
                        .map(calSup -> new CalendarSupView(calSup, billDataService))
                        .collect(Collectors.toMap(SimpleCalendarSupView::getVersion, Function.identity(),
                                (a, b) -> b, TreeMap::new))
        );
        this.activeLists = MapView.of((Map<Integer, ActiveListView>)
                calendar.getActiveListMap().values().stream()
                        .map(activeList -> new ActiveListView(activeList, billDataService))
                        .collect(Collectors.toMap(ActiveListView::getSequenceNumber, Function.identity(),
                                (a, b) -> b, TreeMap::new))
        );
        calDate = calendar.getCalDate();
    }

    //Added for Json deserialization
    protected CalendarView() {}

    public CalendarSupView getFloorCalendar() {
        return floorCalendar;
    }

    public MapView<String, CalendarSupView> getSupplementalCalendars() {
        return supplementalCalendars;
    }

    public MapView<Integer, ActiveListView> getActiveLists() {
        return activeLists;
    }

    public LocalDate getCalDate() {
        return calDate;
    }

    @Override
    public String getViewType() {
        return "calendar";
    }
}
