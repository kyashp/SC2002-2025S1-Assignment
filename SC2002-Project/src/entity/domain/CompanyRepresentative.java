package entity.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import entity.domain.enums.OpportunityStatus;
import repositories.OpportunityRepository;
import repositories.ApplicationRepository;

/**
 * Represents a company representative who can create internship opportunities
 * and review student applications.
 */
public class CompanyRepresentative extends User{
    private String companyName;
    private String department;
    private String position;
    private boolean isApproved;

    /**
     * Constructs
     * @param userId
     * @param username
     * @param password
     * @param companyName
     * @param department
     * @param position
     */
    public CompanyRepresentative(String userId, String username, String password,
            String companyName, String department, String position) {
		super(userId, username);
		this.companyName = companyName;
		this.department = department;
		this.position = position;
		this.isApproved = false; // Default: unapproved until staff approval
    }
    
    
    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        this.isApproved = approved;
    }
    
    
    // ===== Behaviors =====

    /**
     * Creates a new internship opportunity in draft (PENDING) state.
     */
    
    public InternshipOpportunity createOpportunity(String id, String title, String description, 
    		entity.domain.enums.InternshipLevel level, 
    		String preferredMajor, int slots) {
    	
    	InternshipOpportunity opp = new InternshipOpportunity(id, title, description, level, 
    			preferredMajor, companyName, this, slots);
    	
    	opp.setStatus(OpportunityStatus.PENDING);
        opp.setVisibility(false);
        System.out.println(getUserName() + " created opportunity: " + title);
        return opp;
    	
    }
    
    /**
     * Returns a list of all opportunities created by this representative.
     */
    public List<InternshipOpportunity> listMyOpportunities(OpportunityRepository oppRepo) {
        Objects.requireNonNull(oppRepo, "OpportunityRepository required");
        return oppRepo.findByCompany(companyName);
    }
    
    /**
     * Toggles visibility of an approved opportunity.
     */
    public void toggleVisibility(InternshipOpportunity opp, boolean on) {
        Objects.requireNonNull(opp, "Opportunity required");

        if (opp.getStatus() != OpportunityStatus.APPROVED) {
            System.out.println("Only approved opportunities can be made visible.");
            return;
        }

        opp.setVisibility(on);
        System.out.println("Visibility for " + opp.getTitle() + " set to " + (on ? "ON" : "OFF"));
    }
    
    /**
     * Returns all applications associated with a specific opportunity.
     */
    public List<Application> reviewApplications(InternshipOpportunity opp, ApplicationRepository appRepo) {
        Objects.requireNonNull(opp, "Opportunity required");
        Objects.requireNonNull(appRepo, "ApplicationRepository required");
        return new ArrayList<>(appRepo.findByOpportunity(opp));
    }

    @Override
    public String toString() {
        return String.format("CompanyRepresentative[%s, %s, %s, Approved: %b]",
                getUserId(), companyName, position, isApproved);
    }

}
