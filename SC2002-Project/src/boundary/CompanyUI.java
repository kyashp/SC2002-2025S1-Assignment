package boundary;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import control.*;
import entity.domain.*;
import entity.domain.enums.*;
import repositories.*;
import util.IdGenerator;
import util.InputHelper;
import util.FileImporter;
import util.DataReloader;
import java.util.Scanner;

/**
 * UI for Company Representatives to manage opportunities and applications.
 */
public class CompanyUI implements UserInterface {
    private final Scanner sc = new Scanner(System.in);
    private final CompanyRepresentative rep;
    private final OpportunityService oppSvc;
    private final ApplicationService appSvc;
    private final AuthService authSvc;
    private final OpportunityRepository oppRepo;
    private final ApplicationRepository appRepo;
    private final RequestRepository reqRepo;
    private final UserRepository userRepo;
    private final FileImporter importer;
    private final InputHelper input;
    private final IdGenerator ids;

    // Local state for this session
    private final Map<String, OpportunityFilter> userFilters = new HashMap<>();

    /**
     * Gets or creates an opportunity filter for a given user.
     * @param userId the user ID
     * @return the filter associated with this user
     */
    private OpportunityFilter getFilterFor(String userId) {
        return userFilters.computeIfAbsent(userId, k -> new OpportunityFilter());
    }

    /**
     * Creates a CompanyUI.
     * @param rep logged-in company representative
     * @param oppSvc opportunity service
     * @param appSvc application service
     * @param authSvc authentication service
     * @param oppRepo opportunity repository
     * @param appRepo application repository
     * @param input input helper
     * @param ids ID generator
     */
    public CompanyUI(CompanyRepresentative rep, OpportunityService oppSvc, ApplicationService appSvc, AuthService authSvc,
                     OpportunityRepository oppRepo, ApplicationRepository appRepo, RequestRepository reqRepo, UserRepository userRepo,
                     FileImporter importer, InputHelper input, IdGenerator ids) {
        this.rep = rep;
        this.oppSvc = oppSvc;
        this.appSvc = appSvc;
        this.authSvc = authSvc;
        this.oppRepo = oppRepo;
        this.appRepo = appRepo;
        this.reqRepo = reqRepo;
        this.userRepo = userRepo;
        this.importer = importer;
        this.input = input;
        this.ids = ids;
    }

