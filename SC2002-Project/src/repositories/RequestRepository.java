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

import entity.domain.RegistrationRequest;
import entity.domain.Student;
import entity.domain.WithdrawalRequest;
import entity.domain.enums.RequestStatus;
import entity.domain.Application;
import entity.domain.User;

/**
 * <<Repository>> RequestRepository
 * Stores and retrieves Requests: Registration and Withdrawal
 * Acts as an in-memory data access layer for requests to Career Center Staff
 */
public class RequestRepository {
	private final List<RegistrationRequest> registrationRequests = new ArrayList<>();
	private final List<WithdrawalRequest> withdrawalRequests = new ArrayList<>();
    private final String storagePath;
    private final ApplicationRepository appRepo;
    private final UserRepository userRepo;
    private static final String HEADER = "Id,ApplicationId,StudentId,Status,RequestedAt,Reason,LastUpdated";

    public RequestRepository() {
        this.storagePath = null;
        this.appRepo = null;
        this.userRepo = null;
    }

    public RequestRepository(String storagePath, ApplicationRepository appRepo, UserRepository userRepo) {
        this.storagePath = storagePath;
        this.appRepo = appRepo;
        this.userRepo = userRepo;
        loadFromDisk();
    }
	
	/**
     * Saves a request (either RegistrationRequest or WithdrawalRequest).
     * Automatically routes to the correct list.
     * @param req Registration or Withdrawal request
     */
    public void save(Object req) {
        Objects.requireNonNull(req, "Request cannot be null");

        if (req instanceof RegistrationRequest regReq) {
            RegistrationRequest existing = findRegistrationById(regReq.getId());
            if (existing != null) registrationRequests.remove(existing);
            registrationRequests.add(regReq);
        } else if (req instanceof WithdrawalRequest withReq) {
            if (withReq.getId() == null || withReq.getId().isBlank()) {
                withReq.setId(new util.IdGenerator().newId("W"));
            }
            WithdrawalRequest existing = findWithdrawalById(withReq.getId());
            if (existing != null) withdrawalRequests.remove(existing);
            withdrawalRequests.add(withReq);
            persist();
        } else {
            throw new IllegalArgumentException("Unsupported request type: " + req.getClass().getSimpleName());
        }
    }
    public List<WithdrawalRequest> findAllWithdrawals(){
        reloadFromDisk();
        return this.withdrawalRequests;
    }
    /**
     * Finds all pending WithdrawalRequests.
     * @return pending list (Withdrawals)
     */
    public List<WithdrawalRequest> findPendingWithdrawals() {
        reloadFromDisk();
        List<WithdrawalRequest> pending = new ArrayList<>();
        for (WithdrawalRequest req : withdrawalRequests) {
            if (req.getStatus() == RequestStatus.PENDING) {
                pending.add(req);
            }
        }
        return pending;
    }

    /**
     * Finds all pending RegistrationRequest of Company Representatives.
     * @return pending list (Registration)
     */
    public List<RegistrationRequest> findPendingRepRegistrations() {
        List<RegistrationRequest> pending = new ArrayList<>();
        for (RegistrationRequest req : registrationRequests) {
            if (req.getStatus() == RequestStatus.PENDING) {
                pending.add(req);
            }
        }
        return pending;
    }

    /**
     * Finds a Registration Request
     * @param id String id of the request
     * @return RegistrationRequest or null if no request
     */
    private RegistrationRequest findRegistrationById(String id) {
        if (id == null) return null;
        for (RegistrationRequest req : registrationRequests) {
            if (req.getId().equalsIgnoreCase(id)) return req;
        }
        return null;
    }

    /**
     * Finds a Withdrawal Request
     * @param id String id of the request
     * @return WithdrawalRequest or null if no request
     */
    private WithdrawalRequest findWithdrawalById(String id) {
        if (id == null) return null;
        for (WithdrawalRequest req : withdrawalRequests) {
            if (req.getId().equalsIgnoreCase(id)) return req;
        }
        return null;
    }

