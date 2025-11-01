package Control;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import entity.domain.CompanyRepresentative;
import entity.domain.InternshipOpportunity;
import entity.domain.Student;
import entity.domain.enums.OpportunityStatus;
import entity.domain.enums.InternshipLevel;
import repositories.OpportunityRepository;
import util.Validator;


/* <<service>> OpportunityService
 *  Handles the creation, approval, visibility and listing of internship opportunities.
 */
public class OpportunityService {
	
	// ===== Dependencies =====
	private final OpportunityRepository opportunityRepository;
	private final Validator validator;
	
    // ===== Constructor =====
	public OpportunityService(OpportunityRepository opportunityRepository, Validator validator) {
        this.opportunityRepository = Objects.requireNonNull(opportunityRepository, "OpportunityRepository required");
        this.validator = Objects.requireNonNull(validator, "Validator required");
    }
	
	
	/*Allows a Company representative to draft a new internship opportunity.
	 * Basic validation checks are applied before saving the draft.
	 */
	
	public InternshipOpportunity createOpportunity(CompanyRepresentative rep, InternshipOpportunity draft) {
		Objects.requireNonNull(rep, "Company representative required");
		Objects.requireNonNull(draft, "Opportunity ddraft requried");
		
		//validate mandatory fields
		
		if (draft.getTitle() == null || draft.getTitle().isBlank()) {
			throw new IllegalArgumentException("Opportunity title cannot be empty.");
		}
		
		if (!validator.isValidCompanyEmail(rep.getUserName())) {
            throw new IllegalArgumentException("Invalid company representative email.");
        }
		
		draft.setRepInCharge(rep);
        draft.setCompanyName(rep.getCompanyName());
        draft.setStatus(OpportunityStatus.PENDING);
        draft.setVisibility(false); // initially hidden until approval

        opportunityRepository.save(draft);
        System.out.println("Draft opportunity created by " + rep.getCompanyName() + ": " + draft.getTitle());
        return draft;
	}
	
	/* Submits a drafted opportunity for approval by Career Center Staff.
	 * 
	 */
	
	public void submitForApproval(InternshipOpportunity opp) {
		Objects.requireNonNull(opp, "Opportunity required");
		if (opp.getStatus() != OpportunityStatus.PENDING) {
			System.out.println("Only draft (PENDING) opportunities can be submitted.");
			return;
		}
		
		opportunityRepository.save(opp);
		System.out.println("Opportunity submitted for approval: " + opp.getTitle());
		
	}
	
	 /*
     * Approves an opportunity, making it visible (if rep chooses to turn it on) and active for applications.
     */
	
	public void approve(InternshipOpportunity opp) {
		Objects.requireNonNull(opp, "Opportunity required");
		opp.setStatus(OpportunityStatus.APPROVED);
		opportunityRepository.save(opp);
		System.out.println("Opportunity approved: " + opp.getTitle());
		
	}
	
	/*
	 * Rejects an opportunity
	 */
	
	public void reject(InternshipOpportunity opp) {
		Objects.requireNonNull(opp, "Opportunity required");
		opp.setStatus(OpportunityStatus.REJECTED);
		opportunityRepository.save(opp);
		System.out.println("Opportunity rejected " + opp.getTitle());
	}
	/**
     * Allows Company Representatives to toggle visibility of approved opportunities.
     */
    public void setVisibility(InternshipOpportunity opp, boolean on) {
        Objects.requireNonNull(opp, "Opportunity required");

        if (opp.getStatus() != OpportunityStatus.APPROVED) {
            System.out.println("Only approved opportunities can be toggled for visibility.");
            return;
        }

        opp.setVisibility(on);
        opportunityRepository.save(opp);
        System.out.println("Visibility for '" + opp.getTitle() + "' set to: " + (on ? "ON" : "OFF"));
    }

    /**
     * Returns all visible & approved opportunities open to a given student.
     * Includes eligibility check based on student year and opportunity level.
     */
    public List<InternshipOpportunity> listVisibleFor(Student student) {
        Objects.requireNonNull(student, "Student required");

        List<InternshipOpportunity> allOpps = opportunityRepository.findApprovedVisibleByFilter(null);
        List<InternshipOpportunity> eligible = new ArrayList<>();

        for (InternshipOpportunity opp : allOpps) {
            if (isEligible(student, opp)) {
                eligible.add(opp);
            }
        }

        return eligible;
    }

    /**
     * Returns all opportunities belonging to a specific company.
     */
    public List<InternshipOpportunity> listByCompany(String company) {
        if (company == null || company.isBlank()) return new ArrayList<>();
        return opportunityRepository.findByCompany(company);
    }

    /**
     * Updates status to FILLED once all slots are occupied.
     */
    public void updateFilledStatus(InternshipOpportunity opp) {
        Objects.requireNonNull(opp, "Opportunity required");

        if (opp.getSlots() <= 0) {
            opp.setStatus(OpportunityStatus.FILLED);
            opportunityRepository.save(opp);
            System.out.println("Opportunity filled: " + opp.getTitle());
        }
    }

    // ===== Helper Method =====

    /**
     * Checks if a student is eligible for a given opportunity.
     * Y1–Y2: BASIC only; Y3–Y4: BASIC, INTERMEDIATE, ADVANCED.
     */
    private boolean isEligible(Student student, InternshipOpportunity opp) {
        InternshipLevel level = opp.getLevel();
        int year = student.getYear();

        if (year <= 2 && level == InternshipLevel.BASIC) return true;
        if (year >= 3) return true; // can apply to all levels
        return false;
    }
}
