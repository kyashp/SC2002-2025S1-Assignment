package util;

import java.util.regex.Pattern;

/**
 * <<Utility>> Validator
 * Provides common validation methods for IDs and emails.
 */
public class Validator {

    // ===== Regex Patterns =====
    // NTU Student IDs usually look like "S1234567" or "U1234567A"
    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^[SU]\\d{7}[A-Z]?$");
    private static final Pattern NTU_ID_PATTERN = Pattern.compile("^\\w+@ntu\\.edu\\.sg$");
    private static final Pattern COMPANY_EMAIL_PATTERN = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[A-Za-z]{2,}$");

    // ===== Methods =====

    /**
     * Checks if the given ID matches a valid student ID format.
     * Examples: S1234567A, U1234567B, S7654321
     */
    public boolean isValidStudentId(String id) {
        if (id == null) return false;
        return STUDENT_ID_PATTERN.matcher(id.trim()).matches();
    }

    /**
     * Checks if the given ID matches a valid NTU email account.
     * Example: sng001@ntu.edu.sg
     */
    public boolean isValidNtuId(String id) {
        if (id == null) return false;
        return NTU_ID_PATTERN.matcher(id.trim().toLowerCase()).matches();
    }

    /**
     * Checks if the given email looks like a valid company email.
     * Examples: name@company.com, john.doe@abc.sg
     */
    public boolean isValidCompanyEmail(String email) {
        if (email == null) return false;
        return COMPANY_EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates that a string is not null or blank.
     */
    public boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}