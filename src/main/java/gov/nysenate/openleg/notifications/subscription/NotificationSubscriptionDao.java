package gov.nysenate.openleg.notifications.subscription;

import gov.nysenate.openleg.notifications.model.NotificationSubscription;
import gov.nysenate.openleg.notifications.model.SubscriptionNotFoundEx;

import java.util.Set;

public interface NotificationSubscriptionDao {

    /**
     * Retrieves a notification subscription with the given id
     * @param subscriptionId int
     * @return {@link NotificationSubscription}
     */
    NotificationSubscription getSubscription(int subscriptionId) throws SubscriptionNotFoundEx;

    /**
     * Retrieve all notification subscriptions
     * @return List<NotificationSubscription>
     */
    Set<NotificationSubscription> getSubscriptions();

    /**
     * Insert or modify a subscription
     *
     * Assigns an id to the notification if it is new.
     * The returned {@link NotificationSubscription} will contain the assigned id.
     * @param subscription NotificationSubscription
     * @return {@link NotificationSubscription} - the updated subscription
     */
    NotificationSubscription updateSubscription(NotificationSubscription subscription);

    /**
     * Remove a subscription
     * @param subscriptionId int
     */
    void removeSubscription(int subscriptionId);
}
