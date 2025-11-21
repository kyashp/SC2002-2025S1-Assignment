package entity.domain;

import entity.domain.enums.InternshipLevel;
import entity.domain.enums.OpportunityStatus;

/*
 * Represents one row in the generated report — summarizing a single internship opportunity.
 */

public class ReportRow {
	private String opportunityId;
	private String title;
    private String companyName;
	private InternshipLevel level;
	private OpportunityStatus status;
	private String preferredMajor;
	private int totalApplications;
	private int filledSlots;
	private int remainingSlots;
    private int totalSlots;
	
	 // ===== Constructors =====
    public ReportRow() {}

    /**
     * Constructs a fully populated report row.
     *
     * @param opportunityId unique opportunity identifier
     * @param title opportunity title
     * @param level internship level
     * @param status opportunity status
     * @param preferredMajor preferred major text
     * @param totalApplications total applications received
     * @param filledSlots count of successful applications
     * @param remainingSlots remaining slot count
     */
    public ReportRow(String opportunityId, String title, InternshipLevel level,
                     OpportunityStatus status, String preferredMajor,
                     int totalApplications, int filledSlots, int remainingSlots, int totalSlots, String companyName) {
        this.opportunityId = opportunityId;
        this.title = title;
        this.companyName = companyName;
        this.level = level;
        this.status = status;
        this.preferredMajor = preferredMajor;
        this.totalApplications = totalApplications;
        this.filledSlots = filledSlots;
        this.remainingSlots = remainingSlots;
        this.totalSlots = totalSlots;
    }

    // ===== Getters & Setters =====
    /** @return opportunity ID. */
    public String getOpportunityId() {
        return opportunityId;
    }

    /** @param opportunityId opportunity ID. */
    public void setOpportunityId(String opportunityId) {
        this.opportunityId = opportunityId;
    }

    /** @return opportunity title. */
    public String getTitle() {
        return title;
    }

    /** @param title opportunity title. */
    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    /** @return internship level. */
    public InternshipLevel getLevel() {
        return level;
    }

    /** @param level internship level. */
    public void setLevel(InternshipLevel level) {
        this.level = level;
    }

    /** @return opportunity status. */
    public OpportunityStatus getStatus() {
        return status;
    }

    /** @param status opportunity status. */
    public void setStatus(OpportunityStatus status) {
        this.status = status;
    }

    /** @return preferred major string. */
    public String getPreferredMajor() {
        return preferredMajor;
    }

    /** @param preferredMajor preferred major string. */
    public void setPreferredMajor(String preferredMajor) {
        this.preferredMajor = preferredMajor;
    }

    /** @return total application count. */
    public int getTotalApplications() {
        return totalApplications;
    }

    /** @param totalApplications total application count. */
    public void setTotalApplications(int totalApplications) {
        this.totalApplications = totalApplications;
    }

    /** @return count of filled slots (successful offers). */
    public int getFilledSlots() {
        return filledSlots;
    }

    /** @param filledSlots count of filled slots (successful offers). */
    public void setFilledSlots(int filledSlots) {
        this.filledSlots = filledSlots;
    }

    /** @return remaining slot count. */
    public int getRemainingSlots() {
        return remainingSlots;
    }

    /** @param remainingSlots remaining slot count. */
    public void setRemainingSlots(int remainingSlots) {
        this.remainingSlots = remainingSlots;
    }

    public int getTotalSlots() { return totalSlots; }
    public void setTotalSlots(int totalSlots) { this.totalSlots = totalSlots; }

    @Override
    public String toString() {
        return String.format(
                "ReportRow[ID=%s, Title=%s, Level=%s, Status=%s, Major=%s, TotalApps=%d, Filled=%d, Remaining=%d]",
                opportunityId, title, level, status, preferredMajor,
                totalApplications, filledSlots, remainingSlots);
    }

	

}