    /**
     * Find all withdrawal requests of a student
     * @param student Student
     * @return List of withdrawal requests
     */
    public List<WithdrawalRequest> findByStudent(Student student) {
        Objects.requireNonNull(student, "Student required");
        reloadFromDisk();
        List<WithdrawalRequest> requests = new ArrayList<>();
        for (WithdrawalRequest req : withdrawalRequests) {
            if (req.getRequestedBy().equals(student)) {
                requests.add(req);
            }
        }
        return requests;
    }
    /**
     * Empty all requests
     */
    public void clear() {
        registrationRequests.clear();
        withdrawalRequests.clear();
        persist();
    }

    /**
     * Clears only registration requests (keeps withdrawal requests intact).
     */
    public void clearRegistrations() {
        registrationRequests.clear();
    }

    /** Reload withdrawals from disk. */
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
        List<WithdrawalRequest> loaded = new ArrayList<>();
        int maxId = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean header = true;
            while ((line = br.readLine()) != null) {
                if (header) { header = false; continue; }
                if (line.isBlank()) continue;
                String[] t = line.split(",", -1);
                if (t.length < 7) continue;
                String id = t[0];
                String appId = t[1];
                String studentId = t[2];
                String statusText = t[3];
                String requestedAtText = t[4];
                String reason = t[5];
                String lastUpdatedText = t[6];

                Application app = appRepo == null ? null : appRepo.findById(appId);
                Student student = null;
                User user = userRepo == null ? null : userRepo.findById(studentId);
                if (user instanceof Student s) student = s;
                if (app == null || student == null) continue;

                WithdrawalRequest w = new WithdrawalRequest();
                w.setIdForImport(id, numericSuffix(id));
                w.setApplication(app);
                w.setRequestedBy(student);
                try { w.setStatus(RequestStatus.valueOf(statusText)); } catch (Exception e) { w.setStatus(RequestStatus.PENDING); }
                try { w.setRequestedAt(LocalDateTime.parse(requestedAtText)); } catch (Exception e) { w.setRequestedAt(LocalDateTime.now()); }
                w.setReason(reason);
                try { w.setLastUpdated(LocalDateTime.parse(lastUpdatedText)); } catch (Exception e) { /* ignore */ }

                loaded.add(w);
                maxId = Math.max(maxId, numericSuffix(id));
            }
            withdrawalRequests.clear();
            withdrawalRequests.addAll(loaded);
            if (!loaded.isEmpty()) {
                new util.IdGenerator().seedPrefix("W", maxId);
            }
            System.out.println("Imported " + loaded.size() + " withdrawal requests from CSV.");
        } catch (IOException e) {
            System.err.println("Failed to load withdrawals: " + e.getMessage());
        }
    }

    private void persist() {
        if (storagePath == null || storagePath.isBlank()) return;
        File file = new File(storagePath);
        ensureParent(file);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            writer.write(HEADER);
            writer.newLine();
            for (WithdrawalRequest w : withdrawalRequests) {
                writer.write(format(w));
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            System.err.println("Failed to persist withdrawals: " + e.getMessage());
        }
    }

    private static void ensureParent(File file) {
        File parent = file.getParentFile();
        if (parent != null) parent.mkdirs();
    }

    private static String format(WithdrawalRequest w) {
        return String.join("," ,
                safe(w.getId()),
                safeApp(w.getApplication()),
                safeStudent(w.getRequestedBy()),
                w.getStatus() == null ? "" : w.getStatus().name(),
                w.getRequestedAt() == null ? "" : w.getRequestedAt().toString(),
                w.getReason() == null ? "" : w.getReason().replace(",", " "),
                w.getLastUpdated() == null ? "" : w.getLastUpdated().toString()
        );
    }

    private static String safe(String v) { return v == null ? "" : v; }
    private static String safeApp(Application a) { return a == null ? "" : a.getId(); }
    private static String safeStudent(Student s) { return s == null ? "" : s.getUserId(); }
    private static int numericSuffix(String id) {
        if (id == null) return 0;
        String digits = id.replaceAll("\\D+", "");
        try { return Integer.parseInt(digits); } catch (Exception e) { return 0; }
    }
}
