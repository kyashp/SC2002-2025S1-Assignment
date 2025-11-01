package repositories;



import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import entity.domain.Application;
import entity.domain.Student;
import entity.domain.InternshipOpportunity;
import entity.domain.enums.ApplicationStatus;

/**
 * <<repository>> ApplicationRepository
 * Stores and retrieves Application entities.
 */
public class ApplicationRepository {

    // ===== Storage =====
    private final List<Application> applications = new ArrayList<>();

    // ===== CRUD-like ops =====

    /**
     * Saves or updates an application.
     * If an application with the same id exists, it is replaced.
     */
    public void save(Application app) {
        Objects.requireNonNull(app, "Application required");
        Application existing = findById(app.getId());
        if (existing != null) {
            applications.remove(existing);
        }
        applications.add(app);
    }

    /**
     * Returns all applications for a given student.
     */
    public List<Application> findByStudent(Student student) {
        Objects.requireNonNull(student, "Student required");
        List<Application> res = new ArrayList<>();
        for (Application app : applications) {
            if (app.getStudent().equals(student)) {
                res.add(app);
            }
        }
        return res;
    }

    /**
     * Returns all applications for a given opportunity.
     */
    public List<Application> findByOpportunity(InternshipOpportunity opp) {
        Objects.requireNonNull(opp, "Opportunity required");
        List<Application> res = new ArrayList<>();
        for (Application app : applications) {
            if (app.getOpportunity().equals(opp)) {
                res.add(app);
            }
        }
        return res;
    }

    /**
     * Counts applications with status SUCCESSFUL for the given opportunity.
     */
    public int countSuccessfulByOpportunity(InternshipOpportunity opp) {
        Objects.requireNonNull(opp, "Opportunity required");
        int count = 0;
        for (Application app : applications) {
            if (app.getOpportunity().equals(opp)
                && app.getStatus() == ApplicationStatus.SUCCESSFUL) {
                count++;
            }
        }
        return count;
    }

    // ===== Helpers (optional) =====

    /** Finds an application by id (useful for updates). */
    public Application findById(String id) {
        if (id == null) return null;
        for (Application app : applications) {
            if (id.equalsIgnoreCase(app.getId())) {
                return app;
            }
        }
        return null;
    }

    /** Returns a copy of all applications (for testing/debug). */
    public List<Application> findAll() {
        return new ArrayList<>(applications);
    }

    /** Clears repository (for tests/resets). */
    public void clear() {
        applications.clear();
    }
}
