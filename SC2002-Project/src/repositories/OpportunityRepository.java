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
import java.time.LocalDate;

import entity.domain.CompanyRepresentative;
import entity.domain.InternshipOpportunity;
import entity.domain.ReportFilter;
import entity.domain.enums.OpportunityStatus;
import entity.domain.enums.InternshipLevel;
import util.IdGenerator;
import entity.domain.User;

/**
 * Stores and retrieves InternshipOpportunity entities.
 * Acts as an in-memory data access layer for all the listed Interns.
 */
public class OpportunityRepository {
	private final List<InternshipOpportunity> opportunities = new ArrayList<>();
    private final String storagePath;
    private final UserRepository userRepository;
    private final IdGenerator idGenerator;
    private static final String HEADER = "Id,Title,Description,Level,PreferredMajor,OpenDate,CloseDate,Status,CompanyName,RepEmail,Slots,Visibility";

    public OpportunityRepository() {
        this.storagePath = null;
        this.userRepository = null;
        this.idGenerator = null;
    }

    public OpportunityRepository(String storagePath, UserRepository userRepository, IdGenerator idGenerator) {
        this.storagePath = storagePath;
        this.userRepository = userRepository;
        this.idGenerator = idGenerator;
        loadFromDisk();
    }

	/**
     * Saves or updates an internship opportunity.
	 * If the opportunity already exits (same Id), replaces it.
     * @param opp InternshipOpportunity
     */
	public void save(InternshipOpportunity opp) {
		Objects.requireNonNull(opp, "Opportunity cannot be null");
		
		InternshipOpportunity existing = findById(opp.getId());
		if (existing != null) {
			opportunities.remove(existing);
		}
		opportunities.add(opp);
        persist();
	}
	
	/**
     * Finds an opportunity by its unique ID.
     * @param id InternshipOpportunity Id
     * @return InternshipOpportunity if there is one by that Id, null if none
     */
	public InternshipOpportunity findById(String id) {
		if (id == null) return null;
		for (InternshipOpportunity opp: opportunities) {
			if (opp.getId().equalsIgnoreCase(id)) {
				return opp;
			}
		}
		return null;
	}
	
	/**
     * Finds all approved and visible opportunities that match the given report filter.
	 * If filter is null, returns all approved and visible opportunities.
     * @param filter ReportFilter for the filter settings
     * @return List of InternshipOpportunity by the filter
     */
	public List<InternshipOpportunity> findApprovedVisibleByFilter(ReportFilter filter){
		List<InternshipOpportunity> result = new ArrayList<>();
		for (InternshipOpportunity opp: opportunities) {
			
			if (opp.getStatus() == OpportunityStatus.APPROVED && opp.isVisibility()) {
				
				// If no filter specified, include all approved visible opportunities
				if (filter == null) {
					result.add(opp);
					continue;
				}

                boolean match = true;
                
                if (filter.getCompany() != null && !filter.getCompany().isBlank()) {
                    match &= opp.getCompanyName().equalsIgnoreCase(filter.getCompany());
                }
                if (filter.getPreferredMajor() != null && !filter.getPreferredMajor().isBlank()) {
                    match &= opp.getPreferredMajor().equalsIgnoreCase(filter.getPreferredMajor());
                }
                if (filter.getLevel() != null && opp.getLevel() != filter.getLevel()) {
                    match = false;
                }
                if (filter.getStatus() != null && opp.getStatus() != filter.getStatus()) {
                    match = false;
                }

                if (filter.getOpenDateFrom() != null && opp.getOpenDate().isBefore(filter.getOpenDateFrom())) {
                    match = false;
                }
                if (filter.getCloseDateBy() != null && opp.getCloseDate().isAfter(filter.getCloseDateBy())) {
                    match = false;
                }

                if (match) {
                    result.add(opp);
                }
            }
        }
        return result;
	}
	
	/**
     * Returns all opportunities belonging to a given company.
     * @param company String companyName
     * @return List of InternshipOpportunity by the company
     */
    public List<InternshipOpportunity> findByCompany(String company) {
        List<InternshipOpportunity> result = new ArrayList<>();
        if (company == null || company.isBlank()) return result;

        for (InternshipOpportunity opp : opportunities) {
            if (opp.getCompanyName().equalsIgnoreCase(company)) {
                result.add(opp);
            }
        }
        return result;
    }
    
    /**
     * Returns all opportunities stored (for testing or reports).
     */
    public List<InternshipOpportunity> findAll() {
        return new ArrayList<>(opportunities);
    }

    /**
     * Returns all opportunities created by a specific representative.
     */
    public List<InternshipOpportunity> findByRepresentative(CompanyRepresentative rep) {
        List<InternshipOpportunity> result = new ArrayList<>();
        if (rep == null) return result;

        for (InternshipOpportunity opp : opportunities) {
            CompanyRepresentative creator = opp.getRepInCharge();
            if (creator != null && creator.getUserId().equalsIgnoreCase(rep.getUserId())) {
                result.add(opp);
            }
        }
        return result;
    }

