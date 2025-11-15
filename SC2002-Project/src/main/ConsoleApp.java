package main;

import java.io.File;

import control.*;
import repositories.*;
import util.*;

/**
 * Bootstraps the system & launches the interactive console UI.
 */
public class ConsoleApp {
    public static void main(String[] args) {
        // ===== Wiring (DI) =====
        UserRepository userRepo = new UserRepository();
        OpportunityRepository oppRepo = new OpportunityRepository();
        ApplicationRepository appRepo = new ApplicationRepository();
        RequestRepository reqRepo = new RequestRepository();

        IdGenerator idGen = new IdGenerator();
        Validator validator = new Validator();
        FileImporter importer = new FileImporter(userRepo, reqRepo);

        AuthService authService = new AuthService(userRepo);
        UserService userService = new UserService(userRepo, reqRepo, importer);
        OpportunityService opportunityService = new OpportunityService(oppRepo, validator);
        ApplicationService applicationService = new ApplicationService(appRepo, oppRepo, validator);
        ReportService reportService = new ReportService(oppRepo, appRepo);

        // ===== Optional: load CSVs if present at project root or /data =====
        try {
            File s = new File("sample_student_list.csv");
            File st = new File("sample_staff_list.csv");
            File r = new File("sample_company_representative_list.csv");
            if (!s.exists()) s = new File("data/sample_student_list.csv");
            if (!st.exists()) st = new File("data/sample_staff_list.csv");
            if (!r.exists()) r = new File("data/sample_company_representative_list.csv");
            if (s.exists()) importer.importStudents(s);
            if (st.exists()) importer.importStaff(st);
            if (r.exists()) importer.importCompanyReps(r);
        } catch (Exception e) {
            System.err.println("CSV import warning: " + e.getMessage());
        }

        // ===== Minimal seed if empty =====
       /* if (userRepo.findAll().isEmpty()) {
            CareerCenterStaff staff = new CareerCenterStaff(idGen.newId("U"), "AliceStaff", "pass123", "Career Centre");
            userRepo.save(staff);
            Student s1 = new Student(idGen.newId("U"), "Santhosh", "s123", 3, "Computer Science");
            userRepo.save(s1);
            CompanyRepresentative rep = new CompanyRepresentative(idGen.newId("U"), "RepJohn", "r123", "TechCo", "HR", "Recruiter");
            userRepo.save(rep);
            reqRepo.save(new RegistrationRequest(idGen.newId("REQ"), rep)); // pending rep approval
        }*/

        // ===== Launch console UI =====
        ConsoleUI ui = new ConsoleUI(
                authService, userService, opportunityService, applicationService, reportService,
                userRepo, oppRepo, appRepo, reqRepo, idGen
        );
        ui.start();
        System.out.println("Goodbye!");
    }
}
