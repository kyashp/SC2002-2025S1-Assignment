package entity.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import entity.domain.enums.OpportunityStatus;
import entity.domain.enums.RequestStatus;
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
    private RequestStatus isApproved;

    /**
     * Constructs a new Company Representative object with inputs
     * @param userId Company email
     * @param username Name of Representative
     * @param password Password for Representative account
     * @param companyName Company Name
     * @param department Department (ex. Sales)
     * @param position Position (ex. HR)
     */
    public CompanyRepresentative(String userId, String username, String password,
            String companyName, String department, String position) {
		super(userId, username);
        super.setPassword(password);
		this.companyName = companyName;
		this.department = department;
		this.position = position;
		this.isApproved = RequestStatus.PENDING; // Default: unapproved until staff approval
    }
    
    /**
     * Retrieves Company Name
     * @return String companyName
     */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * Sets company name
     * @param companyName String companyName
     */
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    /**
     * Retrieves department
     * @return String department
     */
    public String getDepartment() {
        return department;
    }

    /**
     * Sets department
     * @param department String department
     */
    public void setDepartment(String department) {
        this.department = department;
    }

    /**
     * Retrieves position
     * @return String position
     */
    public String getPosition() {
        return position;
    }


    /**
     * Sets position
     * @param position String position
     */
    public void setPosition(String position) {
        this.position = position;
    }

    /**
     * Retrieves status of account approval
     * @return boolean isApproved;
     */
    public RequestStatus isApproved() {
        return isApproved;
    }

    /**
     * Sets status of account approval
     * @param approved boolean approved;
     */
    public void setApproved(RequestStatus approved) {
        this.isApproved = approved;
    }

    /**
     * Creates a new internship opportunity in draft (PENDING) state.
     * @param id Internship id
     * @param title Internship title
     * @param description Internship description
     * @param level Internship level (Basic, Intermediate, Advanced)
     * @param preferredMajor Preferred major (Computer Science)
     * @param slots Number of slots 0<n<=10
     * @return
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
     * @param oppRepo OpportunityRepository
     * @return List of opportunities within the company
     */
    public List<InternshipOpportunity> listMyOpportunities(OpportunityRepository oppRepo) {
        Objects.requireNonNull(oppRepo, "OpportunityRepository required");
        return oppRepo.findByCompany(companyName);
    }
    
    /**
     * Toggles visibility of an approved opportunity.
     * @param opp InternshipOpportunity
     * @param on Visibility (True/False)
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
     * @param opp InternshipOpportunity
     * @param appRepo ApplicationRepository
     * @return List of all applications for an InternshipOpportunity
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
