package entity.domain;

import java.time.LocalDateTime;
import java.util.Objects;

import entity.domain.enums.RequestStatus;

/**
 * Represents a student's request to withdraw from an internship application.
 */
public class WithdrawalRequest {

    // ===== Attributes =====
    private String id;
    private Application application;
    private Student requestedBy;
    private RequestStatus status;
    private LocalDateTime requestedAt;
    private String reason;

    // ===== Constructors =====
    public WithdrawalRequest() {
        // default constructor
    }

    public WithdrawalRequest(String id, Application application, Student requestedBy, String reason) {
        this.id = id;
        this.application = application;
        this.requestedBy = requestedBy;
        this.reason = reason;
        this.status = RequestStatus.PENDING;
        this.requestedAt = LocalDateTime.now();
    }

    // ===== Getters & Setters =====
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Student getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(Student requestedBy) {
        this.requestedBy = requestedBy;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    // ===== Behavior =====

    /** Approves this withdrawal request. */
    public void approve() {
        this.status = RequestStatus.APPROVED;
        if (application != null) {
            application.setStatus(entity.domain.enums.ApplicationStatus.UNSUCCESSFUL);
        }
        System.out.println("Withdrawal request " + id + " approved.");
    }

    /** Rejects this withdrawal request. */
    public void reject() {
        this.status = RequestStatus.REJECTED;
        System.out.println("Withdrawal request " + id + " rejected.");
    }

    @Override
    public String toString() {
        return String.format("WithdrawalRequest[%s, Student: %s, Application: %s, Status: %s, Reason: %s]",
                id,
                requestedBy != null ? requestedBy.getUserName() : "Unknown",
                application != null ? application.getOpportunity().getTitle() : "Unknown",
                status,
                reason);
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
