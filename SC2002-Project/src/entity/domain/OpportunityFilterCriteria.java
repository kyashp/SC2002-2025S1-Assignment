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

    /** @return status constraint or null if any status is allowed. */
    OpportunityStatus getStatus();

    /** @return preferred major constraint or null. */
    String getPreferredMajor();

    /** @return internship level constraint or null. */
    InternshipLevel getLevel();

    /** @return closing-date upper bound or null. */
    LocalDate getClosingBefore();

    /** @return desired sorting key. */
    SortKey getSortKey();
}
