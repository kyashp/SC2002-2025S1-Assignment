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

    /**
     * Supported sort orders for listing opportunities.
     */
    public enum SortKey {
        TITLE_ASC,            // default
        CLOSING_DATE_ASC,
        COMPANY_ASC,
        LEVEL_ASC
    }

    // Getters & setters
    /** @return desired opportunity status filter (nullable). */
    public OpportunityStatus getStatus() { return status; }
    /** @param status opportunity status constraint. */
    public void setStatus(OpportunityStatus status) { this.status = status; }
    /** @return preferred major filter (nullable). */
    public String getPreferredMajor() { return preferredMajor; }
    /** @param preferredMajor major filter (blank/null treated as no filter). */
    public void setPreferredMajor(String preferredMajor) { this.preferredMajor = (preferredMajor == null || preferredMajor.isBlank() ? null : preferredMajor); }
    /** @return internship level filter (nullable). */
    public InternshipLevel getLevel() { return level; }
    /** @param level internship level constraint. */
    public void setLevel(InternshipLevel level) { this.level = level; }
    /** @return latest acceptable closing date (nullable). */
    public LocalDate getClosingBefore() { return closingBefore; }
    /** @param closingBefore filter to include opportunities closing on/before this date. */
    public void setClosingBefore(LocalDate closingBefore) { this.closingBefore = closingBefore; }
    /** @return current sort preference. */
    public SortKey getSortKey() { return sortKey; }
    /** @param sortKey desired sort order (defaults to TITLE_ASC if null). */
    public void setSortKey(SortKey sortKey) { this.sortKey = (sortKey == null ? SortKey.TITLE_ASC : sortKey); }
}
