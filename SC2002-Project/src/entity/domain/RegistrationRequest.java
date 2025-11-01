package entity.domain;

import java.time.LocalDateTime;
import java.util.Objects;


import entity.domain.enums.RequestStatus;

public class RegistrationRequest {
	 // ===== Attributes =====
	
	private String id;
    private CompanyRepresentative rep;
    private RequestStatus status;
    private LocalDateTime requestedAt;
    
    // ===== Constructors =====
    public RegistrationRequest() {
        // Default constructor for flexibility
    }
    
    public RegistrationRequest(String id, CompanyRepresentative rep) {
        this.id = id;
        this.rep = rep;
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

    public CompanyRepresentative getRep() {
        return rep;
    }

    public void setRep(CompanyRepresentative rep) {
        this.rep = rep;
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

    // ===== Convenience Methods =====

    /**
     * Marks this request as approved.
     */
    public void approve() {
        this.status = RequestStatus.APPROVED;
        if (rep != null) rep.setApproved(true);
    }

    /**
     * Marks this request as rejected.
     */
    public void reject() {
        this.status = RequestStatus.REJECTED;
        if (rep != null) rep.setApproved(false);
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
