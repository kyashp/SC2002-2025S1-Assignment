package entity.domain.enums;

/**
 * Represents the approval and availability status of an internship opportunity.
 */
public enum OpportunityStatus {
    PENDING,   // Waiting for CareerCenterStaff approval
    APPROVED,  // Approved and open (if visible)
    REJECTED,  // Rejected by staff
    FILLED     // All slots taken
}