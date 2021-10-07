package gov.nysenate.openleg.spotchecks.model;

import com.google.common.base.MoreObjects;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class DeNormSpotCheckMismatch<ContentKey> {

    /** An integer id that uniquely identifies this mismatch */
    private int mismatchId;

    /** Identifies the report this mismatch is associated with. */
    private int reportId;

    /** A key that identifies the content being checked. */
    private ContentKey key;

    /** The type of mismatch that occurred. */
    private SpotCheckMismatchType type;

    private SpotCheckDataSource dataSource;

    /** The status of the mismatch (new, existing, etc.) */
    private MismatchState state;

    private SpotCheckContentType contentType;

    /** The source used to compare our data against. */
    private SpotCheckReferenceId referenceId;

    /** String representation of the reference data. (e.g. lbdc daybreak content) */
    private String referenceData;

    /** String representation of the observed data (typically openleg processed content) */
    private String observedData;

    /** Any details about this mismatch. (Optional) */
    private String notes;

    /** The datetime this observation was made. */
    private LocalDateTime observedDateTime;

    /** The first time this mismatch has been seen. This value resets once a mismatch is closed. */
    private LocalDateTime firstSeenDateTime;

    /** The date time when the report that generated this observation was run */
    private LocalDateTime reportDateTime;

    /** The ignore status of this mismatch. */
    private SpotCheckMismatchIgnore ignoreStatus;

    /** A list of related issue tracker ids */
    private Set<String> issueIds = new HashSet<>();

    public DeNormSpotCheckMismatch(ContentKey key, SpotCheckMismatchType mismatchType, SpotCheckDataSource dataSource) {
       this.key = key;
       this.type = mismatchType;
       this.dataSource = dataSource;
       this.state = MismatchState.OPEN;
       this.ignoreStatus = SpotCheckMismatchIgnore.NOT_IGNORED;
    }

    public DeNormSpotCheckMismatch<ContentKey> copy() {
        DeNormSpotCheckMismatch<ContentKey> copy = new DeNormSpotCheckMismatch<>(key, type, dataSource);
        copy.setReportId(reportId);
        copy.setState(state);
        copy.setContentType(contentType);
        copy.setReferenceId(referenceId);
        copy.setReferenceData(referenceData);
        copy.setObservedData(observedData);
        copy.setNotes(notes);
        copy.setObservedDateTime(observedDateTime);
        copy.setFirstSeenDateTime(firstSeenDateTime);
        copy.setReportDateTime(reportDateTime);
        copy.setIgnoreStatus(ignoreStatus);
        copy.setIssueIds(issueIds);
        return copy;
    }

    /* --- Overrides --- */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeNormSpotCheckMismatch<?> that = (DeNormSpotCheckMismatch<?>) o;
        if (!Objects.equals(key, that.key)) return false;
        if (type != that.type) return false;
        if (dataSource != that.dataSource) return false;
        return contentType == that.contentType;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (dataSource != null ? dataSource.hashCode() : 0);
        result = 31 * result + (contentType != null ? contentType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("dataSource", dataSource)
                .add("key", key)
                .add("type", type)
                .add("state", state)
                .toString();
    }

   /* --- Functional Getters/Setters --- */

    public void setReferenceDateTime(LocalDateTime referenceDateTime) {
        this.setReferenceId(new SpotCheckReferenceId(this.getReferenceId().getReferenceType(), referenceDateTime));
    }

    /**
     * Set issue ids to the given values, ignores empty string values.
     */
    public void setIssueIds(Set<String> issueIds) {
        this.issueIds = issueIds.stream().filter(i -> i.length() > 0).collect(Collectors.toSet());
    }

    public void copyIgnoreStatus(DeNormSpotCheckMismatch other) {
        setIgnoreStatus(other.getIgnoreStatus());
    }

    public SpotCheckMismatchKey<ContentKey> getMismatchKey() {
        return new SpotCheckMismatchKey<>(key, type);
    }

    /* --- Getters / Setters --- */

    public void setMismatchId(int mismatchId) {
        this.mismatchId = mismatchId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public void setState(MismatchState state) {
        this.state = state;
    }

    public void setReferenceId(SpotCheckReferenceId referenceId) {
        this.referenceId = referenceId;
    }

    public void setReferenceData(String referenceData) {
        this.referenceData = referenceData;
    }

    public void setObservedData(String observedData) {
        this.observedData = observedData;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setObservedDateTime(LocalDateTime observedDateTime) {
        this.observedDateTime = observedDateTime;
    }

    public void setFirstSeenDateTime(LocalDateTime firstSeenDateTime) {
        this.firstSeenDateTime = firstSeenDateTime;
    }

    public void setReportDateTime(LocalDateTime reportDateTime) {
        this.reportDateTime = reportDateTime;
    }

    public void setIgnoreStatus(SpotCheckMismatchIgnore ignoreStatus) {
        this.ignoreStatus = ignoreStatus;
    }

    public void setContentType(SpotCheckContentType contentType) {
        this.contentType = contentType;
    }

    public int getMismatchId() {
        return mismatchId;
    }

    public int getReportId() {
        return reportId;
    }

    public ContentKey getKey() {
        return key;
    }

    public SpotCheckMismatchType getType() {
        return type;
    }

    public MismatchState getState() {
        return state;
    }

    public SpotCheckDataSource getDataSource() {
        return dataSource;
    }

    public SpotCheckContentType getContentType() {
        return contentType;
    }

    public SpotCheckReferenceId getReferenceId() {
        return referenceId;
    }

    public String getReferenceData() {
        return referenceData;
    }

    public String getObservedData() {
        return observedData;
    }

    public LocalDateTime getFirstSeenDateTime() {
        return firstSeenDateTime;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getObservedDateTime() {
        return observedDateTime;
    }

    public LocalDateTime getReportDateTime() {
        return reportDateTime;
    }

    public SpotCheckMismatchIgnore getIgnoreStatus() {
        return ignoreStatus;
    }

    public Set<String> getIssueIds() {
        return issueIds;
    }
}
