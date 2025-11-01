package entity.domain;


import java.time.LocalDateTime;
import java.util.Objects;

import entity.domain.enums.ApplicationStatus;

/**
 * Represents a student's application for an internship opportunity.
 * Got some issues generating the application ID. It keeps returning null. Need to see whats wrong.
 */
public class Application {

    // ===== Attributes =====
    private String id;
    private Student student;
    private InternshipOpportunity opportunity;
    private LocalDateTime appliedAt;
    private ApplicationStatus status;
    private boolean withdrawalRequested;

    // ===== Constructors =====
    public Application() {
        // Default constructor 
    }

    public Application(String id, Student student, InternshipOpportunity opportunity) {
        this.id = id;
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

    public void setId(String id) {
        this.id = id;
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
