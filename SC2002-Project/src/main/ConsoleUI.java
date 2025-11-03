package main;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import entity.domain.*;
import entity.domain.enums.*;
import repositories.*;
import Control.*;
import util.IdGenerator;

/**
 * Text-based interactive UI for Students, Company Reps, and Staff.
 * Adds per-user saved filters (Status, Preferred Major, Level, Closing Date)
 * with default alphabetical sorting by Title.
 */
public class ConsoleUI {
    private final Scanner sc = new Scanner(System.in);

    private final AuthService auth;
    private final UserService users;
    private final OpportunityService oppSvc;
    private final ApplicationService appSvc;
    private final ReportService reportSvc;

    private final UserRepository userRepo;
    private final OpportunityRepository oppRepo;
    private final ApplicationRepository appRepo;
    private final RequestRepository reqRepo;
    private final IdGenerator ids;

    // per-session, per-user saved filters
    private final Map<String, OpportunityFilter> userFilters = new HashMap<>();
    private OpportunityFilter getFilterFor(String userId) {
        return userFilters.computeIfAbsent(userId, k -> new OpportunityFilter());
    }

    public ConsoleUI(AuthService auth, UserService users, OpportunityService oppSvc,
                     ApplicationService appSvc, ReportService reportSvc,
                     UserRepository userRepo, OpportunityRepository oppRepo,
                     ApplicationRepository appRepo, RequestRepository reqRepo, IdGenerator ids) {
        this.auth = auth;
        this.users = users;
        this.oppSvc = oppSvc;
        this.appSvc = appSvc;
        this.reportSvc = reportSvc;
        this.userRepo = userRepo;
        this.oppRepo = oppRepo;
        this.appRepo = appRepo;
        this.reqRepo = reqRepo;
        this.ids = ids;
    }

