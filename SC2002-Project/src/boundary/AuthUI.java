package boundary;

import java.time.LocalDateTime;
import java.util.List;

import control.AuthService;
import control.NotificationService;
import entity.domain.*;
import repositories.*;
import util.FileImporter;
import util.InputHelper;
import util.Validator;

/**
 * Handles login and registration functions for the system.
 */
public class AuthUI {
    private final AuthService authSvc;
    private final UserRepository userRepo;
    private final RequestRepository reqRepo;
    private final ApplicationRepository appRepo;
    private final OpportunityRepository oppRepo;
    private final Validator validator;
    private final FileImporter importer;
    private final InputHelper input;

    /**
     * Creates an AuthUI.
     * @param authSvc authentication service
     * @param userRepo user repository
     * @param reqRepo request repository
     * @param appRepo application repository
     * @param oppRepo opportunity repository
     * @param validator input validator
     * @param input input helper
     */
    public AuthUI(AuthService authSvc, UserRepository userRepo, RequestRepository reqRepo,
                  ApplicationRepository appRepo, OpportunityRepository oppRepo,
                  Validator validator, FileImporter importer, InputHelper input) {
        this.authSvc = authSvc;
        this.userRepo = userRepo;
        this.reqRepo = reqRepo;
        this.appRepo = appRepo;
        this.oppRepo = oppRepo;
        this.validator = validator;
        this.importer = importer;
        this.input = input;
    }

    /**
     * Handles user login flow.
     * @return logged-in User or null on failure
     */
    public User handleLogin() {
        // ensure latest CSV data is loaded each time someone starts login flow
        try {
            if (importer != null) importer.importCompanyReps(new java.io.File("data/sample_company_representative_list.csv"), reqRepo);
            if (oppRepo != null) oppRepo.reloadFromDisk();
            if (appRepo != null) appRepo.reloadFromDisk();
            if (reqRepo != null) reqRepo.reloadFromDisk();
            System.out.println("CSV data refreshed for login.");
        } catch (Exception e) {
            System.out.println("CSV refresh failed: " + e.getMessage());
        }

        String uid = input.readString("\nEnter User ID: ");

        if (!validator.isValidNtuId(uid) &&
            !validator.isValidCompanyEmail(uid) &&
            !validator.isValidStudentId(uid)) {
            System.out.println("\n<<Enter a valid User ID>>");
            return null;
        }

        User temp = userRepo.findById(uid);
        if (temp == null) {
            System.out.println("\n<<User not found>>");
            return null;
        }

        if (authSvc.isStudentOrStaff(uid) && "password".equals(temp.getPassword())) {
            return handleFirstTimeSetup(temp, uid);
        }

        return handleStandardLogin(uid);
    }

    /**
     * First-time login setup flow.
     * @param temp user object
     * @param uid user ID
     * @return updated User or null on failure
     */
    private User handleFirstTimeSetup(User temp, String uid) {
        String name = input.readString("Enter Full Name: ").toLowerCase();

        if (!temp.getUserName().toLowerCase().equals(name)) {
            System.out.println("\n<<Incorrect User ID or Full Name!>>");
            return null;
        }

        String defaultPw = input.readString("Enter Default Password: ");
        if (!defaultPw.equals("password")) {
            System.out.println("\n<<Incorrect default password!>>");
            return null;
        }

        String newPass, cfmPass;
        do {
            System.out.println("\n=== Change password ===");
            newPass = input.readString("Enter New Password: ");
            cfmPass = input.readString("Confirm Password: ");

            if (newPass.equals("password")) {
                System.out.println("\n<<Error: Enter a password different from default password.>>");
            }
            if (!validator.isNotBlank(newPass)) {
                System.out.println("\n<<Error: Enter a valid password.>>");
            }
            if (!newPass.equals(cfmPass) && validator.isNotBlank(newPass)) {
                System.out.println("\n<<Error: Passwords do not match.>>");
            }
        } while (!validator.isNotBlank(newPass) ||
                 !newPass.equals(cfmPass) ||
                 newPass.equals("password"));

        authSvc.setupPasswordFirstTime(uid, cfmPass);
        User loggedInUser = authSvc.loginVerification(uid, cfmPass);

        if (loggedInUser != null) {
            System.out.println("Welcome, " + loggedInUser.getUserName() + "!");
            System.out.println("---------------------------------------");
            System.out.println("Notifications: No Notifications");
            System.out.println("---------------------------------------");
        }

        return loggedInUser;
    }

    /**
     * Handles a normal login flow.
     * @param uid user ID
     * @return user or null if login fails
     */
    private User handleStandardLogin(String uid) {
        String pw = input.readString("Enter Password: ");

        try {
            User user = authSvc.loginVerification(uid, pw);

            if (user != null) {
                System.out.println("Welcome, " + user.getUserName() + "!");

                List<String> notifs = NotificationService.getNotifications(user, appRepo, oppRepo, reqRepo);

                System.out.println("---------------------------------------");
                if (notifs.isEmpty()) {
                    System.out.println("Notifications: No Notifications");
                } else {
                    System.out.println("Notifications:");
                    for (String n : notifs) {
                        System.out.println(n);
                    }
                }
                System.out.println("---------------------------------------");

                user.setLastNotifCheck(LocalDateTime.now());
                userRepo.save(user);

                return user;
            } else {
                System.out.println("Invalid username or password! Please try again.");
            }
        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
        }
        return null;
    }

    /** Handles registration for company representatives. */
    public void handleRegistration() {
        input.printHeader("Company Representative Registration");
        String email = input.readString("Company Email (User ID): ");

        if (authSvc.isStudentOrStaff(email)) {
            System.out.println("\nFor first-time Student and Staff, login with the default password.");
            return;
        }
        if (!validator.isValidCompanyEmail(email)) {
            System.out.println("\n<<Enter a valid company email.>>");
            return;
        }
        if (userRepo.findById(email) != null) {
            System.out.println("\n<<An existing User with that User ID exists.>>");
            return;
        }

        String compName = input.readString("Enter Company Name: ");
        String name = input.readString("Enter Full Name: ");
        String dept = input.readString("Enter Department: ");
        String pos = input.readString("Enter Position: ");

        String p1, p2;
        do {
            p1 = input.readString("Enter Password: ");
            p2 = input.readString("Confirm Password: ");

            if (!validator.isNotBlank(p1)) {
                System.out.println("\n<<Error: Enter a valid password.>>");
            }
            if (!p1.equals(p2) && validator.isNotBlank(p1)) {
                System.out.println("\n<<Error: Passwords do not match>>");
            }
        } while (!validator.isNotBlank(p1) || !p1.equals(p2));

        try {
            CompanyRepresentative rep = authSvc.setupCompanyRepAccount(email, name, p2, compName, dept, pos);
            RegistrationRequest req = new RegistrationRequest(rep);
            reqRepo.save(req);

            System.out.println("\nAccount created Successfully. You can Login once your account is approved.");
        } catch (Exception e) {
            System.out.println("\n<<Setup failed: " + e.getMessage() + " >>");
        }
    }
}
