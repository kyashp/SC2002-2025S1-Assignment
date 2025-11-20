package entity.domain;

import java.time.LocalDate;

import entity.domain.enums.InternshipLevel;
import entity.domain.enums.OpportunityStatus;

/**
 * Represents filter criteria for generating internship opportunity reports.
 */
public class ReportFilter implements ReportFilterCriteria {

    private OpportunityStatus status;
    private String preferredMajor;
    private InternshipLevel level;
    private String company;
    private LocalDate openDateFrom;
    private LocalDate closeDateBy;

    public ReportFilter() {}

    public ReportFilter(OpportunityStatus status, String preferredMajor, InternshipLevel level,
                        String company, LocalDate openDateFrom, LocalDate closeDateBy) {
        this.status = status;
        this.preferredMajor = preferredMajor;
        this.level = level;
        this.company = company;
        this.openDateFrom = openDateFrom;
        this.closeDateBy = closeDateBy;
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

    public LocalDate getOpenDateFrom() {
        return openDateFrom;
    }

    public void setOpenDateFrom(LocalDate openDateFrom) {
        this.openDateFrom = openDateFrom;
    }

    public LocalDate getCloseDateBy() {
        return closeDateBy;
    }

    public void setCloseDateBy(LocalDate closeDateBy) {
        this.closeDateBy = closeDateBy;
    }

    @Override
    public String toString() {
        return String.format("ReportFilter[Status=%s, Major=%s, Level=%s, Company=%s, OpenFrom=%s, CloseBy=%s]",
                status, preferredMajor, level, company, openDateFrom, closeDateBy);
    }
}
