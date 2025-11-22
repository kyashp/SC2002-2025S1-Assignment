package entity.domain.enums;

/**
 * Represents the approval and availability status of an internship opportunity.
 */
public enum OpportunityStatus {
    /** Waiting for CareerCenterStaff approval. */
    PENDING,
    /** Approved and open (if visible). */
    APPROVED,
    /** Rejected by staff. */
    REJECTED,
    /** All slots taken, opportunity no longer accepting applications. */
    FILLED
}
