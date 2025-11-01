package entity.domain;

import entity.domain.enums.InternshipLevel;
import entity.domain.enums.OpportunityStatus;

/*
 * Represents one row in the generated report — summarizing a single internship opportunity.
 */

public class ReportRow {
	private String opportunityId;
	private String title;
	private InternshipLevel level;
	private OpportunityStatus status;
	private String preferredMajor;
	private int totalApplications;
	private int filledSlots;
	private int remainingSlots;
	
	 // ===== Constructors =====
    public ReportRow() {}

    public ReportRow(String opportunityId, String title, InternshipLevel level,
                     OpportunityStatus status, String preferredMajor,
                     int totalApplications, int filledSlots, int remainingSlots) {
        this.opportunityId = opportunityId;
        this.title = title;
        this.level = level;
        this.status = status;
        this.preferredMajor = preferredMajor;
        this.totalApplications = totalApplications;
        this.filledSlots = filledSlots;
        this.remainingSlots = remainingSlots;
    }

    // ===== Getters & Setters =====
    public String getOpportunityId() {
        return opportunityId;
    }

    public void setOpportunityId(String opportunityId) {
        this.opportunityId = opportunityId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public InternshipLevel getLevel() {
        return level;
    }

    public void setLevel(InternshipLevel level) {
        this.level = level;
    }

    public OpportunityStatus getStatus() {
        return status;
    }

    public void setStatus(OpportunityStatus status) {
        this.status = status;
    }

    public String getPreferredMajor() {
        return preferredMajor;
    }

    public void setPreferredMajor(String preferredMajor) {
        this.preferredMajor = preferredMajor;
    }

    public int getTotalApplications() {
        return totalApplications;
    }

    public void setTotalApplications(int totalApplications) {
        this.totalApplications = totalApplications;
    }

    public int getFilledSlots() {
        return filledSlots;
    }

    public void setFilledSlots(int filledSlots) {
        this.filledSlots = filledSlots;
    }

    public int getRemainingSlots() {
        return remainingSlots;
    }

    public void setRemainingSlots(int remainingSlots) {
        this.remainingSlots = remainingSlots;
    }

    @Override
    public String toString() {
        return String.format(
                "ReportRow[ID=%s, Title=%s, Level=%s, Status=%s, Major=%s, TotalApps=%d, Filled=%d, Remaining=%d]",
                opportunityId, title, level, status, preferredMajor,
                totalApplications, filledSlots, remainingSlots);
    }

	

}
