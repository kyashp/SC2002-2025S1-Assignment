package entity.domain;

import java.time.LocalDate;

import entity.domain.enums.InternshipLevel;
import entity.domain.enums.OpportunityStatus;
import entity.domain.OpportunityFilter.SortKey;

/**
 * Read-only view of opportunity filter criteria.
 * Used by services so they only depend on what they actually need.
 */
public interface OpportunityFilterCriteria {

    OpportunityStatus getStatus();

    String getPreferredMajor();

    InternshipLevel getLevel();

    LocalDate getClosingBefore();

    SortKey getSortKey();
}
