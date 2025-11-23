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

/**
 * Coordinates student applications, company review decisions, and withdrawal handling.
 * Delegates persistence to {@link ApplicationRepository} and {@link OpportunityRepository}.
 */

public class ApplicationService {
	// ===== Dependencies =====
	private final ApplicationRepository applicationRepository;
	private final OpportunityRepository opportunityRepository;
	private final Validator validator;

	/**
	 * Creates an ApplicationService with its required collaborators.
	 *
	 * @param applicationRepository repository for persisting applications
	 * @param opportunityRepository repository for persisting opportunities
	 * @param validator utility used for any validation needs
	 */
	public ApplicationService(ApplicationRepository applicationRepository,
			OpportunityRepository opportunityRepository,
			Validator validator) {
		this.applicationRepository = Objects.requireNonNull(applicationRepository, "ApplicationRepository required");
		this.opportunityRepository = Objects.requireNonNull(opportunityRepository, "OpportunityRepository required");
		this.validator =  Objects.requireNonNull(validator, "Validator required");
	}

	// Core Methods
	
	/**
	 * Allows a student to apply for an internship opportunity after validating eligibility
	 * and ensuring the opportunity is open.
	 *
	 * @param student the student submitting the application
	 * @param opp the opportunity to apply for
	 * @return the newly created {@link Application}
	 * @throws IllegalStateException if the opportunity is not open for applications
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

	/**
	 * Returns all applications submitted by the specified student.
	 *
	 * @param student the student whose applications should be retrieved
	 * @return list of applications belonging to the student
	 */
	public List<Application> listStudentApplications(Student student){
		Objects.requireNonNull(student, "Student required");
		return applicationRepository.findByStudent(student);
	}
	
	/**
	 * Allows a company representative to review a student's application, marking it as
	 * successful or unsuccessful.
	 *
	 * @param rep the representative performing the review
	 * @param app the application under review
	 * @param approve {@code true} to approve (SUCCESSFUL), {@code false} to reject (UNSUCCESSFUL)
	 */
    public void companyReview(CompanyRepresentative rep, Application app, boolean approve) {
        Objects.requireNonNull(rep, "Company representative required");
        Objects.requireNonNull(app, "Application required");
        
        InternshipOpportunity opp = app.getOpportunity();
        CompanyRepresentative owner = opp.getRepInCharge();
        boolean sameCompany = opp.getCompanyName() != null &&
                opp.getCompanyName().equalsIgnoreCase(rep.getCompanyName());
        if (owner == null && sameCompany) {
            // re-attach owner if missing after import
            opp.setRepInCharge(rep);
            owner = rep;
        }
        if (owner != null &&
                !owner.getUserId().equalsIgnoreCase(rep.getUserId()) &&
                !sameCompany) {
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
	
	/**
	 * Allows a student to accept a successful application, decrementing slots and
	 * updating the opportunity status if filled.
	 *
	 * @param app the successful application being accepted
	 * @throws IllegalStateException if the application is not in SUCCESSFUL status
	 */
	public void studentAccept(Application app) {
		Objects.requireNonNull(app, "Application required");
		
		if (app.getStatus() != ApplicationStatus.SUCCESSFUL) {
            throw new IllegalStateException("Only successful applications can be accepted.");
        }

        Student student = app.getStudent();
        // Withdraw all other applications for this student.
        List<Application> all = applicationRepository.findByStudent(student);
        for (Application other : all) {
            if (other.getId().equals(app.getId())) continue;
            if (other.getStatus() != ApplicationStatus.WITHDRAWN) {
                other.setStatus(ApplicationStatus.WITHDRAWN);
                other.setWithdrawalRequested(true);
                applicationRepository.save(other);
            }
        }

        // Mark accepted offer.
        app.setStatus(ApplicationStatus.ACCEPTED);
        if (student != null) {
            student.setAcceptedPlacement(app);
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
     *
     * @param student the student requesting withdrawal
     * @param app the application to withdraw from
     * @param reason free-form reason supplied by the student
     * @return the created withdrawal request
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
     * Allows Career Center Staff to process a withdrawal request, optionally reopening
     * a slot on approval.
     *
     * @param staff the staff member making the decision
     * @param req the withdrawal request being processed
     * @param approve {@code true} to approve; {@code false} to reject
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

