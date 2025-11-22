package util;

import java.io.File;

import repositories.ApplicationRepository;
import repositories.OpportunityRepository;
import repositories.RequestRepository;
import repositories.UserRepository;

/**
 * Utility to reload persisted CSV files into in-memory repositories.
 */
public class DataReloader {

    public static void reloadAll(FileImporter importer,
                                 UserRepository userRepo,
                                 RequestRepository reqRepo,
                                 OpportunityRepository oppRepo,
                                 ApplicationRepository appRepo) {
        if (importer != null && reqRepo != null) {
            importer.importCompanyReps(new File("data/sample_company_representative_list.csv"), reqRepo);
        }
        if (oppRepo != null) {
            oppRepo.reloadFromDisk();
        }
        if (appRepo != null) {
            appRepo.reloadFromDisk();
        }
        if (reqRepo != null) {
            reqRepo.reloadFromDisk();
        }
    }
}
