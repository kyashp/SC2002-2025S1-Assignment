package repositories;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import entity.domain.CompanyRepresentative;
import entity.domain.InternshipOpportunity;
import entity.domain.ReportFilter;
import entity.domain.enums.OpportunityStatus;
import entity.domain.enums.InternshipLevel;

/** 
 * <<Repository>> OpportunityRepository
 * Stores and retrieves InternshipOpportunity entities
 * Acts as an in-memory data access layer for all the listed Interns
 */
public class OpportunityRepository {
	private final List<InternshipOpportunity> opportunities = new ArrayList<>();

	/**
     * Saves or updates an internship opportunity.
	 * If the opportunity already exits (same Id), replaces it.
     * @param opp InternshipOpportunity
     */
	public void save(InternshipOpportunity opp) {
		Objects.requireNonNull(opp, "Opportunity cannot be null");
		
		InternshipOpportunity existing = findById(opp.getId());
		if (existing != null) {
			opportunities.remove(existing);
		}
		opportunities.add(opp);
	}
	
	/**
     * Finds an opportunity by its unique ID.
     * @param id InternshipOpportunity Id
     * @return InternshipOpportunity if there is one by that Id, null if none
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
	
	/**
     * Finds all approved & visible opportunities that match the given report filter.
	 * If filter is null, returns all approved & visible opportunities.
     * @param filter ReportFilter for the filter settings
     * @return List of InternshipOpportunity by the filter
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

                if (filter.getOpenDateFrom() != null && opp.getOpenDate().isBefore(filter.getOpenDateFrom())) {
                    match = false;
                }
                if (filter.getCloseDateBy() != null && opp.getCloseDate().isAfter(filter.getCloseDateBy())) {
                    match = false;
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
     * @param company String companyName
     * @return List of InternshipOpportunity by the company
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
     * Returns all opportunities created by a specific representative.
     */
    public List<InternshipOpportunity> findByRepresentative(CompanyRepresentative rep) {
        List<InternshipOpportunity> result = new ArrayList<>();
        if (rep == null) return result;

        for (InternshipOpportunity opp : opportunities) {
            CompanyRepresentative creator = opp.getRepInCharge();
            if (creator != null && creator.getUserId().equalsIgnoreCase(rep.getUserId())) {
                result.add(opp);
            }
        }
        return result;
    }

    /**
     * Deletes the provided opportunity instance if it exists.
     * @return true if the opportunity was removed, false otherwise.
     */
    public boolean delete(InternshipOpportunity opp) {
        if (opp == null) return false;
        return opportunities.remove(opp);
    }
    
    /**
     * Clears all stored opportunities (used for testing or system reset).
     */
    public void clear() {
        opportunities.clear();
    }
}
