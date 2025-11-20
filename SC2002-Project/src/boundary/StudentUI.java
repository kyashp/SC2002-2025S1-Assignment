package boundary;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import control.*;
import entity.domain.*;
import entity.domain.enums.ApplicationStatus;
import entity.domain.enums.InternshipLevel;
import entity.domain.enums.OpportunityStatus;
import repositories.*;
import util.InputHelper;

/**
 * Boundary class that exposes the student console menu for toggling visibility,
 * browsing/applying to opportunities, viewing applications, accepting offers,
 * and requesting withdrawals, along with filter management.
 *
 * <p>Business logic is delegated to ApplicationService, OpportunityService,
 * and related repositories; this class focuses on user interaction.</p>
 */
public class StudentUI implements UserInterface {

    private final Student student;
    private final ApplicationService appSvc;
    private final OpportunityService oppSvc;
    private final AuthService authSvc;
    private final ApplicationRepository appRepo;
    private final OpportunityRepository oppRepo;
    private final RequestRepository reqRepo;
    private final InputHelper input;

    private final Map<String, OpportunityFilter> userFilters = new HashMap<>();

    /**
     * Retrieves or creates an OpportunityFilter for the given user.
     *
     * @param userId the student’s user ID
     * @return an existing or newly created OpportunityFilter object
     */
    private OpportunityFilter getFilterFor(String userId) {
        return userFilters.computeIfAbsent(userId, k -> new OpportunityFilter());
    }

    /**
     * Constructs a StudentUI instance with all required services and repositories.
     *
     * @param student the currently logged-in student
     * @param appSvc the service for managing internship applications
     * @param oppSvc the service for retrieving and filtering opportunities
     * @param authSvc the authentication service
     * @param appRepo the repository for storing and retrieving applications
     * @param oppRepo the repository for storing and retrieving opportunities
     * @param reqRepo the repository for storing withdrawal requests
     * @param input helper class for validated console input
     */
    public StudentUI(Student student, ApplicationService appSvc, OpportunityService oppSvc, AuthService authSvc,
                     ApplicationRepository appRepo, OpportunityRepository oppRepo,
                     RequestRepository reqRepo, InputHelper input) {

        this.student = student;
        this.appSvc = appSvc;
        this.oppSvc = oppSvc;
        this.authSvc = authSvc;
        this.appRepo = appRepo;
        this.oppRepo = oppRepo;
        this.reqRepo = reqRepo;
        this.input = input;
    }

    /**
     * Starts the main student menu loop.
     * Handles visibility toggling, viewing opportunities,
     * applying, checking applications, accepting offers,
     * requesting withdrawal, and adjusting filters.
     */
    @Override
    public void start() {
        while (true) {
            input.printHeader("[Student] " + student.getUserName());
            System.out.println("1) Toggle visibility (Current: " + student.getVisibility() + ")");
            System.out.println("2) View visible & eligible opportunities");
            System.out.println("3) Apply to opportunity");
            System.out.println("4) View my applications");
            System.out.println("5) Accept successful application");
            System.out.println("6) Request withdrawal");
            System.out.println("7) Set filters / sort");
            System.out.println("0) Logout");

            int choice = input.readInt("Choice: ");

            switch (choice) {
                case 1 -> {
                    student.setVisibility(!student.getVisibility());
                    System.out.println("Visibility set to: " + student.getVisibility());
                }
                case 2 -> studentViewEligible();
                case 3 -> studentApply();
                case 4 -> studentViewApps();
                case 5 -> studentAccept();
                case 6 -> studentRequestWithdrawal();
                case 7 -> editFiltersStudent(student);
                case 0 -> {
                    student.logout();
                    return;
                }
                default -> System.out.println("\n<<Invalid choice!>>");
            }
        }
    }

    /**
     * Displays all visible and eligible internship opportunities for the student.
     * Applies current filter settings automatically.
     */
    private void studentViewEligible() {
        if (!student.getVisibility()) {
            System.out.println("\n<<Toggle Visibility to True to proceed.>>");
            return;
        }
        OpportunityFilter f = getFilterFor(student.getUserId());
        List<InternshipOpportunity> list = oppSvc.listVisibleFor(student, f);

        if (list.isEmpty()) {
            System.out.println("No visible/eligible opportunities right now.");
            return;
        }

        printFilterSummary(f);
        printOpps(list);
    }

    /**
     * Allows the student to apply for a visible and eligible opportunity.
     */
    private void studentApply() {
        if (!student.getVisibility()) {
            System.out.println("<< You must set Visibility to TRUE to apply. >>");
        }
        OpportunityFilter f = getFilterFor(student.getUserId());
        List<InternshipOpportunity> list = oppSvc.listVisibleFor(student, f);

        if (list.isEmpty()) {
            System.out.println("No eligible opportunities found.");
            return;
        }

        printFilterSummary(f);
        printOpps(list);

        String oid = input.readString("\nEnter Opportunity ID to apply: ");
        InternshipOpportunity opp = oppRepo.findById(oid);

        if (opp == null) {
            System.out.println("Not found.");
            return;
        }

        Application app = appSvc.apply(student, opp);
        if (app != null) {
            appRepo.save(app);
            System.out.println("Applied. Application ID: " + app.getId());
        }
    }