    public void start() {
        while (true) {
            System.out.println("\n=== Internship Placement System ===");
            System.out.println("1) Login");
            System.out.println("2) First-time setup (set password)");
            System.out.println("0) Exit");
            System.out.print("Choice: ");
            int choice = readInt();
            switch (choice) {
                case 1 -> doLogin();
                case 2 -> firstTimeSetup();
                case 0 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void doLogin() {
        System.out.print("Enter User ID: ");
        String uid = sc.nextLine().trim();
        System.out.print("Enter Password: ");
        String pw = sc.nextLine().trim();
        try {
            User u = auth.loginVerification(uid, pw);
            System.out.println("Welcome, " + u.getUserName() + "!");
            if (u instanceof Student s) studentMenu(s);
            else if (u instanceof CompanyRepresentative r) repMenu(r);
            else if (u instanceof CareerCenterStaff c) staffMenu(c);
            auth.logout(u);
        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }

    // ===================== Student =====================
    private void studentMenu(Student s) {
        while (true) {
            System.out.println("\n[Student] " + s.getUserName());
            System.out.println("1) View visible & eligible opportunities");
            System.out.println("2) Apply to opportunity");
            System.out.println("3) View my applications");
            System.out.println("4) Accept successful application");
            System.out.println("5) Request withdrawal");
            System.out.println("6) Set filters / sort"); // NEW
            System.out.println("0) Logout");
            System.out.print("Choice: ");
            switch (readInt()) {
                case 1 -> studentViewEligible(s);
                case 2 -> studentApply(s);
                case 3 -> studentViewApps(s);
                case 4 -> studentAccept(s);
                case 5 -> studentRequestWithdrawal(s);
                case 6 -> editFiltersStudent(s);
                case 0 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void studentViewEligible(Student s) {
        OpportunityFilter f = getFilterFor(s.getUserId());
        List<InternshipOpportunity> list = oppSvc.listVisibleFor(s, f); // filtered + sorted (default TITLE_ASC)
        if (list.isEmpty()) {
            System.out.println("No visible/eligible opportunities right now.");
            return;
        }
        printFilterSummary(f);
        printOpps(list);
    }

    private void studentApply(Student s) {
        OpportunityFilter f = getFilterFor(s.getUserId());
        List<InternshipOpportunity> list = oppSvc.listVisibleFor(s, f);
        if (list.isEmpty()) { System.out.println("No opportunities available."); return; }
        printFilterSummary(f);
        printOpps(list);
        System.out.print("Enter Opportunity ID to apply: ");
        String oid = sc.nextLine().trim();
        InternshipOpportunity opp = oppRepo.findById(oid);
        if (opp == null) { System.out.println("Not found."); return; }
        Application app = appSvc.apply(s, opp);
        if (app != null) {
            appRepo.save(app);
            System.out.println("Applied. Application ID: " + app.getId());
        }
    }

    private void studentViewApps(Student s) {
        List<Application> my = appSvc.listStudentApplications(s);
        if (my.isEmpty()) { System.out.println("No applications."); return; }
        for (Application a : my) {
            System.out.printf("APP=%s | Opp=%s | Status=%s | AppliedAt=%s%n",
                    a.getId(),
                    a.getOpportunity() != null ? a.getOpportunity().getTitle() : "?",
                    a.getStatus(),
                    a.getAppliedAt());
        }
    }

    private void studentAccept(Student s) {
        List<Application> my = appSvc.listStudentApplications(s);
        List<Application> successful = new ArrayList<>();
        for (Application a : my) if (a.getStatus() == ApplicationStatus.SUCCESSFUL) successful.add(a);
        if (successful.isEmpty()) { System.out.println("No successful offers to accept."); return; }
        for (Application a : successful) {
            System.out.printf("APP=%s | Opp=%s%n", a.getId(), a.getOpportunity().getTitle());
        }
        System.out.print("Enter Application ID to accept: ");
        String aid = sc.nextLine().trim();
        Application target = appRepo.findById(aid);
        if (target == null) { System.out.println("Not found."); return; }
        appSvc.studentAccept(target);
        appRepo.save(target);
        oppSvc.updateFilledStatus(target.getOpportunity());
        oppRepo.save(target.getOpportunity());
        System.out.println("Accepted. Congrats!");
    }

    private void studentRequestWithdrawal(Student s) {
        List<Application> my = appSvc.listStudentApplications(s);
        if (my.isEmpty()) { System.out.println("No applications to withdraw."); return; }
        for (Application a : my) {
            System.out.printf("APP=%s | Opp=%s | Status=%s%n", a.getId(), a.getOpportunity().getTitle(), a.getStatus());
        }
        System.out.print("Enter Application ID to request withdrawal: ");
        String aid = sc.nextLine().trim();
        Application target = appRepo.findById(aid);
        if (target == null) { System.out.println("Not found."); return; }
        System.out.print("Reason: ");
        String reason = sc.nextLine().trim();
        WithdrawalRequest req = appSvc.requestWithdrawal(s, target, reason);
        if (req != null) {
            reqRepo.save(req);
            System.out.println("Withdrawal requested. Request ID: " + req.getId());
        }
    }

    private void editFiltersStudent(Student s) {
        OpportunityFilter f = getFilterFor(s.getUserId());
        while (true) {
            System.out.println("\n--- Filters (Student) ---");
            System.out.println("1) Status (current: " + f.getStatus() + ")");
            System.out.println("2) Preferred Major (current: " + f.getPreferredMajor() + ")");
            System.out.println("3) Level (current: " + f.getLevel() + ")");
            System.out.println("4) Closing on/before (YYYY-MM-DD) (current: " + f.getClosingBefore() + ")");
            System.out.println("5) Sort (TITLE_ASC, CLOSING_DATE_ASC, COMPANY_ASC, LEVEL_ASC) (current: " + f.getSortKey() + ")");
            System.out.println("6) Clear all");
            System.out.println("0) Back");
            System.out.print("Choice: ");
            switch (readInt()) {
                case 1 -> {
                    System.out.print("Enter Status or blank: ");
                    String s1 = sc.nextLine().trim();
                    f.setStatus(s1.isBlank() ? null : OpportunityStatus.valueOf(s1.toUpperCase()));
                }
                case 2 -> {
                    System.out.print("Preferred Major (blank=any): ");
                    f.setPreferredMajor(sc.nextLine().trim());
                }
                case 3 -> {
                    System.out.print("Level (BASIC/INTERMEDIATE/ADVANCED or blank): ");
                    String lv = sc.nextLine().trim();
                    f.setLevel(lv.isBlank() ? null : InternshipLevel.valueOf(lv.toUpperCase()));
                }
                case 4 -> {
                    System.out.print("Closing on/before (YYYY-MM-DD or blank): ");
                    String d = sc.nextLine().trim();
                    f.setClosingBefore(d.isBlank() ? null : LocalDate.parse(d));
                }
                case 5 -> {
                    System.out.print("Sort: ");
                    String sk = sc.nextLine().trim();
                    if (!sk.isBlank()) f.setSortKey(OpportunityFilter.SortKey.valueOf(sk.toUpperCase()));
                }
                case 6 -> {
                    userFilters.put(s.getUserId(), new OpportunityFilter());
                    System.out.println("Filters cleared.");
                    return;
                }
                case 0 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    // ===================== Company Rep =====================
    private void repMenu(CompanyRepresentative rep) {
        if (!rep.isApproved()) {
            System.out.println("Your account is not yet approved by Career Center Staff.");
        }
        while (true) {
            System.out.println("\n[Company Rep] " + rep.getUserName() + " @ " + rep.getCompanyName() +
                    (rep.isApproved() ? " (APPROVED)" : " (PENDING)"));
            System.out.println("1) Create opportunity (draft)");
            System.out.println("2) List my opportunities");
            System.out.println("3) Toggle visibility");
            System.out.println("4) Review applications for an opportunity");
            System.out.println("5) Set filters / sort"); // NEW
            System.out.println("0) Logout");
            System.out.print("Choice: ");
            switch (readInt()) {
                case 1 -> repCreateOpp(rep);
                case 2 -> repListOpps(rep);
                case 3 -> repToggleVisibility(rep);
                case 4 -> repReviewApps(rep);
                case 5 -> editFiltersRep(rep);
                case 0 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void repCreateOpp(CompanyRepresentative rep) {
        if (!rep.isApproved()) { System.out.println("Cannot create until approved."); return; }
        System.out.print("Title: "); String title = sc.nextLine().trim();
        System.out.print("Description: "); String desc = sc.nextLine().trim();
        System.out.print("Preferred Major (blank=any): "); String major = sc.nextLine().trim();
        System.out.print("Level (BASIC/INTERMEDIATE/ADVANCED): ");
        InternshipLevel lvl = InternshipLevel.valueOf(sc.nextLine().trim().toUpperCase());
        System.out.print("Slots (<=10): "); int slots = readInt();

        String id = ids.newId("O");
        InternshipOpportunity draft = rep.createOpportunity(id, title, desc, lvl, major, slots);
        draft.setOpenDate(LocalDate.now());
        draft.setCloseDate(LocalDate.now().plusWeeks(4));
        oppRepo.save(draft);
        System.out.println("Created with ID " + id + " (status PENDING, visibility OFF). Staff must approve.");
    }

    private void repListOpps(CompanyRepresentative rep) {
        OpportunityFilter f = getFilterFor(rep.getUserId());
        List<InternshipOpportunity> list = oppSvc.listByCompanyFiltered(rep.getCompanyName(), f);
        if (list.isEmpty()) { System.out.println("No opportunities yet."); return; }
        printFilterSummary(f);
        printOpps(list);
    }

    private void repToggleVisibility(CompanyRepresentative rep) {
        List<InternshipOpportunity> list = oppRepo.findByCompany(rep.getCompanyName());
        if (list.isEmpty()) { System.out.println("No opportunities."); return; }
        printOpps(list);
        System.out.print("Enter Opportunity ID: ");
        String oid = sc.nextLine().trim();
        InternshipOpportunity opp = oppRepo.findById(oid);
        if (opp == null) { System.out.println("Not found."); return; }
        System.out.print("Turn visibility ON? (y/n): ");
        boolean on = sc.nextLine().trim().equalsIgnoreCase("y");
        rep.toggleVisibility(opp, on);
        oppRepo.save(opp);
    }

    private void repReviewApps(CompanyRepresentative rep) {
        List<InternshipOpportunity> list = oppRepo.findByCompany(rep.getCompanyName());
        if (list.isEmpty()) { System.out.println("No opportunities."); return; }
        printOpps(list);
        System.out.print("Enter Opportunity ID: ");
        String oid = sc.nextLine().trim();
        InternshipOpportunity opp = oppRepo.findById(oid);
        if (opp == null) { System.out.println("Not found."); return; }

        List<Application> apps = appRepo.findByOpportunity(opp);
        if (apps.isEmpty()) { System.out.println("No applications yet."); return; }
        for (Application a : apps) {
            System.out.printf("APP=%s | Student=%s | Status=%s%n",
                    a.getId(), a.getStudent().getUserName(), a.getStatus());
        }
        System.out.print("Enter Application ID to approve/reject: ");
        String aid = sc.nextLine().trim();
        Application target = appRepo.findById(aid);
        if (target == null) { System.out.println("Not found."); return; }
        System.out.print("Approve? (y=approve / n=reject): ");
        boolean approve = sc.nextLine().trim().equalsIgnoreCase("y");
        appSvc.companyReview(rep, target, approve);
        appRepo.save(target);
        oppSvc.updateFilledStatus(opp);
        oppRepo.save(opp);
    }

    private void editFiltersRep(CompanyRepresentative rep) {
        OpportunityFilter f = getFilterFor(rep.getUserId());
        while (true) {
            System.out.println("\n--- Filters (Company Rep) ---");
            System.out.println("1) Status (current: " + f.getStatus() + ")");
            System.out.println("2) Preferred Major (current: " + f.getPreferredMajor() + ")");
            System.out.println("3) Level (current: " + f.getLevel() + ")");
            System.out.println("4) Closing on/before (current: " + f.getClosingBefore() + ")");
            System.out.println("5) Sort (current: " + f.getSortKey() + ")");
            System.out.println("6) Clear all");
            System.out.println("0) Back");
            System.out.print("Choice: ");
            switch (readInt()) {
                case 1 -> {
                    System.out.print("Status or blank: ");
                    String s1 = sc.nextLine().trim();
                    f.setStatus(s1.isBlank() ? null : OpportunityStatus.valueOf(s1.toUpperCase()));
                }
                case 2 -> {
                    System.out.print("Preferred Major (blank=any): ");
                    f.setPreferredMajor(sc.nextLine().trim());
                }
                case 3 -> {
                    System.out.print("Level (BASIC/INTERMEDIATE/ADVANCED or blank): ");
                    String lv = sc.nextLine().trim();
                    f.setLevel(lv.isBlank() ? null : InternshipLevel.valueOf(lv.toUpperCase()));
                }
                case 4 -> {
                    System.out.print("Closing on/before (YYYY-MM-DD or blank): ");
                    String d = sc.nextLine().trim();
                    f.setClosingBefore(d.isBlank() ? null : LocalDate.parse(d));
                }
                case 5 -> {
                    System.out.print("Sort: ");
                    String sk = sc.nextLine().trim();
                    if (!sk.isBlank()) f.setSortKey(OpportunityFilter.SortKey.valueOf(sk.toUpperCase()));
                }
                case 6 -> {
                    userFilters.put(rep.getUserId(), new OpportunityFilter());
                    System.out.println("Filters cleared.");
                    return;
                }
                case 0 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    // ===================== Staff =====================
    private void staffMenu(CareerCenterStaff staff) {
        while (true) {
            System.out.println("\n[Career Center Staff] " + staff.getUserName());
            System.out.println("1) Approve/Reject Company Representatives");
            System.out.println("2) Approve/Reject Opportunities");
            System.out.println("3) Process Withdrawal Requests");
            System.out.println("4) Generate Report");
            System.out.println("5) Browse opportunities (filtered)"); // NEW
            System.out.println("6) Set filters / sort");               // NEW
            System.out.println("0) Logout");
            System.out.print("Choice: ");
            switch (readInt()) {
                case 1 -> staffApproveReps(staff);
                case 2 -> staffApproveOpps(staff);
                case 3 -> staffProcessWithdrawals(staff);
                case 4 -> staffGenerateReport(staff);
                case 5 -> staffBrowseOppsFiltered(staff);
                case 6 -> editFiltersStaff(staff);
                case 0 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void staffApproveReps(CareerCenterStaff staff) {
        List<RegistrationRequest> pending = reqRepo.findPendingRepRegistrations();
        if (pending.isEmpty()) { System.out.println("No pending registrations."); return; }
        for (RegistrationRequest rr : pending) {
            System.out.printf("REQ=%s | Rep=%s | Company=%s%n",
                    rr.getId(), rr.getRep().getUserName(), rr.getRep().getCompanyName());
            System.out.print("Approve? (y=approve / n=reject): ");
            boolean ok = sc.nextLine().trim().equalsIgnoreCase("y");
            if (ok) staff.approveCompanyRep(rr); else staff.rejectCompanyRep(rr);
            reqRepo.save(rr);
        }
    }

    private void staffApproveOpps(CareerCenterStaff staff) {
        for (InternshipOpportunity o : oppRepo.findAll()) {
            if (o.getStatus() == OpportunityStatus.PENDING) {
                System.out.printf("OPP=%s | %s | %s%n", o.getId(), o.getTitle(), o.getCompanyName());
                System.out.print("Approve? (y=approve / n=reject): ");
                boolean ok = sc.nextLine().trim().equalsIgnoreCase("y");
                if (ok) staff.approveOpportunity(o); else staff.rejectOpportunity(o);
                oppRepo.save(o);
            }
        }
    }

    private void staffProcessWithdrawals(CareerCenterStaff staff) {
        List<WithdrawalRequest> pend = reqRepo.findPendingWithdrawals();
        if (pend.isEmpty()) { System.out.println("No pending withdrawals."); return; }
        for (WithdrawalRequest w : pend) {
            System.out.printf("WREQ=%s | Student=%s | App=%s | Reason=%s%n",
                    w.getId(),
                    w.getRequestedBy().getUserName(),
                    w.getApplication().getId(),
                    w.getReason());
            System.out.print("Approve? (y=approve / n=reject): ");
            boolean ok = sc.nextLine().trim().equalsIgnoreCase("y");
            appSvc.processWithdrawal(staff, w, ok);
            reqRepo.save(w);
            appRepo.save(w.getApplication());
        }
    }

    private void staffGenerateReport(CareerCenterStaff staff) {
        System.out.print("Filter by company (blank=any): ");
        String company = sc.nextLine().trim();
        System.out.print("Filter by status (PENDING/APPROVED/REJECTED/FILLED, blank=any): ");
        String st = sc.nextLine().trim();
        OpportunityStatus status = null;
        if (!st.isBlank()) status = OpportunityStatus.valueOf(st.toUpperCase());

        ReportFilter filter = new ReportFilter(
                status, null, null,
                company.isBlank() ? null : company,
                null // skip date range in CLI for brevity
        );
        Report r = reportSvc.generate(filter);
        System.out.println("Report generated at: " + r.getGeneratedAt());
        for (ReportRow row : r.getRows()) {
            System.out.printf(" - [%s] %s | Level=%s | Status=%s | Major=%s | Total=%d | Filled=%d | Remaining=%d%n",
                    row.getOpportunityId(), row.getTitle(), row.getLevel(), row.getStatus(),
                    row.getPreferredMajor(), row.getTotalApplications(), row.getFilledSlots(), row.getRemainingSlots());
        }
        System.out.println("Total opportunities: " + r.getTotalOpportunities());
    }

    private void staffBrowseOppsFiltered(CareerCenterStaff staff) {
        OpportunityFilter f = getFilterFor(staff.getUserId());
        List<InternshipOpportunity> list = oppSvc.listAllFiltered(f);
        if (list.isEmpty()) { System.out.println("No opportunities."); return; }
        printFilterSummary(f);
        printOpps(list);
    }

    private void editFiltersStaff(CareerCenterStaff staff) {
        OpportunityFilter f = getFilterFor(staff.getUserId());
        while (true) {
            System.out.println("\n--- Filters (Staff) ---");
            System.out.println("1) Status (current: " + f.getStatus() + ")");
            System.out.println("2) Preferred Major (current: " + f.getPreferredMajor() + ")");
            System.out.println("3) Level (current: " + f.getLevel() + ")");
            System.out.println("4) Closing on/before (current: " + f.getClosingBefore() + ")");
            System.out.println("5) Sort (current: " + f.getSortKey() + ")");
            System.out.println("6) Clear all");
            System.out.println("0) Back");
            System.out.print("Choice: ");
            switch (readInt()) {
                case 1 -> {
                    System.out.print("Status or blank: ");
                    String s1 = sc.nextLine().trim();
                    f.setStatus(s1.isBlank() ? null : OpportunityStatus.valueOf(s1.toUpperCase()));
                }
                case 2 -> {
                    System.out.print("Preferred Major (blank=any): ");
                    f.setPreferredMajor(sc.nextLine().trim());
                }
                case 3 -> {
                    System.out.print("Level (BASIC/INTERMEDIATE/ADVANCED or blank): ");
                    String lv = sc.nextLine().trim();
                    f.setLevel(lv.isBlank() ? null : InternshipLevel.valueOf(lv.toUpperCase()));
                }
                case 4 -> {
                    System.out.print("Closing on/before (YYYY-MM-DD or blank): ");
                    String d = sc.nextLine().trim();
                    f.setClosingBefore(d.isBlank() ? null : LocalDate.parse(d));
                }
                case 5 -> {
                    System.out.print("Sort: ");
                    String sk = sc.nextLine().trim();
                    if (!sk.isBlank()) f.setSortKey(OpportunityFilter.SortKey.valueOf(sk.toUpperCase()));
                }
                case 6 -> {
                    userFilters.put(staff.getUserId(), new OpportunityFilter());
                    System.out.println("Filters cleared.");
                    return;
                }
                case 0 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    // ===================== Helpers =====================
    private int readInt() {
        while (true) {
            String s = sc.nextLine().trim();
            try { return Integer.parseInt(s); }
            catch (Exception e) { System.out.print("Enter a number: "); }
        }
    }

    private void printOpps(List<InternshipOpportunity> list) {
        System.out.println("--- Opportunities ---");
        for (InternshipOpportunity o : list) {
            System.out.printf("ID=%s | %s | %s | Level=%s | Status=%s | Visible=%s | Slots=%d | Window=%s..%s%n",
                    o.getId(), o.getTitle(), o.getCompanyName(), o.getLevel(), o.getStatus(), o.isVisibility(),
                    o.getSlots(), o.getOpenDate(), o.getCloseDate());
        }
    }

    private void printFilterSummary(OpportunityFilter f) {
        if (f == null) return;
        System.out.println("~ Current filters: "
                + "Status=" + f.getStatus()
                + ", Major=" + f.getPreferredMajor()
                + ", Level=" + f.getLevel()
                + ", Close<= " + f.getClosingBefore()
                + ", Sort=" + f.getSortKey());
    }

    private void firstTimeSetup() {
        System.out.print("Enter your User ID: ");
        String uid = sc.nextLine().trim();

        User existingUser = userRepo.findById(uid);
        if (existingUser == null) {
            System.out.println("❌ User ID not found. Please contact the Career Center Staff.");
            return;
        }

        System.out.print("Enter new password: ");
        String p1 = sc.nextLine().trim();
        System.out.print("Confirm new password: ");
        String p2 = sc.nextLine().trim();

        if (!p1.equals(p2)) {
            System.out.println("❌ Passwords do not match.");
            return;
        }

        try {
            auth.setupPasswordFirstTime(uid, p1);
            System.out.println("✅ Password set successfully. You can now login.");
        } catch (Exception e) {
            System.out.println("❌ Setup failed: " + e.getMessage());
        }
    }
}
