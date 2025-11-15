package control;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import entity.domain.Report;
import entity.domain.ReportRow;
import entity.domain.ReportFilter;
import entity.domain.InternshipOpportunity;
import entity.domain.enums.InternshipLevel;
import entity.domain.enums.OpportunityStatus;
import repositories.ApplicationRepository;
import repositories.OpportunityRepository;


/**
 * <<service>> ReportService
 * Generates aggregated reports of opportunities and applications.
 */
public class ReportService {

    // ===== Dependencies =====
    private final OpportunityRepository opportunityRepository;
    private final ApplicationRepository applicationRepository;

    // ===== Constructor =====
    public ReportService(OpportunityRepository opportunityRepository,
                         ApplicationRepository applicationRepository) {
        this.opportunityRepository = Objects.requireNonNull(opportunityRepository, "opportunityRepository required");
        this.applicationRepository = Objects.requireNonNull(applicationRepository, "applicationRepository required");
    }

    // ===== API =====

    /**
     * Builds a Report based on the given filter.
     * For each matching opportunity, adds a ReportRow with:
     * - opportunityId, title, level, status, preferredMajor
     * - totalApplications, filledSlots (successful apps), remainingSlots
     */
    public Report generate(ReportFilter filter) {
        // 1) Fetch opportunities (approved/visible + filter) as per your UML
        List<InternshipOpportunity> opps =
                opportunityRepository.findApprovedVisibleByFilter(filter);

        // 2) Build rows
        List<ReportRow> rows = new ArrayList<>();
        for (InternshipOpportunity opp : opps) {
            rows.add(buildRow(opp));
        }

        // 3) Assemble report
        Report report = new Report();
        report.setGeneratedAt(LocalDateTime.now());
        report.setRows(rows);
        return report;
    }

    // ===== Helpers =====

    private ReportRow buildRow(InternshipOpportunity opp) {
        int totalApps = applicationRepository.findByOpportunity(opp).size();
        int filledSlots = applicationRepository.countSuccessfulByOpportunity(opp);
        int remaining = Math.max(0, opp.getSlots() - filledSlots);

        ReportRow row = new ReportRow();
        row.setOpportunityId(opp.getId());
        row.setTitle(opp.getTitle());
        row.setLevel(opp.getLevel());                     // InternshipLevel
        row.setStatus(opp.getStatus());                   // OpportunityStatus
        row.setPreferredMajor(opp.getPreferredMajor());
        row.setTotalApplications(totalApps);
        row.setFilledSlots(filledSlots);
        row.setRemainingSlots(remaining);
        return row;
    }
}