    /**
     * Deletes the provided opportunity instance if it exists.
     * @return true if the opportunity was removed, false otherwise.
     */
    public boolean delete(InternshipOpportunity opp) {
        if (opp == null) return false;
        boolean removed = opportunities.remove(opp);
        if (removed) persist();
        return removed;
    }
    
    /**
     * Clears all stored opportunities (used for testing or system reset).
     */
    public void clear() {
        opportunities.clear();
        persist();
    }

    /** Reload from disk. */
    public void reloadFromDisk() {
        loadFromDisk();
    }

    private void loadFromDisk() {
        if (storagePath == null || storagePath.isBlank()) {
            return;
        }

        File file = new File(storagePath);
        if (!file.exists()) {
            ensureParent(file);
            return;
        }

        List<InternshipOpportunity> loaded = new ArrayList<>();
        int maxId = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean header = true;
            while ((line = br.readLine()) != null) {
                if (header) { header = false; continue; }
                if (line.isBlank()) continue;
                String[] t = line.split(",", -1);

                if (t.length < 12) continue;
                InternshipOpportunity opp = new InternshipOpportunity();
                opp.setId(t[0]);
                opp.setTitle(t[1]);
                opp.setDescription(t[2]);
                opp.setLevel(parseLevel(t[3]));
                opp.setPreferredMajor(t[4]);
                opp.setOpenDate(parseDate(t[5]));
                opp.setCloseDate(parseDate(t[6]));
                opp.setStatus(parseStatus(t[7]));
                opp.setCompanyName(t[8]);
                opp.setSlots(parseIntSafe(t[10]));
                opp.setVisibility("true".equalsIgnoreCase(t[11]) || "1".equals(t[11]));

                String repEmail = t[9];
                if (userRepository != null) {
                    User u = userRepository.findById(repEmail);
                    if (u instanceof CompanyRepresentative rep) {
                        opp.setRepInCharge(rep);
                    }
                }

                loaded.add(opp);
                maxId = Math.max(maxId, numericSuffix(t[0]));
            }
            opportunities.clear();
            opportunities.addAll(loaded);
            if (idGenerator != null) {
                idGenerator.seedPrefix("O", maxId);
            }
        } catch (IOException e) {
            System.err.println("Failed to load opportunities: " + e.getMessage());
        }
    }

    private void persist() {
        if (storagePath == null || storagePath.isBlank()) {
            return;
        }
        File file = new File(storagePath);
        ensureParent(file);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            writer.write(HEADER);
            writer.newLine();
            for (InternshipOpportunity opp : opportunities) {
                writer.write(formatOpp(opp));
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            System.err.println("Failed to persist opportunities: " + e.getMessage());
        }
    }

    private static void ensureParent(File file) {
        File parent = file.getParentFile();
        if (parent != null) parent.mkdirs();
    }

    private static String formatOpp(InternshipOpportunity opp) {
        String repId = opp.getRepInCharge() != null ? opp.getRepInCharge().getUserId() : "";
        return String.join("," ,
                safe(opp.getId()),
                safe(opp.getTitle()),
                safe(opp.getDescription()),
                safeEnum(opp.getLevel()),
                safe(opp.getPreferredMajor()),
                safeDate(opp.getOpenDate()),
                safeDate(opp.getCloseDate()),
                safeEnum(opp.getStatus()),
                safe(opp.getCompanyName()),
                safe(repId),
                Integer.toString(opp.getSlots()),
                Boolean.toString(opp.isVisibility())
        );
    }

    private static String safe(String v) {
        return v == null ? "" : v.replace(",", " ");
    }
    private static String safeEnum(Enum<?> e) { return e == null ? "" : e.name(); }
    private static String safeDate(LocalDate d) { return d == null ? "" : d.toString(); }

    private static LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return LocalDate.now();
        try {
            return LocalDate.parse(s.trim());
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    private static InternshipLevel parseLevel(String s) {
        if (s == null || s.isBlank()) return InternshipLevel.BASIC;
        try {
            return InternshipLevel.valueOf(s.trim());
        } catch (Exception e) {
            return InternshipLevel.BASIC;
        }
    }

    private static OpportunityStatus parseStatus(String s) {
        if (s == null || s.isBlank()) return OpportunityStatus.PENDING;
        try {
            return OpportunityStatus.valueOf(s.trim());
        } catch (Exception e) {
            return OpportunityStatus.PENDING;
        }
    }

    private static int parseIntSafe(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
    }

    private static int numericSuffix(String id) {
        if (id == null) return 0;
        String digits = id.replaceAll("\\D+", "");
        try { return Integer.parseInt(digits); } catch (Exception e) { return 0; }
    }
}
