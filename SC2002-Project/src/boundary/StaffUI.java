package boundary;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import control.*;
import entity.domain.*;
import entity.domain.enums.InternshipLevel;
import entity.domain.enums.OpportunityStatus;
import repositories.*;
import util.InputHelper;

/**
 * UI for Career Center Staff interactions (approvals, reports, filtering).
 */
public class StaffUI implements UserInterface {

    private final Scanner sc = new Scanner(System.in);
    private final CareerCenterStaff staff;
    private final UserService userSvc;
    private final OpportunityService oppSvc;
    private final ApplicationService appSvc;
    private final ReportService reportSvc;
    private final AuthService authSvc;
    private final RequestRepository reqRepo;
    private final OpportunityRepository oppRepo;
    private final ApplicationRepository appRepo;
    private final InputHelper input;

    private final Map<String, OpportunityFilter> userFilters = new HashMap<>();
    private OpportunityFilter getFilterFor(String userId) {
        return userFilters.computeIfAbsent(userId, k -> new OpportunityFilter());
    }

    /**
     * Creates StaffUI.
     * @param staff logged-in staff user
     * @param oppSvc opportunity service
     * @param userSvc user service
     * @param appSvc application service
     * @param authSvc authentication service
     * @param reportSvc reporting service
     * @param reqRepo withdrawal/registration request repo
     * @param oppRepo opportunity repository
     * @param appRepo application repository
     * @param input input helper
     */
    public StaffUI(CareerCenterStaff staff, OpportunityService oppSvc, UserService userSvc,
                   ApplicationService appSvc, AuthService authSvc, ReportService reportSvc,
                   RequestRepository reqRepo, OpportunityRepository oppRepo,
                   ApplicationRepository appRepo, InputHelper input) {
        this.staff = staff;
        this.oppSvc = oppSvc;
        this.userSvc = userSvc;
        this.appSvc = appSvc;
        this.authSvc = authSvc;
        this.reportSvc = reportSvc;
        this.reqRepo = reqRepo;
        this.oppRepo = oppRepo;
        this.appRepo = appRepo;
        this.input = input;
    }

