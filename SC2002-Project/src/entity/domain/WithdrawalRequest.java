package entity.domain;

import java.time.LocalDateTime;
import java.util.Objects;

import entity.domain.enums.RequestStatus;
import util.IdGenerator;

/**
 * Represents a Student's request to withdraw from an internship application.
 */
public class WithdrawalRequest {
    private static final IdGenerator idGen = new IdGenerator();
    private String id;
    private Application application;
    private Student requestedBy;
    private RequestStatus status;
    private LocalDateTime requestedAt;
    private String reason;
    private LocalDateTime lastUpdated;
    /**
     * Default constructor
     */
    public WithdrawalRequest(){
        this.id = idGen.newId("W");
        this.status = RequestStatus.PENDING;
        this.requestedAt = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Constructs a WithdrawalRequest object by a Student for Career Center Staff to approve
     * @param id String RequestId
     * @param application Application for withdrawal
     * @param requestedBy Student that has requested for withdrawal
     * @param reason String reason for withdrawal
     */
    public WithdrawalRequest(String id, Application application, Student requestedBy, String reason) {
        this.id = id;
        this.application = application;
        this.requestedBy = requestedBy;
        this.reason = reason;
        this.status = RequestStatus.PENDING;
        this.requestedAt = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Retrieves request id
     * @return Request id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets request id
     * @param id String id
     */
    public void setId(String id) {
        this.id = id;
    }

    /** For loading from storage: sets id and seeds generator. */
    public void setIdForImport(String id, int numericMax) {
        if (id != null && !id.isBlank()) {
            this.id = id;
        }
        idGen.seedPrefix("W", numericMax);
    }

    /**
     * Retrieves application WithdrawalRequest was submitted for
     * @return Application for WithdrawalRequest
     */
    public Application getApplication() {
        return application;
    }

    /**
     * Sets application for WithdrawalRequest
     * @param application Application to be withdrawn
     */
    public void setApplication(Application application) {
        this.application = application;
    }

    /**
     * Retrieves Student who requested for WithdrawalRequest
     * @return Student
     */
    public Student getRequestedBy() {
        return requestedBy;
    }

    /**
     * Sets Student who requested for WithdrawalRequest
     * @param requestedBy Student
     */
    public void setRequestedBy(Student requestedBy) {
        this.requestedBy = requestedBy;
    }

    /**
     * Retrieves the status of WithdrawalRequest
     * @return RequestStatus enum
     */
    public RequestStatus getStatus() {
        return status;
    }

    /**
     * Sets the status of WithdrawalRequest
     * @param status RequestStatus enum
     */
    public void setStatus(RequestStatus status) {
        this.status = status;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Retrieves the lastUpdated time
     * @return lastUpdated LocalDateTime
     */
    public LocalDateTime getLastUpdated(){
        return this.lastUpdated;
    }
    /** For import: set last updated explicitly. */
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    /**
     * Retrieves the date and time of WithdrawalRequest
     * @return  LocalDateTime
     */
    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    /**
     * Sets the date and time of WithdrawalRequest
     * @param requestedAt LocalDateTime
     */
    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    /**
     * Retrieves reason for WithdrawalRequest
     * @return String reason
     */
    public String getReason() {
        return reason;
    }

    /**
     * Sets reason for WithdrawalRequest
     * @param reason String reason
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * Approves this withdrawal request and cascades WITHDRAWN status to the application.
     */
    public void approve() {
        this.status = RequestStatus.APPROVED;
        if (application != null) {
            application.setStatus(entity.domain.enums.ApplicationStatus.WITHDRAWN);
        }
        System.out.println("Withdrawal request " + id + " approved.");
    }

    /**
     * Rejects this withdrawal request, leaving the application unchanged.
     */
    public void reject() {
        this.status = RequestStatus.REJECTED;
        System.out.println("Withdrawal request " + id + " rejected.");
    }

    @Override
    public String toString() {
        return String.format("WithdrawalRequest[%s, Student: %s, Application: %s, Status: %s, Requested At: %s]",
                id,
                requestedBy != null ? requestedBy.getUserName() : "Unknown",
                application != null ? application.getOpportunity().getTitle() : "Unknown",
                status,
                this.getRequestedAt().toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WithdrawalRequest)) return false;
        WithdrawalRequest other = (WithdrawalRequest) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
