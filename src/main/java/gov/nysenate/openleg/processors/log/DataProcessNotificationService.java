package gov.nysenate.openleg.processors.log;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import gov.nysenate.openleg.config.Environment;
import gov.nysenate.openleg.notifications.model.Notification;
import gov.nysenate.openleg.processors.DataProcessor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

import static gov.nysenate.openleg.notifications.model.NotificationType.*;

@Service
public class DataProcessNotificationService {

    @Autowired
    private EventBus eventBus;

    @Autowired
    Environment environment;

    @Autowired
    DataProcessor dataProcessor;

    private static final String dataProcessRunPath = "/api/3/admin/process/runs/id";

    @PostConstruct
    public void init() {
        eventBus.register(this);
    }

    /**
     * Generates and dispatches a data process exception notification based on an exception
     * @param ex Throwable
     */
    public void exceptionNotification(Throwable ex, int dataProcessId) {
        LocalDateTime occurred = LocalDateTime.now();
        String summary = "Processing Exception at " + occurred + " - " + ExceptionUtils.getStackFrames(ex)[0];
        String message = getDataProcessRunUrl(dataProcessId) +
                "\nThe following exception occurred while processing data at " + occurred + ":\n" +
                ExceptionUtils.getStackTrace(ex);
        Notification notification = new Notification(PROCESS_EXCEPTION, occurred, summary, message);

        eventBus.post(notification);
    }

    public void warningNotification(String warning, int dataProcessId, DataProcessUnit unit) {
        LocalDateTime occurred = unit.getEndDateTime();
        String summary = "Non-fatal Processing exception at " + occurred;
        String message = getDataProcessRunUrl(dataProcessId) + "\n" +
                "action: " + unit.getAction() + " " + unit.getSourceType() + ": " + unit.getSourceId() + "\n" +
                "The following non-fatal exception occurred while processing data at " + occurred + ":\n" + warning;
        Notification notification = new Notification(PROCESS_WARNING, occurred, summary, message);

        eventBus.post(notification);
    }

    @Subscribe
    public void handleDataProcessErrorEvent(DataProcessErrorEvent event) {
        exceptionNotification(event.getEx(), event.getProcessRunId());
    }

    @Subscribe
    public void handleDataProcessWarnEvent(DataProcessWarnEvent event) {
        event.getUnit().getErrors()
                .forEach(warning -> warningNotification(warning, event.getDataProcessId(), event.getUnit()));
    }

    private String getDataProcessRunUrl(int dataProcessId) {
        return environment.getUrl() + dataProcessRunPath + "/" + dataProcessId;
    }
}