    /** Starts the company representative menu loop. */
    @Override
    public void start() {
        if (rep.isApproved() != RequestStatus.APPROVED) {
            System.out.println("<< Access Denied: Your account is " + rep.isApproved() + " >>");
            return;
        }
        while (true) {
            DataReloader.reloadAll(importer, userRepo, reqRepo, oppRepo, appRepo);
            input.printHeader("[Company Representative] " + rep.getCompanyName());
            System.out.println("1) Create opportunity (draft)");
            System.out.println("2) List my opportunities");
            System.out.println("3) Toggle visibility");
            System.out.println("4) Review applications for an opportunity");
            System.out.println("5) Set filters / sort");
            System.out.println("6) Delete opportunity");
            System.out.println("0) Logout");
            int choice = input.readInt("Choice: ");
            switch (choice) {
                case 1 -> repCreateOpp();
                case 2 -> repListOpps();
                case 3 -> repToggleVisibility();
                case 4 -> repReviewApps();
                case 5 -> editFiltersRep();
                case 6 -> repDeleteOpp();
                case 0 -> { rep.logout(); return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    /** Creates a new opportunity draft. */
    private void repCreateOpp() {
        if (!(rep.isApproved() == RequestStatus.APPROVED)) {
            System.out.println("Cannot create until approved.");
            return;
        }
        String title = input.readString("Title: ");
        String desc = input.readString("Description: ");
        String major = input.readString("Preferred Major (or blank): ");
        System.out.print("Level (BASIC/INTERMEDIATE/ADVANCED): ");
        InternshipLevel lvl = InternshipLevel.valueOf(sc.nextLine().trim().toUpperCase());
        int slots = input.readInt("Slots: ");
        String id = ids.newId("O");
        InternshipOpportunity draft = rep.createOpportunity(id, title, desc, lvl, major, slots);
        LocalDate openDate = LocalDate.now();
        draft.setOpenDate(openDate);
        LocalDate closeDate = input.readDateOnOrAfter("Closing Date", openDate);
        draft.setCloseDate(closeDate);
        System.out.println(rep.getUserName() + " created opportunity ID " + id + " (" + title + ")");
        oppRepo.save(draft);
        System.out.println("Created with ID " + id + " (status PENDING, visibility OFF). Staff must approve.");
    }

    /** Lists this representative's opportunities with filters applied. */
    private void repListOpps() {
        OpportunityFilter f = getFilterFor(rep.getUserId());
        List<InternshipOpportunity> list = oppSvc.listByCompanyFiltered(rep.getCompanyName(), f);
        if (list.isEmpty()) {
            System.out.println("No opportunities yet.");
            return;
        }
        printFilterSummary(f);
        printOpps(list);
    }

    /** Toggles visibility of a selected opportunity. */
    private void repToggleVisibility() {
        List<InternshipOpportunity> list = oppRepo.findByCompany(rep.getCompanyName());
        if (list.isEmpty()) {
            System.out.println("No opportunities.");
            return;
        }
        printOpps(list);
        System.out.print("Enter Opportunity ID: ");
        String oid = sc.nextLine().trim();
        InternshipOpportunity opp = oppRepo.findById(oid);
        if (opp == null) {
            System.out.println("Not found.");
            return;
        }
        System.out.print("Turn visibility ON? (y/n): ");
        boolean on = sc.nextLine().trim().equalsIgnoreCase("y");
        rep.toggleVisibility(opp, on);
        oppRepo.save(opp);
    }

    /** Reviews applications for a chosen opportunity. */
    private void repReviewApps() {
        List<InternshipOpportunity> list = oppRepo.findByCompany(rep.getCompanyName());
        if (list.isEmpty()) {
            System.out.println("No opportunities.");
            return;
        }
        printOpps(list);
        System.out.print("Enter Opportunity ID: ");
        String oid = sc.nextLine().trim();
        InternshipOpportunity opp = oppRepo.findById(oid);
        if (opp == null) {
            System.out.println("Not found.");
            return;
        }

        List<Application> apps = appRepo.findByOpportunity(opp);
        if (apps.isEmpty()) {
            System.out.println("No applications yet.");
            return;
        }
        for (Application a : apps) {
            System.out.printf("APP=%s | Student=%s | Status=%s%n",
                    a.getId(), a.getStudent().getUserName(), a.getStatus());
        }
        System.out.print("Enter Application ID to approve/reject: ");
        String aid = sc.nextLine().trim();
        Application target = appRepo.findById(aid);
        if (target == null) {
            System.out.println("Not found.");
            return;
        }
        System.out.print("Approve? (y=approve / n=reject): ");
        boolean approve = sc.nextLine().trim().equalsIgnoreCase("y");
        appSvc.companyReview(rep, target, approve);
        appRepo.save(target);
        oppSvc.updateFilledStatus(opp);
        oppRepo.save(opp);
    }

    /** Edits filters used for listing the representative's opportunities. */
    private void editFiltersRep() {
        OpportunityFilter f = getFilterFor(rep.getUserId());
        while (true) {
            System.out.println("\n=== Filters (Company Rep) ===");
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
                    System.out.print("Status (PENDING/APPROVED/REJECTED/FILLED or blank): ");
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
                case 2 -> {
                    System.out.print("Preferred Major (blank=any): ");
                    f.setPreferredMajor(sc.nextLine().trim());
                }
                case 3 -> {
                    System.out.print("Level (BASIC/INTERMEDIATE/ADVANCED or blank): ");
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
                    System.out.print("Closing on/before (YYYY-MM-DD or blank): ");
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
                    System.out.print("Sort (TITLE_ASC, CLOSING_DATE_ASC, COMPANY_ASC, LEVEL_ASC): ");
                    String sk = sc.nextLine().trim();
                    if (sk.isBlank()) continue;
                    try {
                        f.setSortKey(OpportunityFilter.SortKey.valueOf(sk.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid sort key. Choose TITLE_ASC, CLOSING_DATE_ASC, COMPANY_ASC, LEVEL_ASC.");
                    }
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

    /** Deletes an opportunity owned by this representative. */
    private void repDeleteOpp() {
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

    /**
     * Prints a list of opportunities.
     * @param list list of InternshipOpportunity to display
     */
    private void printOpps(List<InternshipOpportunity> list) {
        System.out.println("=== Opportunities ===");
        for (InternshipOpportunity o : list) {
            System.out.printf("ID=%s | %s | %s | Level=%s | Status=%s | Visible=%s | Slots=%d | Window=%s..%s%n",
                    o.getId(), o.getTitle(), o.getCompanyName(), o.getLevel(), o.getStatus(), o.isVisibility(),
                    o.getSlots(), o.getOpenDate(), o.getCloseDate());
        }
    }

    /**
     * Prints summary of the current filters.
     * @param f the filter whose values are printed
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
