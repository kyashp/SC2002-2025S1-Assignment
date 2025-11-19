package entity.domain;


import entity.domain.enums.ApplicationStatus;
import java.time.LocalDateTime;
import java.util.Objects;
import util.IdGenerator;

/**
 * Represents a student's application for an internship opportunity.
 * Got some issues generating the application ID. It keeps returning null. Need to see whats wrong.
 */
public class Application {


    // ===== Static shared ID generator ======
    private static final IdGenerator idGen = new IdGenerator();
    // ===== Attributes =====
    private final String id =  idGen.newId("A");
    private Student student;
    private InternshipOpportunity opportunity;
    private LocalDateTime appliedAt;
    private ApplicationStatus status;
    private boolean withdrawalRequested;
    private LocalDateTime lastUpdated = LocalDateTime.now();
    // ===== Constructors =====
    public Application() {
        // Default constructor 
    }

    public Application(Student student, InternshipOpportunity opportunity) {
        this.student = student;
        this.opportunity = opportunity;
        this.appliedAt = LocalDateTime.now();
        this.status = ApplicationStatus.PENDING;
        this.withdrawalRequested = false;
    }

    // ===== Getters & Setters =====
    public String getId() {
        return id;
    }

    

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public InternshipOpportunity getOpportunity() {
        return opportunity;
    }

    public void setOpportunity(InternshipOpportunity opportunity) {
        this.opportunity = opportunity;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
        this.lastUpdated = LocalDateTime.now();
    }

    public LocalDateTime getLastUpdated(){
        return lastUpdated;
    }

    public boolean isWithdrawalRequested() {
        return withdrawalRequested;
    }

    public void setWithdrawalRequested(boolean withdrawalRequested) {
        this.withdrawalRequested = withdrawalRequested;
    }

    // ===== Business Logic =====

    /**
     * Marks this application as successful.
     */
    public void markSuccessful() {
        this.status = ApplicationStatus.SUCCESSFUL;
        System.out.println("Application " + id + " marked as SUCCESSFUL.");
    }

    /**
     * Marks this application as unsuccessful.
     */
    public void markUnsuccessful() {
        this.status = ApplicationStatus.UNSUCCESSFUL;
        System.out.println("Application " + id + " marked as UNSUCCESSFUL.");
    }

    @Override
    public String toString() {
        return String.format("Application[%s, Student: %s, Opportunity: %s, Status: %s, AppliedAt: %s]",
                id,
                student != null ? student.getUserName() : "Unknown",
                opportunity != null ? opportunity.getTitle() : "Unknown",
                status,
                appliedAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Application)) return false;
        Application other = (Application) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
