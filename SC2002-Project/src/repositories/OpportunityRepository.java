package repositories;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import entity.domain.InternshipOpportunity;
import entity.domain.ReportFilter;
import entity.domain.enums.OpportunityStatus;

/** 
 * <<Repository>> Opportunity Repository
 * Stores and retrieves InternshipOpportuniity entities
 * Acts as an in-memory data access layer for company reps,staff, and reports
 */

public class OpportunityRepository {
	private final List<InternshipOpportunity> opportunities = new ArrayList<>();
	
	
	// Methods
	
	/* 
	 * Saves or updates an internship opportunity
	 * If the opportunity already exits (same ID), replaces it
	 */
	
	public void save(InternshipOpportunity opp) {
		Objects.requireNonNull(opp, "Opportunity cannot be null");
		
		InternshipOpportunity existing = findById(opp.getId());
		if (existing != null) {
			opportunities.remove(existing);
		}
		opportunities.add(opp);
	}
	
	/* 
	 * Finds an opportunity by its unique ID.
	 */
	
	public InternshipOpportunity findById(String id) {
		if (id == null) return null;
		for (InternshipOpportunity opp: opportunities) {
			if (opp.getId().equalsIgnoreCase(id)) {
				return opp;
			}
		}
		return null;
	}
	
	/*
	 * Finds all approved & visible opportunities that match the given report filter.
	 *  If filter is null, returns all approved & visible opportunities
	 */
	
	public List<InternshipOpportunity> findApprovedVisibleByFilter(ReportFilter filter){
		List<InternshipOpportunity> result = new ArrayList<>();
		for (InternshipOpportunity opp: opportunities) {
			
			if (opp.getStatus() == OpportunityStatus.APPROVED && opp.isVisibility()) {
				
				// If no filter specified, include all approved visible opportunities
				if (filter == null) {
					result.add(opp);
					continue;
				}

                boolean match = true;
                
                if (filter.getCompany() != null && !filter.getCompany().isBlank()) {
                    match &= opp.getCompanyName().equalsIgnoreCase(filter.getCompany());
                }
                if (filter.getPreferredMajor() != null && !filter.getPreferredMajor().isBlank()) {
                    match &= opp.getPreferredMajor().equalsIgnoreCase(filter.getPreferredMajor());
                }
                if (filter.getLevel() != null && opp.getLevel() != filter.getLevel()) {
                    match = false;
                }
                if (filter.getStatus() != null && opp.getStatus() != filter.getStatus()) {
                    match = false;
                }

                // (Optional) DateRange filter check if implemented
                if (filter.getDateRange() != null) {
                    if (opp.getOpenDate().isBefore(filter.getDateRange().getStart())
                            || opp.getCloseDate().isAfter(filter.getDateRange().getEnd())) {
                        match = false;
                    }
                }

                if (match) {
                    result.add(opp);
                }
            }
        }
        return result;
	}
	
	/**
     * Returns all opportunities belonging to a given company.
     */
    public List<InternshipOpportunity> findByCompany(String company) {
        List<InternshipOpportunity> result = new ArrayList<>();
        if (company == null || company.isBlank()) return result;

        for (InternshipOpportunity opp : opportunities) {
            if (opp.getCompanyName().equalsIgnoreCase(company)) {
                result.add(opp);
            }
        }
        return result;
    }
    
    /**
     * Returns all opportunities stored (for testing or reports).
     */
    public List<InternshipOpportunity> findAll() {
        return new ArrayList<>(opportunities);
    }
    
    /**
     * Clears all stored opportunities (used for testing or system reset).
     */
    public void clear() {
        opportunities.clear();
    }
}
