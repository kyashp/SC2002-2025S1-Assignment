package entity.domain;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import entity.domain.enums.ApplicationStatus;
import repositories.ApplicationRepository;

/**
 * Represents a student in the internship placement system.
 * Extends the abstract User class.
 */

public class Student extends User {
	// ===== Attributes =====
	private int year;
	private String major;
	private Application acceptedPlacement;
	
	// ===== Constructors =====
	public Student(String userId, String userName, String password, int year, String Major) {
		super(userId, userName, password);
		this.year = year;
		this.major = major;
		this.acceptedPlacement = null;
	}
	
	// ===== Getters & Setters =====
	public int getYear() {
		return year;
	}
	
	public void setYear(int year) {
        if (year < 1 || year > 4) {
            throw new IllegalArgumentException("Year must be between 1 and 4.");
        }
        this.year = year;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public Application getAcceptedPlacement() {
        return acceptedPlacement;
    }

    public void setAcceptedPlacement(Application acceptedPlacement) {
        this.acceptedPlacement = acceptedPlacement;
    }
    
    // ===== Behaviors =====

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

        Application newApp = new Application(
                "A" + (myApps.size() + 1), // Simplified; can use IdGenerator in real implementation
                this,
                opp
        );

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
            System.out.println("Cannot accept an application that was not marked SUCCESSFUL.");
        }
    }

    @Override
    public String toString() {
        return String.format("Student[%s, %s, Year %d, Major: %s]",
                getUserId(), getUserName(), year, major);
    }

	

	
}

