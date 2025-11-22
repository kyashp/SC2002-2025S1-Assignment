package entity.domain.enums;

/**
 * Represents the review status of a student's application.
 */
public enum ApplicationStatus {
    /** Application submitted, awaiting review. */
    PENDING,
    /** Application approved by company representative. */
    SUCCESSFUL,
    /** Student accepted the successful offer. */
    ACCEPTED,
    /** Application rejected during company review. */
    UNSUCCESSFUL,
    /** Application withdrawn by the student. */
    WITHDRAWN
}
