package main;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import control.*;
import entity.domain.*;
import entity.domain.enums.*;
import repositories.*;
import util.*;

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
    private final Validator vdr;
    // per-session, per-user saved filters
    private final Map<String, OpportunityFilter> userFilters = new HashMap<>();
    private OpportunityFilter getFilterFor(String userId) {
        return userFilters.computeIfAbsent(userId, k -> new OpportunityFilter());
    }

    public ConsoleUI(AuthService auth, UserService users, OpportunityService oppSvc,
                     ApplicationService appSvc, ReportService reportSvc,
                     UserRepository userRepo, OpportunityRepository oppRepo,
                     ApplicationRepository appRepo, RequestRepository reqRepo, IdGenerator ids, Validator vdr) {
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
        this.vdr = vdr;
    }

    public void start() {
        while (true) {
            String design = "=".repeat(46);
            System.out.println();
            System.out.println(design);
            System.out.println("=== Internship Placement Management System ===");
            System.out.println(design);
            System.out.println("\nNote: For first-time Students and Career Center Staff proceed to Login with the default password.\nNote: For first-time Company Representatives please Register for an account.\n");
            System.out.println("1) Login");
            System.out.println("2) Register");
            System.out.println("0) Exit");
            System.out.print("Choice: ");
            int choice = readInt();
            switch (choice) {
                case 1 -> doLogin();
                case 2 -> firstTimeSetup();
                case 0 -> { return; }
                default -> System.out.println("\n<<Invalid choice!>>");
            }
        }
    }

    private void doLogin() {
        System.out.print("\nEnter User ID: ");
        String uid = sc.nextLine().trim();
        if((!vdr.isValidNtuId(uid)) && (!vdr.isValidCompanyEmail(uid)) && (!vdr.isValidStudentId(uid))){
            System.out.println("\n<<Enter a valid User ID>>");
            return;
        }
        User temp = userRepo.findById(uid);
        if(auth.isStudentOrStaff(uid) && (temp.getPassword()=="password")){
            System.out.print("Enter Full Name: ");
            String name = sc.nextLine().trim().toLowerCase();
            if(!temp.getUserName().toLowerCase().equals(name)){
                System.out.println("\n<<Incorrect User ID or Full Name!>>\n<<Type your Full Name as in your Matrictulation Card.>>");
                return;
            }
            System.out.print("Enter Default Password: ");
            String default_pw = sc.nextLine();
            if(!default_pw.equals("password")){
                System.out.println("\n<<Incorrect default password! Try again later!>>");
                return;
            }
            //First time change password
            String new_pass,cfm_pass;
            do{
                System.out.println("\n=== Change password ===");
                System.out.println("\nNote: Use a different password from default password.");
                System.out.print("\nEnter New Password: ");
                new_pass = sc.nextLine();
                System.out.print("Confirm Password: ");
                cfm_pass = sc.nextLine();
                if(new_pass.equals("password")){
                    System.out.println("\n<<Error: Enter a password different from default password.>>");
                }
                if(!vdr.isNotBlank(new_pass)){
                    System.out.println("\n<<Error: Enter a valid password.>>");
                }
                if ((!new_pass.equals(cfm_pass)) && (vdr.isNotBlank(new_pass))) {
                    System.out.println("\n<<Error: Passwords do not match. Please try again.>>");
                }
            } while(!new_pass.equals(cfm_pass)||(new_pass.equals("")) || (!vdr.isNotBlank(new_pass)));

            auth.setupPasswordFirstTime(uid, cfm_pass);
            temp = auth.loginVerification(uid, cfm_pass);
            System.out.println("Welcome, " + temp.getUserName() + "!");
            System.out.println("---------------------------------------");
            System.out.println("Notifications: No Notifications");
            System.out.println("---------------------------------------");
            if (temp instanceof Student s) studentMenu(s);
            if (temp instanceof CareerCenterStaff c) staffMenu(c);
            auth.logout(temp);
            return;
        }
        System.out.print("Enter Password: ");
        String pw = sc.nextLine().trim();
        try {
            User u = auth.loginVerification(uid, pw);
            System.out.println("Welcome, " + u.getUserName() + "!");

            List<String> notifs = NotificationService.getNotifications(u, appRepo, oppRepo, reqRepo);
            
            // 2. DISPLAY
            System.out.println("---------------------------------------");
            if (notifs.isEmpty()) {
                System.out.println("Notifications: No Notifications");
            } else {
                System.out.println("Notifications:");
                for (String n : notifs) {
                    System.out.println(n);
                }
            }
            System.out.println("---------------------------------------");

            u.setLastNotifCheck(LocalDateTime.now());
            userRepo.save(u);
            if (u instanceof Student s) studentMenu(s);
            else if (u instanceof CompanyRepresentative r) repMenu(r);
            else if (u instanceof CareerCenterStaff c) staffMenu(c);
            auth.logout(u);
        } catch (Exception e) {
            //System.out.println("Login failed: " + e.getMessage());
        }
    }

    //Student
    private void studentMenu(Student s) {
        while (true) {
            System.out.println("\n[Student] " + s.getUserName());
            System.out.println("1) Toggle visibility");
            System.out.println("2) View visible & eligible opportunities");
            System.out.println("3) Apply to opportunity");
            System.out.println("4) View my applications");
            System.out.println("5) Accept successful application");
            System.out.println("6) Request withdrawal");
            System.out.println("7) Set filters / sort");
            System.out.println("0) Logout");
            System.out.print("Choice: ");
            switch (readInt()) {
                case 1:
                    if(s.getVisibility()==true){
                        s.setVisibility(false);
                        System.out.println("*Visibility*: False");
                    } else {
                        s.setVisibility(true);
                        System.out.println("*Visibility*: True");
                    }
                    break;
                case 2: studentViewEligible(s); break;
                case 3: studentApply(s); break;
                case 4: studentViewApps(s); break;
                case 5: studentAccept(s); break;
                case 6: studentRequestWithdrawal(s); break;
                case 7: editFiltersStudent(s); break;
                case 0: { return; }
                default: System.out.println("\n<<Invalid choice!>>");
            }
        }
    }

    private void studentViewEligible(Student s) {
        if(!s.getVisibility()){
            System.out.println("\n<<Toggle Visibility to True to proceed.>>");
            return;
        }
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
        if(!s.getVisibility()){
            System.out.println("\n<<Toggle Visibility to True to proceed.>>");
            return;
        }
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
            System.out.println("\n=== Filters (Student) ===");
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

    //Company Rep
    private void repMenu(CompanyRepresentative rep) {
        if (rep.isApproved()==RequestStatus.PENDING) {
            System.out.println("Your account is not yet approved by Career Center Staff.");
        }
        if(rep.isApproved()==RequestStatus.REJECTED){
            System.out.println("Your account is rejected by Career Center Staff.\n Contact Career Center.");
        }
        while (true) {
            System.out.println("\n[Company Rep] " + rep.getUserName() + " @ " + rep.getCompanyName() + "(" + rep.isApproved().toString() + ")");
            System.out.println("1) Create opportunity (draft)");
            System.out.println("2) List my opportunities");
            System.out.println("3) Toggle visibility");
            System.out.println("4) Review applications for an opportunity");
            System.out.println("5) Set filters / sort"); // NEW
            System.out.println("6) Delete opportunity");
            System.out.println("0) Logout");
            System.out.print("Choice: ");
            switch (readInt()) {
                case 1 -> repCreateOpp(rep);
                case 2 -> repListOpps(rep);
                case 3 -> repToggleVisibility(rep);
                case 4 -> repReviewApps(rep);
                case 5 -> editFiltersRep(rep);
                case 6 -> repDeleteOpp(rep);
                case 0 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void repCreateOpp(CompanyRepresentative rep) {
        if (!(rep.isApproved()==RequestStatus.APPROVED)) { System.out.println("Cannot create until approved."); return; }
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

    private void repDeleteOpp(CompanyRepresentative rep) {
        List<InternshipOpportunity> list = oppRepo.findByRepresentative(rep);
        if (list.isEmpty()) {
            System.out.println("No opportunities.");
            return;
        }
        printOpps(list);
        System.out.print("Enter Opportunity ID to delete: ");
        String oid = sc.nextLine().trim();
        if (rep.deleteOpportunity(oid, oppRepo)) {
            System.out.println("Opportunity removed.");
        } else {
            System.out.println("Deletion failed.");
        }
    }

    private void editFiltersRep(CompanyRepresentative rep) {
        OpportunityFilter f = getFilterFor(rep.getUserId());
        while (true) {
            System.out.println("\n=== Filters (Company Rep) ===");
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
                default -> System.out.println("<<Invalid choice.>>");
            }
        }
    }

    //Staff
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
            System.out.println("\n=== Filters (Staff) ===");
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

    //Helpers
    private int readInt() {
        while (true) {
            String s = sc.nextLine().trim();
            try { return Integer.parseInt(s); }
            catch (Exception e) { System.out.print("Enter a number: "); }
        }
    }

    private void printOpps(List<InternshipOpportunity> list) {
        System.out.println("=== Opportunities ===");
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
        System.out.println("\n=== Registration for Company Representative account ===");
        System.out.print("\nEnter your User ID(Company Email): ");
        String uid = sc.nextLine().trim();

        User existingUser = userRepo.findById(uid);
        if(auth.isStudentOrStaff(uid)){
            System.out.println("\nFor first-time Student and Career Center Staff, Login with the default password.");
            return;
        }
        if (existingUser != null) {
            System.out.println("\n<<An existing User with that User ID exists.>>");
            return;
        }
        if(!vdr.isValidCompanyEmail(uid)){
            System.out.println("\n<<Enter a valid company email.>>");
            return;
        }

        System.out.print("Enter Company Name: ");
        String comp_name = sc.nextLine().trim();

        System.out.print("Enter Full Name: ");
        String name = sc.nextLine().trim();

        System.out.print("Enter Department: ");
        String dept = sc.nextLine().trim();

        System.out.print("Enter Position: ");
        String pos = sc.nextLine().trim();

        String p1, p2;
        do{
            System.out.print("Enter Password: ");
            p1 = sc.nextLine().trim();
            System.out.print("Confirm Password: ");
            p2 = sc.nextLine().trim();
            if(!vdr.isNotBlank(p1)){
                System.out.println("\n<<Error: Enter a valid password.>>");
            }
            if ((!p1.equals(p2)) && (!p1.equals(""))) {
                System.out.println("\n<<Error: Passwords do not match.>>");
            }
        } while((!p1.equals(p2)) || (p1.equals("")));

        try {
            CompanyRepresentative r = auth.setupCompanyRepAccount(uid, name, p2, comp_name, dept, pos);
            RegistrationRequest req = new RegistrationRequest(r);
            reqRepo.save(req);
            System.out.println("\nAccount created Successfully. You can now Login.");
        } catch (Exception e) {
            System.out.println("\n<<Setup failed: %s >>" + e.getMessage());
        }
    }
}