    /** Starts staff menu loop. */
    @Override
    public void start() {
        while (true) {
            input.printHeader("[Career Center Staff] " + staff.getUserName());
            System.out.println("1) Approve/Reject Company Representatives");
            System.out.println("2) Approve/Reject Opportunities");
            System.out.println("3) Process Withdrawal Requests");
            System.out.println("4) Generate Report");
            System.out.println("5) Browse opportunities (filtered)");
            System.out.println("6) Set filters / sort");
            System.out.println("0) Logout");

            int choice = input.readInt("Choice: ");
            switch (choice) {
                case 1 -> staffApproveReps();
                case 2 -> staffApproveOpps();
                case 3 -> staffProcessWithdrawals();
                case 4 -> staffGenerateReport();
                case 5 -> staffBrowseOppsFiltered();
                case 6 -> editFiltersStaff();
                case 0 -> { staff.logout(); return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    /** Approves or rejects rep registrations. */
    private void staffApproveReps() {
        List<RegistrationRequest> pending = reqRepo.findPendingRepRegistrations();
        if (pending.isEmpty()) { System.out.println("No pending registrations."); return; }
        for (RegistrationRequest rr : pending) {
            System.out.printf("REQ=%s | Rep=%s | Company=%s%n",
                    rr.getId(), rr.getRep().getUserName(), rr.getRep().getCompanyName());
            System.out.print("Approve? (y/n): ");
            boolean ok = sc.nextLine().trim().equalsIgnoreCase("y");
            if (ok) staff.approveCompanyRep(rr); else staff.rejectCompanyRep(rr);
            reqRepo.save(rr);
        }
    }

    /** Approves or rejects pending opportunities. */
    private void staffApproveOpps() {
        List<InternshipOpportunity> pendingList = oppRepo.findAll().stream()
                .filter(o -> o.getStatus() == OpportunityStatus.PENDING)
                .collect(Collectors.toList());

        if (pendingList.isEmpty()) {
            System.out.println("There are no pending opportunities to review.");
            return;
        }
        for (InternshipOpportunity o : pendingList) {
            System.out.printf("OPP=%s | %s | %s%n", o.getId(), o.getTitle(), o.getCompanyName());
            System.out.print("Approve? (y/n): ");
            boolean ok = sc.nextLine().trim().equalsIgnoreCase("y");
            if (ok) staff.approveOpportunity(o); else staff.rejectOpportunity(o);
            oppRepo.save(o);
        }
    }

    /** Processes withdrawal requests. */
    private void staffProcessWithdrawals() {
        List<WithdrawalRequest> pend = reqRepo.findPendingWithdrawals();
        if (pend.isEmpty()) { System.out.println("No pending withdrawals."); return; }
        for (WithdrawalRequest w : pend) {
            System.out.printf("WREQ=%s | Student=%s | App=%s | Reason=%s%n",
                    w.getId(), w.getRequestedBy().getUserName(),
                    w.getApplication().getId(), w.getReason());
            System.out.print("Approve? (y/n): ");
            boolean ok = sc.nextLine().trim().equalsIgnoreCase("y");
            appSvc.processWithdrawal(staff, w, ok);
            reqRepo.save(w);
            appRepo.save(w.getApplication());
        }
    }

    /** Generates a filtered report. */
    private void staffGenerateReport() {
        System.out.print("Filter by company (blank=any): ");
        String company = sc.nextLine().trim();
        System.out.print("Filter by preferred major (blank=any): ");
        String major = sc.nextLine().trim();
        System.out.print("Filter by level (blank=any): ");
        String lvl = sc.nextLine().trim();
        System.out.print("Filter by status (blank=any): ");
        String st = sc.nextLine().trim();
        OpportunityStatus status = st.isBlank() ? null : OpportunityStatus.valueOf(st.toUpperCase());
        InternshipLevel level = lvl.isBlank() ? null : InternshipLevel.valueOf(lvl.toUpperCase());

        LocalDate openFrom = readOptionalIsoDate("Filter by opening date (blank=any): ");
        LocalDate closeBy = readOptionalIsoDate("Filter by closing date (blank=any): ");

        ReportFilter filter = new ReportFilter(
                status,
                major.isBlank() ? null : major,
                level,
                company.isBlank() ? null : company,
                openFrom,
                closeBy
        );

        Report r = reportSvc.generate(filter);
        System.out.println("Report generated at: " + r.getGeneratedAt());
        for (ReportRow row : r.getRows()) {
            System.out.printf(" - [%s] %s | Level=%s | Status=%s%n",
                    row.getOpportunityId(), row.getTitle(),
                    row.getLevel(), row.getStatus());
        }
        System.out.println("Total opportunities: " + r.getTotalOpportunities());
    }

    /** Shows filtered opportunities. */
    private void staffBrowseOppsFiltered() {
        OpportunityFilter f = getFilterFor(staff.getUserId());
        List<InternshipOpportunity> list = oppSvc.listAllFiltered(f);
        if (list.isEmpty()) { System.out.println("No opportunities."); return; }
        printFilterSummary(f);
        printOpps(list);
    }

    /** Edits staff filters. */
    private void editFiltersStaff() {
        OpportunityFilter f = getFilterFor(staff.getUserId());
        while (true) {
            System.out.println("\n=== Filters (Staff) ===");
            System.out.println("1) Status");
            System.out.println("2) Preferred Major");
            System.out.println("3) Level");
            System.out.println("4) Closing on/before");
            System.out.println("5) Sort");
            System.out.println("6) Clear all");
            System.out.println("0) Back");
            int choice = input.readInt("Choice: ");
            switch (choice) {
                case 1 -> {
                    String s1 = sc.nextLine().trim();
                    f.setStatus(s1.isBlank() ? null : OpportunityStatus.valueOf(s1.toUpperCase()));
                }
                case 2 -> f.setPreferredMajor(sc.nextLine().trim());
                case 3 -> {
                    String lv = sc.nextLine().trim();
                    f.setLevel(lv.isBlank() ? null : InternshipLevel.valueOf(lv.toUpperCase()));
                }
                case 4 -> {
                    String d = sc.nextLine().trim();
                    f.setClosingBefore(d.isBlank() ? null : LocalDate.parse(d));
                }
                case 5 -> {
                    String sk = sc.nextLine().trim();
                    if (!sk.isBlank()) f.setSortKey(OpportunityFilter.SortKey.valueOf(sk.toUpperCase()));
                }
                case 6 -> {
                    userFilters.put(staff.getUserId(), new OpportunityFilter());
                    System.out.println("Filters cleared.");
                    return;
                }
                case 0 -> { return; }
            }
        }
    }

    /**
     * Reads an optional ISO date (blank allowed).
     * @param prompt text prompt
     * @return parsed LocalDate or null
     */
    private LocalDate readOptionalIsoDate(String prompt) {
        while (true) {
            String raw = input.readString(prompt);
            if (raw.isBlank()) return null;
            try { return LocalDate.parse(raw); }
            catch (DateTimeParseException e) {
                System.out.println("Invalid date. Use YYYY-MM-DD.");
            }
        }
    }
    /**
     * Prints opportunities list.
     * @param list list of InternshipOpportunity to print
     */
    private void printOpps(List<InternshipOpportunity> list) {
        System.out.println("=== Opportunities ===");
        for (InternshipOpportunity o : list) {
            System.out.printf("ID=%s | %s | %s | Level=%s | Status=%s%n",
                    o.getId(), o.getTitle(), o.getCompanyName(),
                    o.getLevel(), o.getStatus());
        }
    }

    /**
     * Prints summary of the provided filter.
     * @param f the OpportunityFilter to summarize
     */
    private void printFilterSummary(OpportunityFilter f) {
        if (f == null) return;
        System.out.println("~ Current filters: Status=" + f.getStatus()
                + ", Major=" + f.getPreferredMajor()
                + ", Level=" + f.getLevel()
                + ", Close<= " + f.getClosingBefore()
                + ", Sort=" + f.getSortKey());
    }
}
