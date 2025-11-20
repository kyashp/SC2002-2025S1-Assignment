package entity.domain;

import java.time.LocalDate;

import entity.domain.enums.InternshipLevel;
import entity.domain.enums.OpportunityStatus;

/**
 * Read-only view of report filter criteria.
 * Used by report generation so it only depends on the needed fields.
 */
public interface ReportFilterCriteria {

    OpportunityStatus getStatus();

    String getPreferredMajor();

    InternshipLevel getLevel();

    String getCompany();

    LocalDate getOpenDateFrom();

    LocalDate getCloseDateBy();
}
