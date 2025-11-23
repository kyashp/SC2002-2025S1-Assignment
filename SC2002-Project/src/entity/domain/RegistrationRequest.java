package entity.domain;

import java.time.LocalDateTime;
import java.util.Objects;


import entity.domain.enums.RequestStatus;
import util.IdGenerator;

/**
 * Represents a company representative's request to be approved by staff.
 * Tracks the representative, current status, and submission timestamp.
 */
public class RegistrationRequest {
	 // ===== Attributes =====
	private static final IdGenerator idGen = new IdGenerator();
	private final String id =  idGen.newId("REG");
    private CompanyRepresentative rep;
    private RequestStatus status;
    private LocalDateTime requestedAt;
    
    // ===== Constructors =====
    public RegistrationRequest() {
        // Default constructor for flexibility
    }
    
    /**
     * Creates a request for the specified representative with default PENDING status.
     *
     * @param rep representative seeking approval
     */
    public RegistrationRequest(CompanyRepresentative rep) {

        this.rep = rep;
        this.status = RequestStatus.PENDING;
        this.requestedAt = LocalDateTime.now();
    }
    
 // ===== Getters & Setters =====
    public String getId() {
        return id;
    }

    /** @return associated representative. */
    public CompanyRepresentative getRep() {
        return rep;
    }

    /** @param rep representative to associate. */
    public void setRep(CompanyRepresentative rep) {
        this.rep = rep;
    }

    /** @return current status. */
    public RequestStatus getStatus() {
        return status;
    }

    /** @param status new status to assign. */
    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    /** @return timestamp when the request was submitted. */
    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    /** @param requestedAt submission timestamp to set. */
    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    // ===== Convenience Methods =====

    /**
     * Marks this request as approved.
     */
    public void approve() {
        this.status = RequestStatus.APPROVED;
        if (rep != null) rep.setApproved(RequestStatus.APPROVED);
    }

    /**
     * Marks this request as rejected.
     */
    public void reject() {
        this.status = RequestStatus.REJECTED;
        if (rep != null) rep.setApproved(RequestStatus.REJECTED);
    }

    @Override
    public String toString() {
        return String.format("RegistrationRequest[%s, Rep: %s, Status: %s, RequestedAt: %s]",
                id,
                rep != null ? rep.getUserName() : "Unknown",
                status,
                requestedAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegistrationRequest)) return false;
        RegistrationRequest other = (RegistrationRequest) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


}
