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

    /**
     * @param status opportunity status filter
     * @param preferredMajor preferred major to match (nullable)
     * @param level internship level filter
     * @param company company name filter
     * @param openDateFrom restrict opportunities opening on/after this date
     * @param closeDateBy restrict close date to on/before this date
     */
    public ReportFilter(OpportunityStatus status, String preferredMajor, InternshipLevel level,
                        String company, LocalDate openDateFrom, LocalDate closeDateBy) {
        this.status = status;
        this.preferredMajor = preferredMajor;
        this.level = level;
        this.company = company;
        this.openDateFrom = openDateFrom;
        this.closeDateBy = closeDateBy;
    }

    /** @return opportunity status filter. */
    public OpportunityStatus getStatus() {
        return status;
    }

    /** @param status opportunity status filter. */
    public void setStatus(OpportunityStatus status) {
        this.status = status;
    }

    /** @return preferred major filter. */
    public String getPreferredMajor() {
        return preferredMajor;
    }

    /** @param preferredMajor preferred major filter. */
    public void setPreferredMajor(String preferredMajor) {
        this.preferredMajor = preferredMajor;
    }

    /** @return internship level filter. */
    public InternshipLevel getLevel() {
        return level;
    }

    /** @param level internship level filter. */
    public void setLevel(InternshipLevel level) {
        this.level = level;
    }

    /** @return company name filter. */
    public String getCompany() {
        return company;
    }

    /** @param company company name filter. */
    public void setCompany(String company) {
        this.company = company;
    }

    /** @return earliest acceptable opening date. */
    public LocalDate getOpenDateFrom() {
        return openDateFrom;
    }

    /** @param openDateFrom earliest acceptable opening date. */
    public void setOpenDateFrom(LocalDate openDateFrom) {
        this.openDateFrom = openDateFrom;
    }

    /** @return latest acceptable closing date. */
    public LocalDate getCloseDateBy() {
        return closeDateBy;
    }

    /** @param closeDateBy latest acceptable closing date. */
    public void setCloseDateBy(LocalDate closeDateBy) {
        this.closeDateBy = closeDateBy;
    }

    @Override
    public String toString() {
        return String.format("ReportFilter[Status=%s, Major=%s, Level=%s, Company=%s, OpenFrom=%s, CloseBy=%s]",
                status, preferredMajor, level, company, openDateFrom, closeDateBy);
    }
}
