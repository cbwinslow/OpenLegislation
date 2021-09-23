package gov.nysenate.openleg.legislation;

import com.google.common.collect.ImmutableSet;

/**
 * Content caches store various types of data. The cache types enumerated here should
 * be able to manage themselves, have configurable sizes, and have functionality to warm
 * up upon request.
 */
public enum ContentCache
{
    BILL,
    BILL_INFO,
    AGENDA,
    CALENDAR,
    LAW,
    COMMITTEE,
    SESSION_MEMBER, //Session Member
    FULL_MEMBER, //Member
    SESSION_CHAMBER_SHORTNAME, //Session Member with a different key
    APIUSER,
    SHIRO,
    NOTIFICATION_SUBSCRIPTION;

    private static final ImmutableSet<ContentCache> allContentCaches = ImmutableSet.copyOf(ContentCache.values());

    public static ImmutableSet<ContentCache> getAllContentCaches() {
        return allContentCaches;
    }
}