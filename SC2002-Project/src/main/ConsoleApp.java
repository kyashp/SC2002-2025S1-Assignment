package main;

import java.io.File;

import boundary.*;
import control.*;
import repositories.*;
import util.*;
import java.util.Scanner;

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
        FileImporter importer = new FileImporter(userRepo);
        InputHelper input = new InputHelper(new Scanner(System.in));
        
        AuthService authService = new AuthService(userRepo);
        UserService userService = new UserService(userRepo, reqRepo, importer);
        OpportunityService opportunityService = new OpportunityService(oppRepo, validator);
        ApplicationService applicationService = new ApplicationService(appRepo, oppRepo, validator);
        ReportService reportService = new ReportService(oppRepo, appRepo);

        // ===== Optional: load CSVs if present at project root or /data =====
        try {
            File s = new File("sample_student_list.csv");
            File st = new File("sample_staff_list.csv");
            
            if (!s.exists()) s = new File("data/sample_student_list.csv");
            if (!st.exists()) st = new File("data/sample_staff_list.csv");

            if (s.exists()) importer.importStudents(s);
            if (st.exists()) importer.importStaff(st);

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
        UIFactory uiFactory = new UIFactory(
                applicationService, 
                opportunityService, 
                userService, 
                reportService,
                authService,
                appRepo, 
                oppRepo, 
                reqRepo, 
                input,
                idGen
            );

            // B. Create the Auth UI (Handles Login/Register)
            AuthUI authUI = new AuthUI(
                authService, 
                userRepo, 
                reqRepo,
                appRepo, 
                oppRepo,
                validator, 
                input
            );

            // C. Create the Main Console Router
            ConsoleUI ui = new ConsoleUI(
                authUI, 
                uiFactory, 
                input
            );

            // ==================================================
            // 7. LAUNCH
            // ==================================================
            ui.start();
        }
}
