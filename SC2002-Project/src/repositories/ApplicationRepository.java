package repositories;



import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import entity.domain.Application;
import entity.domain.Student;
import entity.domain.InternshipOpportunity;
import entity.domain.enums.ApplicationStatus;

/**
 * <<Repository>> ApplicationRepository
 * Stores and retrieves Application entities.
 * Acts as an in-memory data access layer for the Applications by Students
 */
public class ApplicationRepository {

    //Storage
    private final List<Application> applications = new ArrayList<>();

    //CRUD-like Operations

    /**
     * Saves or updates an application.
     * If an application with the same id exists, it is replaced.
     * @param app Application to be saved
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
     * @param student Student to retrieve application for
     * @return List of student's application
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
     * Returns all applications for a given opportunity created by Company Representative.
     * @param opp InternshipOpportunity
     * @return List of all application by internship listed
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
     * @param opp InternshipOpportunity
     * @return Count of SUCCESSFULL applications for an internship
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

    /** 
     * Finds an application by id (useful for updates) 
     * @param id Application Id
     */
    public Application findById(String id) {
        if (id == null) return null;
        for (Application app : applications) {
            if (id.equalsIgnoreCase(app.getId())) {
                return app;
            }
        }
        return null;
    }

    /**
     * Returns a copy of all applications (for testing/debug) hi 
     */
    public List<Application> findAll() {
        return new ArrayList<>(applications);
    }

    /**
     * Clears the repository (for testing or reset)
     */
    public void clear() {
        applications.clear();
    }
}
