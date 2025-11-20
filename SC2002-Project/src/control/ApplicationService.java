package control;

import entity.domain.Application;
import entity.domain.CareerCenterStaff;
import entity.domain.CompanyRepresentative;
import entity.domain.InternshipOpportunity;
import entity.domain.Student;
import entity.domain.WithdrawalRequest;
import entity.domain.enums.ApplicationStatus;
import entity.domain.enums.OpportunityStatus;
import entity.domain.enums.RequestStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import repositories.ApplicationRepository;
import repositories.OpportunityRepository;
import util.Validator;


/* <<service>> ApplicationService
 * Handles student applications, company reviews, and withdrawal processing
 */

public class ApplicationService {
	// ===== Dependencies =====
	private final ApplicationRepository applicationRepository;
	private final OpportunityRepository opportunityRepository;
	private final Validator validator;

	
	public ApplicationService(ApplicationRepository applicationRepository,
			OpportunityRepository opportunityRepository,
			Validator validator) {
		this.applicationRepository = Objects.requireNonNull(applicationRepository, "ApplicationRepository required");
		this.opportunityRepository = Objects.requireNonNull(opportunityRepository, "OpportunityRepository required");
		this.validator =  Objects.requireNonNull(validator, "Validator required");
	}

	// Core Methods
	
	/*
	 * Allows a student to apply for an internship opportunity.
	 * Validates eligibility and open/close dates.
	 */
	public Application apply(Student student, InternshipOpportunity opp) {
		Objects.requireNonNull(student, "Student required");
		Objects.requireNonNull(opp, "Opportunity required");
		
		
		if (opp.getStatus() != OpportunityStatus.APPROVED || !opp.isVisibility()) {
			throw new IllegalStateException("Opportunity is not open for Application.");
		}
		
		if (!opp.isOpenFor(student)) {
			throw new IllegalStateException("Opportunity not currently open for this student.");
		}
		
		Application app = new Application();
		app.setStudent(student);
		app.setOpportunity(opp);
		app.setAppliedAt(LocalDateTime.now());
		app.setStatus(ApplicationStatus.PENDING);
		app.setWithdrawalRequested(false);
		
		applicationRepository.save(app);
		System.out.println(student.getUserName() + " applied to " + opp.getTitle());
		return app;
	}

	/*
	 * Returns all the applications submitted by a Student.
	 */
	
	public List<Application> listStudentApplications(Student student){
		Objects.requireNonNull(student, "Student required");
		return applicationRepository.findByStudent(student);
	}
	
	/*
	 * Allows a Company rep to review a student's application.
	 * Approve = SUCCESSFUL, Reject = UNSUCCESSFUL
	 */
	
	public void companyReview(CompanyRepresentative rep, Application app, boolean approve) {
		Objects.requireNonNull(rep, "Company representative required");
		Objects.requireNonNull(app, "Application required");
		
		if (!app.getOpportunity().getRepInCharge().equals(rep)) {
			throw new IllegalArgumentException("This representative is not assigned to the opportunity.");
		}
		
		if (approve) {
			app.setStatus(ApplicationStatus.SUCCESSFUL);
			System.out.println("Application marked as SUCCESSFUL by: " + rep.getCompanyName());
		} else {
			app.setStatus(ApplicationStatus.UNSUCCESSFUL);
			System.out.println("Application marked as UNSUCCESSFUL by: " + rep.getCompanyName());
			
		}
		applicationRepository.save(app);
	}
	
	/*
	 * Allows a student to accept a successful application
	 * Updates opportunity slots and sets filled status  if necessary
	 */
	
	public void studentAccept(Application app) {
		Objects.requireNonNull(app, "Application required");
		
		if (app.getStatus() != ApplicationStatus.SUCCESSFUL) {
            throw new IllegalStateException("Only successful applications can be accepted.");
        }
		
		InternshipOpportunity opp = app.getOpportunity();
		opp.setSlots(opp.getSlots() - 1);
		
		if (opp.getSlots() <= 0) {
			opp.setStatus(OpportunityStatus.FILLED);
		}
		
		opportunityRepository.save(opp);
		applicationRepository.save(app);
		System.out.println("Student accepted the offer for " + opp.getTitle());
		
	}
	
	
	
	/**
     * Allows a student to request a withdrawal (before or after acceptance).
     */
    public WithdrawalRequest requestWithdrawal(Student student, Application app, String reason) {
        Objects.requireNonNull(student, "Student required");
        Objects.requireNonNull(app, "Application required");
        
        WithdrawalRequest req = new WithdrawalRequest();
        req.setApplication(app);
        req.setRequestedBy(student);
        req.setReason(reason);
        req.setStatus(RequestStatus.PENDING);
        req.setRequestedAt(LocalDateTime.now());

        app.setWithdrawalRequested(true);
        applicationRepository.save(app);
        System.out.println(student.getUserName() + " requested withdrawal for " + app.getOpportunity().getTitle());

        return req;
    }
    
    /**
     * Allows Career Center Staff to process a withdrawal request.
     * If approved, reopens a slot in the opportunity.
     */
    public void processWithdrawal(CareerCenterStaff staff, WithdrawalRequest req, boolean approve) {
        Objects.requireNonNull(staff, "Staff required");
        Objects.requireNonNull(req, "Request required");

        Application app = req.getApplication();
        InternshipOpportunity opp = app.getOpportunity();

        if (approve) {
            req.setStatus(RequestStatus.APPROVED);
            app.setStatus(ApplicationStatus.WITHDRAWN);
            opp.setSlots(opp.getSlots() + 1);

            if (opp.getStatus() == OpportunityStatus.FILLED && opp.getSlots() > 0) {
                opp.setStatus(OpportunityStatus.APPROVED); // reopen if slot freed
            }

            System.out.println("Withdrawal approved by staff: " + staff.getUserName());
        } else {
            req.setStatus(RequestStatus.REJECTED);
            System.out.println("Withdrawal rejected by staff: " + staff.getUserName());
        }

        opportunityRepository.save(opp);
        applicationRepository.save(app);
    }
}

