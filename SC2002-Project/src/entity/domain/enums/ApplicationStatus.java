package entity.domain.enums;

/**
 * Represents the review status of a student's application.
 */
public enum ApplicationStatus {
    PENDING,      // Application submitted, awaiting review
    SUCCESSFUL,   // Approved by company representative
    UNSUCCESSFUL, // Rejected
    WITHDRAWN  // Withdrawn
}
