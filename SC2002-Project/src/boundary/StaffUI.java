package boundary;

import java.time.LocalDate;
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
    
    // Local state for this session
    private final Map<String, OpportunityFilter> userFilters = new HashMap<>();
    private OpportunityFilter getFilterFor(String userId) {
        return userFilters.computeIfAbsent(userId, k -> new OpportunityFilter());
    }

    public StaffUI(CareerCenterStaff staff, OpportunityService oppSvc, UserService userSvc, ApplicationService appSvc, AuthService authSvc,
                    ReportService reportSvc, RequestRepository reqRepo, OpportunityRepository oppRepo, ApplicationRepository appRepo, InputHelper input) {
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
	            case 0 -> {staff.logout(); return;}
	            default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void staffApproveReps() {
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

    
    private void staffApproveOpps() {
    	// 1. Filter the list to get ONLY Pending opportunities
        List<InternshipOpportunity> pendingList = oppRepo.findAll().stream()
                .filter(o -> o.getStatus() == OpportunityStatus.PENDING)
                .collect(Collectors.toList());

        // 2. Check if the list is empty
        if (pendingList.isEmpty()) {
            System.out.println("There are no pending opportunities to review.");
            return;
        }
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
    
    private void staffProcessWithdrawals() {
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
    
    private void staffGenerateReport() {
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
    
    
    private void staffBrowseOppsFiltered() {
        OpportunityFilter f = getFilterFor(staff.getUserId());
        List<InternshipOpportunity> list = oppSvc.listAllFiltered(f);
        if (list.isEmpty()) { System.out.println("No opportunities."); return; }
        printFilterSummary(f);
        printOpps(list);
    }

    private void editFiltersStaff() {
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
            int choice = input.readInt("Choice: ");
            switch (choice) {
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
}