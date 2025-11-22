package entity.domain.enums;

/**
 * Represents the status of administrative requests such as
 * company registration or student withdrawal.
 */
public enum RequestStatus {
    /** Waiting for staff review. */
    PENDING,
    /** Approved by staff. */
    APPROVED,
    /** Rejected by staff. */
    REJECTED
}
