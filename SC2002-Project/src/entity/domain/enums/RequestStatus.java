package entity.domain.enums;

/**
 * Represents the status of administrative requests such as
 * company registration or student withdrawal.
 */
public enum RequestStatus {
    PENDING,   // Waiting for staff review
    APPROVED,  // Approved by staff
    REJECTED   // Rejected by staff
}

