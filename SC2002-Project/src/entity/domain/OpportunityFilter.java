package entity.domain;

import java.time.LocalDate;
import entity.domain.enums.InternshipLevel;
import entity.domain.enums.OpportunityStatus;

/**
 * Per-user filter for browsing internship opportunities.
 * Any null/empty field is treated as "no filter".
 */
public class OpportunityFilter implements OpportunityFilterCriteria{
    private OpportunityStatus status;     // PENDING/APPROVED/REJECTED/FILLED
    private String preferredMajor;        // exact match or null for any
    private InternshipLevel level;        // BASIC/INTERMEDIATE/ADVANCED
    private LocalDate closingBefore;      // show opps with closeDate <= this
    private SortKey sortKey = SortKey.TITLE_ASC;

    public enum SortKey {
        TITLE_ASC,            // default
        CLOSING_DATE_ASC,
        COMPANY_ASC,
        LEVEL_ASC
    }

    // Getters & setters
    public OpportunityStatus getStatus() { return status; }
    public void setStatus(OpportunityStatus status) { this.status = status; }
    public String getPreferredMajor() { return preferredMajor; }
    public void setPreferredMajor(String preferredMajor) { this.preferredMajor = (preferredMajor == null || preferredMajor.isBlank() ? null : preferredMajor); }
    public InternshipLevel getLevel() { return level; }
    public void setLevel(InternshipLevel level) { this.level = level; }
    public LocalDate getClosingBefore() { return closingBefore; }
    public void setClosingBefore(LocalDate closingBefore) { this.closingBefore = closingBefore; }
    public SortKey getSortKey() { return sortKey; }
    public void setSortKey(SortKey sortKey) { this.sortKey = (sortKey == null ? SortKey.TITLE_ASC : sortKey); }
}