    /**
     * Shows the student all their current and past applications.
     */
    private void studentViewApps() {
        List<Application> apps = appSvc.listStudentApplications(student);

        if (apps.isEmpty()) {
            System.out.println("No applications.");
            return;
        }

        for (Application a : apps) {
            System.out.printf("App ID: %s | Role: %s | Status: %s\n",
                    a.getId(),
                    a.getOpportunity() != null ? a.getOpportunity().getTitle() : "?",
                    a.getStatus());
        }
    }

    /**
     * Allows a student to accept a successful application offer.
     * Updates the application's status and opportunity slot counts accordingly.
     */
    private void studentAccept() {
        List<Application> apps = appSvc.listStudentApplications(student);
        List<Application> successful = new ArrayList<>();

        for (Application a : apps)
            if (a.getStatus() == ApplicationStatus.SUCCESSFUL) successful.add(a);

        if (successful.isEmpty()) {
            System.out.println("No successful offers to accept.");
            return;
        }

        for (Application a : successful) {
            System.out.printf("APP=%s | Opp=%s%n",
                    a.getId(),
                    a.getOpportunity().getTitle());
        }

        String aid = input.readString("Enter Application ID to accept: ");
        Application target = appRepo.findById(aid);

        if (target == null) {
            System.out.println("Not found.");
            return;
        }

        try {
            appSvc.studentAccept(target);
            appRepo.save(target);
            oppSvc.updateFilledStatus(target.getOpportunity());
            oppRepo.save(target.getOpportunity());
            System.out.println("Accepted. Congrats!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Allows the student to submit a withdrawal request for an application.
     */
    private void studentRequestWithdrawal() {
        List<Application> my = appSvc.listStudentApplications(student);

        if (my.isEmpty()) {
            System.out.println("No applications to withdraw.");
            return;
        }

        for (Application a : my) {
            System.out.printf("APP=%s | Opp=%s | Status=%s%n",
                    a.getId(),
                    a.getOpportunity().getTitle(),
                    a.getStatus());
        }

        String aid = input.readString("Enter Application ID to request withdrawal: ");
        Application target = appRepo.findById(aid);

        if (target == null) {
            System.out.println("Not found.");
            return;
        }

        String reason = input.readString("Reason: ");
        WithdrawalRequest req = appSvc.requestWithdrawal(student, target, reason);

        if (req != null) {
            reqRepo.save(req);
            System.out.println("Withdrawal requested. Request ID: " + req.getId());
        }
    }

    /**
     * Allows the student to update their personal filters used when viewing opportunities.
     *
     * @param s the student who owns the filter settings
     */
    private void editFiltersStudent(Student s) {
        OpportunityFilter f = getFilterFor(s.getUserId());

        while (true) {
            System.out.println("\n=== Filters (Student) ===");
            System.out.println("1) Status (current: " + f.getStatus() + ")");
            System.out.println("2) Preferred Major (current: " + f.getPreferredMajor() + ")");
            System.out.println("3) Level (current: " + f.getLevel() + ")");
            System.out.println("4) Closing on/before (current: " + f.getClosingBefore() + ")");
            System.out.println("5) Sort (TITLE_ASC, CLOSING_DATE_ASC, COMPANY_ASC, LEVEL_ASC)");
            System.out.println("6) Clear all");
            System.out.println("0) Back");

            int choice = input.readInt("Choice: ");

            switch (choice) {
                case 1 -> {
                    String s1 = input.readString("Enter Status or blank: ");
                    f.setStatus(s1.isBlank() ? null : OpportunityStatus.valueOf(s1.toUpperCase()));
                }
                case 2 -> f.setPreferredMajor(input.readString("Preferred Major (blank=any): "));
                case 3 -> {
                    String lv = input.readString("Level (BASIC/INTERMEDIATE/ADVANCED or blank): ");
                    f.setLevel(lv.isBlank() ? null : InternshipLevel.valueOf(lv.toUpperCase()));
                }
                case 4 -> {
                    String d = input.readString("Closing on/before (YYYY-MM-DD or blank): ");
                    f.setClosingBefore(d.isBlank() ? null : LocalDate.parse(d));
                }
                case 5 -> {
                    String sk = input.readString("Sort: ");
                    if (!sk.isBlank())
                        f.setSortKey(OpportunityFilter.SortKey.valueOf(sk.toUpperCase()));
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

    /**
     * Prints a list of internship opportunities in a summary format.
     *
     * @param list the list of opportunities to display
     */
    private void printOpps(List<InternshipOpportunity> list) {
        System.out.println("=== Opportunities ===");
        for (InternshipOpportunity o : list) {
            System.out.printf(
                    "ID=%s | %s | %s | Level=%s | Status=%s | Visible=%s | Slots=%d | Window=%s..%s%n",
                    o.getId(), o.getTitle(), o.getCompanyName(), o.getLevel(), o.getStatus(), o.isVisibility(),
                    o.getSlots(), o.getOpenDate(), o.getCloseDate());
        }
    }

    /**
     * Prints a one-line summary of the student's current active filters.
     *
     * @param f the filter object for the student
     */
    private void printFilterSummary(OpportunityFilter f) {
        if (f == null) return;

        System.out.println("~ Current filters: "
                + "Status=" + f.getStatus()
                + ", Major=" + f.getPreferredMajor()
                + ", Level=" + f.getLevel()
                + ", Close<= " + f.getClosingBefore()
                + ", Sort=" + f.getSortKey());
    }
}
