package repositories;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

import entity.domain.Application;
import entity.domain.Student;
import entity.domain.InternshipOpportunity;
import entity.domain.enums.ApplicationStatus;
import entity.domain.User;

/**
 * <<Repository>> ApplicationRepository
 * Stores and retrieves Application entities.
 * Acts as an in-memory data access layer for the Applications by Students
 */
public class ApplicationRepository {

    //Storage
    private final List<Application> applications = new ArrayList<>();
    private final String storagePath;
    private final UserRepository userRepository;
    private final OpportunityRepository opportunityRepository;
    private static final String HEADER = "Id,StudentId,OpportunityId,Status,AppliedAt,WithdrawalRequested";

    public ApplicationRepository() {
        this.storagePath = null;
        this.userRepository = null;
        this.opportunityRepository = null;
    }

    public ApplicationRepository(String storagePath, UserRepository userRepository, OpportunityRepository opportunityRepository) {
        this.storagePath = storagePath;
        this.userRepository = userRepository;
        this.opportunityRepository = opportunityRepository;
        loadFromDisk();
    }

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
        persist();
    }

    /**
     * Returns all applications for a given student.
     * @param student Student to retrieve application for
     * @return List of student's application
     */
    public List<Application> findByStudent(Student student) {
        Objects.requireNonNull(student, "Student required");
        reloadFromDisk();
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
        reloadFromDisk();
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
        reloadFromDisk();
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
        reloadFromDisk();
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
        reloadFromDisk();
        return new ArrayList<>(applications);
    }

    /**
     * Clears the repository (for testing or reset)
     */
    public void clear() {
        applications.clear();
        persist();
    }

    /** Reload applications from disk into memory. */
    public void reloadFromDisk() {
        loadFromDisk();
    }

    private void loadFromDisk() {
        if (storagePath == null || storagePath.isBlank()) return;
        File file = new File(storagePath);
        if (!file.exists()) {
            ensureParent(file);
            return;
        }

        List<Application> loaded = new ArrayList<>();
        int maxId = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean header = true;
            while ((line = br.readLine()) != null) {
                if (header) { header = false; continue; }
                if (line.isBlank()) continue;
                String[] t = line.split(",", -1);
                if (t.length < 6) continue;

                String id = t[0];
                String studentId = t[1];
                String oppId = t[2];
                String statusText = t[3];
                String appliedAtText = t[4];
                String withdrawal = t[5];

                Student student = null;
                User u = userRepository == null ? null : userRepository.findById(studentId);
                if (u instanceof Student s) student = s;

                InternshipOpportunity opp = opportunityRepository == null ? null : opportunityRepository.findById(oppId);
                if (student == null || opp == null) continue;

                Application app = new Application();
                app.setIdForImport(id);
                app.setStudent(student);
                app.setOpportunity(opp);
                try { app.setStatus(ApplicationStatus.valueOf(statusText)); } catch (Exception e) { app.setStatus(ApplicationStatus.PENDING); }
                try { app.setAppliedAt(LocalDateTime.parse(appliedAtText)); } catch (Exception e) { app.setAppliedAt(LocalDateTime.now()); }
                app.setWithdrawalRequested("true".equalsIgnoreCase(withdrawal) || "1".equals(withdrawal));

                loaded.add(app);
                maxId = Math.max(maxId, numericSuffix(id));
            }
            applications.clear();
            applications.addAll(loaded);
            Application.seedIdCounter(maxId);
            System.out.println("Imported " + loaded.size() + " applications from CSV.");
        } catch (IOException e) {
            System.err.println("Failed to load applications: " + e.getMessage());
        }
    }

    private void persist() {
        if (storagePath == null || storagePath.isBlank()) return;
        File file = new File(storagePath);
        ensureParent(file);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            writer.write(HEADER);
            writer.newLine();
            for (Application app : applications) {
                writer.write(format(app));
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            System.err.println("Failed to persist applications: " + e.getMessage());
        }
    }

    private static void ensureParent(File file) {
        File parent = file.getParentFile();
        if (parent != null) parent.mkdirs();
    }

    private static String format(Application app) {
        return String.join("," ,
                app.getId(),
                safeId(app.getStudent()),
                safeOpp(app.getOpportunity()),
                app.getStatus() == null ? "" : app.getStatus().name(),
                app.getAppliedAt() == null ? "" : app.getAppliedAt().toString(),
                Boolean.toString(app.isWithdrawalRequested())
        );
    }

    private static String safeId(Student s) { return s == null ? "" : s.getUserId(); }
    private static String safeOpp(InternshipOpportunity o) { return o == null ? "" : o.getId(); }
    private static int numericSuffix(String id) {
        if (id == null) return 0;
        String digits = id.replaceAll("\\D+", "");
        try { return Integer.parseInt(digits); } catch (Exception e) { return 0; }
    }
}
