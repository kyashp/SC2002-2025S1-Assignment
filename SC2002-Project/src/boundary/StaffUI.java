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
import util.FileImporter;
import util.DataReloader;

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
    private final UserRepository userRepo;
    private final FileImporter importer;
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
                   ApplicationRepository appRepo, UserRepository userRepo,
                   FileImporter importer, InputHelper input) {
        this.staff = staff;
        this.oppSvc = oppSvc;
        this.userSvc = userSvc;
        this.appSvc = appSvc;
        this.authSvc = authSvc;
        this.reportSvc = reportSvc;
        this.reqRepo = reqRepo;
        this.oppRepo = oppRepo;
        this.appRepo = appRepo;
        this.userRepo = userRepo;
        this.importer = importer;
        this.input = input;
    }

    /** Starts staff menu loop. */
    @Override
    public void start() {
        while (true) {
            DataReloader.reloadAll(importer, userRepo, reqRepo, oppRepo, appRepo);
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
        // Ensure CSV-backed data is refreshed right before listing pending opportunities.
        DataReloader.reloadAll(importer, userRepo, reqRepo, oppRepo, appRepo);
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

        Map<String, List<ReportRow>> byCompany = new HashMap<>();
        for (ReportRow row : r.getRows()) {
            String key = row.getCompanyName() == null ? "Unknown Company" : row.getCompanyName();
            byCompany.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(row);
        }

        if (r.getRows().isEmpty()) {
            System.out.println("No opportunities match this filter.");
            return;
        }

        int totalOpps = r.getTotalOpportunities();
        int totalApps = r.getRows().stream().mapToInt(ReportRow::getTotalApplications).sum();
        int totalFilled = r.getRows().stream().mapToInt(ReportRow::getFilledSlots).sum();

        for (var entry : byCompany.entrySet()) {
            String comp = entry.getKey();
            List<ReportRow> rows = entry.getValue();
            int compApps = rows.stream().mapToInt(ReportRow::getTotalApplications).sum();
            int compFilled = rows.stream().mapToInt(ReportRow::getFilledSlots).sum();
            int compSlots = rows.stream().mapToInt(ReportRow::getTotalSlots).sum();
            System.out.println("\n--- " + comp + " ---");
            System.out.println("Opportunities: " + rows.size() + " | Applications: " + compApps + " | Filled: " + compFilled + " | Slots: " + compSlots);
            for (ReportRow row : rows) {
                System.out.printf(" [%s] %s | Level=%s | Status=%s | Apps=%d | Filled=%d | Remaining=%d/%d%n",
                        row.getOpportunityId(),
                        row.getTitle(),
                        row.getLevel(),
                        row.getStatus(),
                        row.getTotalApplications(),
                        row.getFilledSlots(),
                        row.getRemainingSlots(),
                        row.getTotalSlots());
            }
        }

        System.out.println("\nTotal opportunities: " + totalOpps + " | Total applications: " + totalApps + " | Total filled: " + totalFilled);
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
            System.out.println("1) Status (PENDING/APPROVED/REJECTED/FILLED, current: " + f.getStatus() + ")");
            System.out.println("2) Preferred Major (current: " + f.getPreferredMajor() + ")");
            System.out.println("3) Level (BASIC/INTERMEDIATE/ADVANCED, current: " + f.getLevel() + ")");
            System.out.println("4) Closing on/before (current: " + f.getClosingBefore() + ")");
            System.out.println("5) Sort (TITLE_ASC, CLOSING_DATE_ASC, COMPANY_ASC, LEVEL_ASC, current: " + f.getSortKey() + ")");
            System.out.println("6) Clear all");
            System.out.println("0) Back");
            int choice = input.readInt("Choice: ");
            switch (choice) {
                case 1 -> {
                    String s1 = sc.nextLine().trim();
                    if (s1.isBlank()) {
                        f.setStatus(null);
                    } else {
                        try {
                            f.setStatus(OpportunityStatus.valueOf(s1.toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            System.out.println("Invalid status. Use PENDING/APPROVED/REJECTED/FILLED or blank.");
                            continue;
                        }
                    }
                }
                case 2 -> f.setPreferredMajor(sc.nextLine().trim());
                case 3 -> {
                    String lv = sc.nextLine().trim();
                    if (lv.isBlank()) {
                        f.setLevel(null);
                    } else {
                        try {
                            f.setLevel(InternshipLevel.valueOf(lv.toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            System.out.println("Invalid level. Use BASIC/INTERMEDIATE/ADVANCED or blank.");
                            continue;
                        }
                    }
                }
                case 4 -> {
                    String d = sc.nextLine().trim();
                    if (d.isBlank()) {
                        f.setClosingBefore(null);
                    } else {
                        try {
                            f.setClosingBefore(LocalDate.parse(d));
                        } catch (Exception e) {
                            System.out.println("Invalid date. Use YYYY-MM-DD or blank.");
                            continue;
                        }
                    }
                }
                case 5 -> {
                    String sk = sc.nextLine().trim();
                    if (sk.isBlank()) continue;
                    try {
                        f.setSortKey(OpportunityFilter.SortKey.valueOf(sk.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid sort key. Choose TITLE_ASC, CLOSING_DATE_ASC, COMPANY_ASC, LEVEL_ASC.");
                    }
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
