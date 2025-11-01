package entity.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import entity.domain.enums.InternshipLevel;
import entity.domain.enums.OpportunityStatus;

/**
 * Represents an internship opportunity created by a company representative.
 */
public class InternshipOpportunity {
	// ===== Attributes =====
    private String id;
    private String title;
    private String description;
    private InternshipLevel level;
    private String preferredMajor;
    private LocalDate openDate;
    private LocalDate closeDate;
    private OpportunityStatus status;
    private String companyName;
    private CompanyRepresentative repInCharge;
    private int slots;
    private boolean visibility;
    private final List<Application> applications = new ArrayList<>();
    
    // ===== Constructors =====
    public InternshipOpportunity() {
        // Default constructor
    }
    
    public InternshipOpportunity(String id, String title, String description,
    		InternshipLevel level, String preferredMajor, String companyName, 
    		CompanyRepresentative repInCharge,
    		int slots) {
    	this.id = id;
        this.title = title;
        this.description = description;
        this.level = level;
        this.preferredMajor = preferredMajor;
        this.companyName = companyName;
        this.repInCharge = repInCharge;
        this.slots = slots;
        this.status = OpportunityStatus.PENDING;
        this.visibility = false;
        this.openDate = LocalDate.now();
        this.closeDate = LocalDate.now().plusMonths(1); // default 1-month window
    }
    
 // ===== Getters & Setters =====
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public InternshipLevel getLevel() {
        return level;
    }

    public void setLevel(InternshipLevel level) {
        this.level = level;
    }

    public String getPreferredMajor() {
        return preferredMajor;
    }

    public void setPreferredMajor(String preferredMajor) {
        this.preferredMajor = preferredMajor;
    }

    public LocalDate getOpenDate() {
        return openDate;
    }

    public void setOpenDate(LocalDate openDate) {
        this.openDate = openDate;
    }

    public LocalDate getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(LocalDate closeDate) {
        this.closeDate = closeDate;
    }

    public OpportunityStatus getStatus() {
        return status;
    }

    public void setStatus(OpportunityStatus status) {
        this.status = status;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public CompanyRepresentative getRepInCharge() {
        return repInCharge;
    }

    public void setRepInCharge(CompanyRepresentative repInCharge) {
        this.repInCharge = repInCharge;
    }

    public int getSlots() {
        return slots;
    }

    public void setSlots(int slots) {
        if (slots < 0) throw new IllegalArgumentException("Slots cannot be negative.");
        this.slots = slots;
    }

    public boolean isVisibility() {
        return visibility;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    public List<Application> getApplications() {
        return new ArrayList<>(applications);
    }
    
 // ===== Business Logic =====

    /**
     * Allows a student to apply for this opportunity.
     * Creates and stores a new Application.
     */
    public Application apply(Student student) {
        Objects.requireNonNull(student, "Student required");

        if (!isOpenFor(student)) {
            System.out.println("Student not eligible or opportunity not open.");
            return null;
        }

        Application app = new Application("A-" + (applications.size() + 1), student, this);
        applications.add(app);
        System.out.println(student.getUserName() + " applied to " + title);
        return app;
    }

    /**
     * Checks whether this opportunity is open and suitable for the student.
     * - Must be within open/close date range.
     * - Must match student year eligibility.
     * - Must be visible and approved.
     */
    public boolean isOpenFor(Student student) {
        Objects.requireNonNull(student, "Student required");
        LocalDate now = LocalDate.now();

        boolean withinDate = (now.isAfter(openDate) || now.isEqual(openDate)) &&
                             (now.isBefore(closeDate) || now.isEqual(closeDate));

        boolean approvedAndVisible = (status == OpportunityStatus.APPROVED && visibility);
        boolean eligibleByYear = (student.getYear() >= 3) || (level == InternshipLevel.BASIC);

        return withinDate && approvedAndVisible && eligibleByYear;
    }

    /**
     * Updates the status to FILLED if all slots are taken.
     */
    public void updateStatusIfFilled() {
        if (slots <= 0 && status == OpportunityStatus.APPROVED) {
            status = OpportunityStatus.FILLED;
            System.out.println("Opportunity " + title + " is now FILLED.");
        }
    }

    @Override
    public String toString() {
        return String.format("InternshipOpportunity[%s, %s, %s, Level: %s, Status: %s, Slots: %d]",
                id, title, companyName, level, status, slots);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InternshipOpportunity)) return false;
        InternshipOpportunity other = (InternshipOpportunity) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    

}
