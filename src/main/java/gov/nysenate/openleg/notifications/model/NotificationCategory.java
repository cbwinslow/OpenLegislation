package gov.nysenate.openleg.notifications.model;

/**
 * Notification Categories represent different types of notifications.
 */
public enum NotificationCategory {

    INFO,   // A routine notification. i.e. Results from a scheduled spotcheck report.
    ERROR   // An unexpected notification, usually an exception of some type.
    ;
}
