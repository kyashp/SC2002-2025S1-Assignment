package entity.domain;

import entity.domain.enums.InternshipLevel;
import entity.domain.enums.OpportunityStatus;


/**
 * Represents filter criteria for generating internship opportunity reports.
 */

public class ReportFilter implements ReportFilterCriteria{
	
	// Attributes
	
	private OpportunityStatus status;
	private String preferredMajor;
	private InternshipLevel level;
	private String company;
	private DateRange dateRange;
	
	// Constructors
	
	public ReportFilter() {}
	
	public ReportFilter(OpportunityStatus status, String preferredMajor, InternshipLevel level,
            String company, DateRange dateRange) {
		
		this.status = status;
		this.preferredMajor = preferredMajor;
		this.level = level;
		this.company = company;
		this.dateRange = dateRange;
	}
	
	
	// Getters and Setters
	
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

    public InternshipLevel getLevel() {
        return level;
    }

    public void setLevel(InternshipLevel level) {
        this.level = level;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public DateRange getDateRange() {
        return dateRange;
    }

    public void setDateRange(DateRange dateRange) {
        this.dateRange = dateRange;
    }

    @Override
    public String toString() {
        return String.format("ReportFilter[Status=%s, Major=%s, Level=%s, Company=%s, DateRange=%s]",
                status, preferredMajor, level, company, dateRange);
    }


}
