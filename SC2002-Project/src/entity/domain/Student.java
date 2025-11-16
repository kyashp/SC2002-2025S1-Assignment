package entity.domain;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import entity.domain.enums.ApplicationStatus;
import repositories.ApplicationRepository;

/**
 * Represents a student in the Internship Placement Management System.
 * Extends the abstract User class.
 */

public class Student extends User {
	private int year;
	private String major;
	private Application acceptedPlacement;
    private Boolean visibility;
	
	/**
     * Constructs a new Student object with inputs
     * @param userId Matriculation number: Begins with U, 7 digits and ends with a letter
     * @param userName Student name
     * @param password Default: password, First time login students will be asked to change
     * @param year Student year of study (1-4)
     * @param major Student major (Computer Science)
     */
	public Student(String userId, String userName, int year, String major) {
		super(userId, userName);
		this.year = year;
		this.major = major;
		this.acceptedPlacement = null;
        this.visibility = false;
	}
	/**
     * Retrieves Student visibilty
     * @return boolean of Student visibility
     */
    public boolean getVisibility(){
        return this.visibility;
    }

    /**
     * Sets Student visibility
     * @param visibility boolean of Student visibility
     */
    public void setVisibility(boolean visibility){
        this.visibility = visibility;
    }
	/**
     * Retrieves Student year of study
     * @return int year (1-4)
     */
	public int getYear() {
		return year;
	}
	
    /**
     * Sets Student year of study
     * @param year Year of study (1-4)
     */
	public void setYear(int year) {
        if (year < 1 || year > 4) {
            throw new IllegalArgumentException("Year must be between 1 and 4.");
        }
        this.year = year;
    }

    /**
     * Retrieves Student major
     * @return String major (Computer Science)
     */
    public String getMajor() {
        return major;
    }

    /**
     * Sets Student major
     * @param major String major (Computer Science)
     */
    public void setMajor(String major) {
        this.major = major;
    }

    /**
     * Retrieves the acceptedPlacement
     * @return Application acceptedPlacement
     */
    public Application getAcceptedPlacement() {
        return acceptedPlacement;
    }
    /**
     * Sets a placement as an acceptedPlacement
     * @param acceptedPlacement
     */
    public void setAcceptedPlacement(Application acceptedPlacement) {
        this.acceptedPlacement = acceptedPlacement;
    }
    
    /**
     * Returns a list of opportunities a student is eligible for.
     * (Stub method — actual filtering is handled by OpportunityService)
     */
    
    public List<InternshipOpportunity> viewEligibleOpportunities(List<InternshipOpportunity> allOpps) {
        List<InternshipOpportunity> eligible = new ArrayList<>();
        for (InternshipOpportunity opp : allOpps) {
            if (opp.isOpenFor(this)) {
                eligible.add(opp);
            }
        }
        return eligible;
    }
    
    /**
     * Allows a student to apply for an opportunity.
     * Uses the ApplicationRepository to store the application.
     */
    public void apply(InternshipOpportunity opp, ApplicationRepository appRepo) {
        Objects.requireNonNull(opp, "Opportunity required");
        Objects.requireNonNull(appRepo, "ApplicationRepository required");

        // Limit: Max 3 concurrent applications
        List<Application> myApps = appRepo.findByStudent(this);
        long pendingCount = myApps.stream()
                .filter(a -> a.getStatus() == ApplicationStatus.PENDING)
                .count();

        if (pendingCount >= 3) {
            System.out.println("You have reached the maximum of 3 concurrent applications.");
            return;
        }

        Application newApp = new Application(this,opp);

        appRepo.save(newApp);
        System.out.println(getUserName() + " applied for " + opp.getTitle());
    }

    /**
     * Lists all applications made by this student.
     */
    public List<Application> viewMyApplications(ApplicationRepository appRepo) {
        Objects.requireNonNull(appRepo, "ApplicationRepository required");
        return appRepo.findByStudent(this);
    }

    /**
     * Allows the student to accept an offer (successful application).
     */
    public void accept(Application app, ApplicationRepository appRepo) {
        Objects.requireNonNull(app, "Application required");
        Objects.requireNonNull(appRepo, "ApplicationRepository required");

        if (app.getStatus() == ApplicationStatus.SUCCESSFUL) {
            this.acceptedPlacement = app;
            appRepo.save(app);
            System.out.println(getUserName() + " accepted the offer for " + app.getOpportunity().getTitle());
        } else {
            System.out.println("Cannot accept an application unless application status is SUCCESSFUL.");
        }
    }

    @Override
    public String toString() {
        return String.format("Student[%s, %s, Year %d, Major: %s]",
                getUserId(), getUserName(), year, major);
    }
}

