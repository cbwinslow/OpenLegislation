package gov.nysenate.openleg.auth.model;

import com.google.common.collect.ImmutableSet;
import gov.nysenate.openleg.auth.user.ApiUserSubscriptionType;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * This class will model an API User.
 */
public class ApiUser implements Serializable {
    private static final long serialVersionUID = 4297265625690273957L;

    /**
     * Each ApiUser will have their own unique key
     */
    private String apiKey;

    /**
     * The user's email address
     */
    private String email;

    /**
     * The user's name
     */
    private String name;

    /**
     * If the user belongs to a certain organization then that will be stored here
     */
    private String organizationName;

    /**
     * Whether or not the user has confirmed their email address.
     */
    private boolean authenticated;

    /**
     * Number of Api Requests made by the user
     */
    private long numApiRequests;

    /**
     * The user's unique registration token
     */
    private String registrationToken;

    /**
     * Whether or not this user is active
     */
    private boolean active;

    /**
     * A list of additional roles granted to this api user
     */
    private final Set<OpenLegRole> grantedRoles = new HashSet<>();

    /**
     * A list of subscription types this api user is enrolled in
     */
    private Set<ApiUserSubscriptionType> subscriptions = new HashSet<>();

    /**
     * --- Constructors ---
     */

    public ApiUser(String email) {
        this.apiKey = "";
        this.numApiRequests = 0l;
        this.authenticated = false;
        this.email = email;
        generateKey();
    }

    /**
     * Code to handle when an apiuser makes an api request.
     */
    public void logRequest() {
        this.numApiRequests++;
    }

    /**
     * This will be called once the ApiUser has authenticated their account.
     * Their key will be emailed to them.
     */
    public void authenticate() {
        this.authenticated = true;
    }

    /**
     * This method will generate a new 32 character long key for the user.
     */
    public void generateKey() {
        this.apiKey = RandomStringUtils.randomAlphanumeric(32);
    }

    /**
     * Adds a role to the api user's granted roles
     */
    public void addRole(OpenLegRole role) {
        this.grantedRoles.add(role);
    }

    /**
     * removes a role to the api user's granted roles
     */
    public void removeRole(OpenLegRole role) {
        this.grantedRoles.remove(role);
    }

    public ImmutableSet<OpenLegRole> getGrantedRoles() {
        return ImmutableSet.copyOf(grantedRoles);
    }

    /**
     * Adds a subscription type to the api user's subscription types
     */
    public void addSubscription(ApiUserSubscriptionType subscription) {
        this.subscriptions.add(subscription);
    }

    public void setSubscriptions(Set<ApiUserSubscriptionType> subscriptions) {
        this.subscriptions = new HashSet<>();
        this.subscriptions.addAll(subscriptions);
    }

    /**
     * Removes a subscription type from the api user's subscription types
     */
    public void removeSubscription(ApiUserSubscriptionType subscription) {
        this.subscriptions.remove(subscription);
    }

    /**
     * Returns an immutable set containing this api users subscriptions
     */
    public ImmutableSet<ApiUserSubscriptionType> getSubscriptions() {
        return ImmutableSet.copyOf(subscriptions);
    }

    /**
     * --- Basic Getters/Setters ---
     */

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public long getNumApiRequests() {
        return numApiRequests;
    }

    public void setNumApiRequests(long numApiRequests) {
        this.numApiRequests = numApiRequests;
    }

    public String getRegistrationToken() {
        return registrationToken;
    }

    public void setRegistrationToken(String registrationToken) {
        this.registrationToken = registrationToken;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiUser apiUser = (ApiUser) o;
        return apiKey.equals(apiUser.apiKey) &&
               email.equals(apiUser.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiKey, email);
    }
}
