package control;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import entity.domain.CompanyRepresentative;
import entity.domain.InternshipOpportunity;
import entity.domain.OpportunityFilter;
import entity.domain.OpportunityFilter.SortKey;
import entity.domain.Student;
import entity.domain.enums.OpportunityStatus;
import entity.domain.enums.InternshipLevel;
import repositories.OpportunityRepository;
import util.Validator;

/* <<service>> OpportunityService
 * Handles the creation, approval, visibility and listing of internship opportunities.
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

    /* Allows a Company representative to draft a new internship opportunity.
     * Basic validation checks are applied before saving the draft.
     */
    public InternshipOpportunity createOpportunity(CompanyRepresentative rep, InternshipOpportunity draft) {
        Objects.requireNonNull(rep, "Company representative required");
        Objects.requireNonNull(draft, "Opportunity draft required");

        // validate mandatory fields
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

    /* Submits a drafted opportunity for approval by Career Center Staff. */
    public void submitForApproval(InternshipOpportunity opp) {
        Objects.requireNonNull(opp, "Opportunity required");
        if (opp.getStatus() != OpportunityStatus.PENDING) {
            System.out.println("Only draft (PENDING) opportunities can be submitted.");
            return;
        }
        opportunityRepository.save(opp);
        System.out.println("Opportunity submitted for approval: " + opp.getTitle());
    }

    /* Approves an opportunity. */
    public void approve(InternshipOpportunity opp) {
        Objects.requireNonNull(opp, "Opportunity required");
        opp.setStatus(OpportunityStatus.APPROVED);
        opportunityRepository.save(opp);
        System.out.println("Opportunity approved: " + opp.getTitle());
    }

    /* Rejects an opportunity. */
    public void reject(InternshipOpportunity opp) {
        Objects.requireNonNull(opp, "Opportunity required");
        opp.setStatus(OpportunityStatus.REJECTED);
        opportunityRepository.save(opp);
        System.out.println("Opportunity rejected " + opp.getTitle());
    }

    /** Allows Company Representatives to toggle visibility of approved opportunities. */
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
     * STUDENT: Returns visible & approved opportunities open to a given student
     * using DEFAULT alphabetical (title) sorting.
     */
    public List<InternshipOpportunity> listVisibleFor(Student student) {
        return listVisibleFor(student, null);
    }

    /**
     * STUDENT: Returns visible & approved opportunities, applying a user filter + sorting.
     * Null filter means default alphabetical sorting without extra constraints.
     */
    public List<InternshipOpportunity> listVisibleFor(Student student, OpportunityFilter filter) {
        Objects.requireNonNull(student, "Student required");

        List<InternshipOpportunity> all = opportunityRepository.findAll();
        List<InternshipOpportunity> eligible = all.stream()
                .filter(o -> o.getStatus() == OpportunityStatus.APPROVED)
                .filter(InternshipOpportunity::isVisibility)   // visible to students
                .filter(o -> isEligible(student, o))
                .filter(o -> filterMatch(o, filter))
                .collect(Collectors.toList());

        return applySort(eligible, filter); // default TITLE_ASC if filter null
    }

    /** REP: list own company opportunities with optional filter + sorting. */
    public List<InternshipOpportunity> listByCompanyFiltered(String company, OpportunityFilter filter) {
        if (company == null || company.isBlank()) return new ArrayList<>();
        List<InternshipOpportunity> base = opportunityRepository.findByCompany(company);
        List<InternshipOpportunity> out = base.stream()
                .filter(o -> filterMatch(o, filter))
                .collect(Collectors.toList());
        return applySort(out, filter);
    }

    /** STAFF: list all opportunities with optional filter + sorting. */
    public List<InternshipOpportunity> listAllFiltered(OpportunityFilter filter) {
        List<InternshipOpportunity> base = opportunityRepository.findAll();
        List<InternshipOpportunity> out = base.stream()
                .filter(o -> filterMatch(o, filter))
                .collect(Collectors.toList());
        return applySort(out, filter);
    }

    /** Existing method kept (used elsewhere). */
    public List<InternshipOpportunity> listByCompany(String company) {
        if (company == null || company.isBlank()) return new ArrayList<>();
        return opportunityRepository.findByCompany(company);
    }

    /** Updates status to FILLED once all slots are occupied. */
    public void updateFilledStatus(InternshipOpportunity opp) {
        Objects.requireNonNull(opp, "Opportunity required");
        if (opp.getSlots() <= 0) {
            opp.setStatus(OpportunityStatus.FILLED);
            opportunityRepository.save(opp);
            System.out.println("Opportunity filled: " + opp.getTitle());
        }
    }

    // ===== Helper Methods =====

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

    /** Apply user-supplied filters. Null filter = accept all. */
    private boolean filterMatch(InternshipOpportunity o, OpportunityFilter f) {
        if (f == null) return true;

        if (f.getStatus() != null && o.getStatus() != f.getStatus()) return false;

        if (f.getPreferredMajor() != null) {
            if (o.getPreferredMajor() == null ||
                !o.getPreferredMajor().equalsIgnoreCase(f.getPreferredMajor())) return false;
        }

        if (f.getLevel() != null && o.getLevel() != f.getLevel()) return false;

        if (f.getClosingBefore() != null) {
            if (o.getCloseDate() == null || o.getCloseDate().isAfter(f.getClosingBefore())) return false;
        }

        return true;
    }

    /** Default alphabetical sort if filter is null or no sortKey set. */
    private List<InternshipOpportunity> applySort(List<InternshipOpportunity> list, OpportunityFilter f) {
    OpportunityFilter.SortKey key = (f == null ? OpportunityFilter.SortKey.TITLE_ASC : f.getSortKey());
    Comparator<InternshipOpportunity> c;

    switch (key) {
        case CLOSING_DATE_ASC -> c = Comparator.comparing(
                InternshipOpportunity::getCloseDate,
                Comparator.nullsLast(Comparator.naturalOrder())
        );
        case COMPANY_ASC -> c = Comparator.comparing(
                InternshipOpportunity::getCompanyName,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
        );
        case LEVEL_ASC -> c = Comparator.comparing(
                InternshipOpportunity::getLevel,
                Comparator.nullsLast(Comparator.naturalOrder())
        );
        case TITLE_ASC -> c = Comparator.comparing(
                InternshipOpportunity::getTitle,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
        );
        default -> c = Comparator.comparing(
                InternshipOpportunity::getTitle,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
        );
    }
    list.sort(c);
    return list;
}

}
