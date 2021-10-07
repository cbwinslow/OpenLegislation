package gov.nysenate.openleg.notifications.model;

public class SubscriptionNotFoundEx extends RuntimeException {

    private final int subscriptionId;

    public SubscriptionNotFoundEx(int subscriptionId) {
        super("Could not locate notification subscription with id=" + subscriptionId);
        this.subscriptionId = subscriptionId;
    }

    public int getSubscriptionId() {
        return subscriptionId;
    }
}
