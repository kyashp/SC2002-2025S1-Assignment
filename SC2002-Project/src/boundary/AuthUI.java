package boundary;

import java.time.LocalDateTime;
import java.util.List;

import control.AuthService;
import control.NotificationService;
import entity.domain.*;
import repositories.*;
import util.InputHelper;
import util.Validator;

public class AuthUI {
    private final AuthService authSvc;
    private final UserRepository userRepo;
    private final RequestRepository reqRepo;
    
    // Repositories needed for NotificationService
    private final ApplicationRepository appRepo;
    private final OpportunityRepository oppRepo;
    
    private final Validator validator;
    private final InputHelper input;

    public AuthUI(AuthService authSvc, UserRepository userRepo, RequestRepository reqRepo, 
                  ApplicationRepository appRepo, OpportunityRepository oppRepo,
                  Validator validator, InputHelper input) {
        this.authSvc = authSvc;
        this.userRepo = userRepo;
        this.reqRepo = reqRepo;
        this.appRepo = appRepo;
        this.oppRepo = oppRepo;
        this.validator = validator;
        this.input = input;
    }

    /**
     * Handles the login logic.
     * Returns the User object on success, or null on failure.
     */
    public User handleLogin() {
        String uid = input.readString("\nEnter User ID: ");

        // 1. VALIDATION CHECKS
        if((!validator.isValidNtuId(uid)) && (!validator.isValidCompanyEmail(uid)) && (!validator.isValidStudentId(uid))){
            System.out.println("\n<<Enter a valid User ID>>");
            return null;
        }

        User temp = userRepo.findById(uid);
        
        // Safety check: if user doesn't exist in repo
        if (temp == null) {
            System.out.println("\n<<User not found>>");
            return null;
        }

        // 2. FIRST TIME LOGIN FLOW
        // We use .equals() for string comparison in Java
        if(authSvc.isStudentOrStaff(uid) && "password".equals(temp.getPassword())){
            String name = input.readString("Enter Full Name: ").toLowerCase();
            
            if(!temp.getUserName().toLowerCase().equals(name)){
                System.out.println("\n<<Incorrect User ID or Full Name!>>\n<<Type your Full Name as in your Matriculation Card.>>");
                return null;
            }
            
            String default_pw = input.readString("Enter Default Password: ");
            if(!default_pw.equals("password")){
                System.out.println("\n<<Incorrect default password! Try again later!>>");
                return null;
            }
            
            // Change Password Loop
            String new_pass, cfm_pass;
            do {
                System.out.println("\n=== Change password ===");
                System.out.println("\nNote: Use a different password from default password.");
                
                new_pass = input.readString("\nEnter New Password: ");
                cfm_pass = input.readString("Confirm Password: ");
                
                if(new_pass.equals("password")){
                    System.out.println("\n<<Error: Enter a password different from default password.>>");
                }
                if(!validator.isNotBlank(new_pass)){
                    System.out.println("\n<<Error: Enter a valid password.>>");
                }
                if ((!new_pass.equals(cfm_pass)) && (validator.isNotBlank(new_pass))) {
                    System.out.println("\n<<Error: Passwords do not match. Please try again.>>");
                }
            } while(!new_pass.equals(cfm_pass) || new_pass.equals("") || !validator.isNotBlank(new_pass) || new_pass.equals("password"));

            // Perform Setup
            authSvc.setupPasswordFirstTime(uid, cfm_pass);
            temp = authSvc.loginVerification(uid, cfm_pass);
            
            System.out.println("Welcome, " + temp.getUserName() + "!");
            System.out.println("---------------------------------------");
            System.out.println("Notifications: No Notifications");
            System.out.println("---------------------------------------");
            
            // Return user to ConsoleUI to handle routing
            authSvc.logout(temp);
            return temp;
        }

        // 3. STANDARD LOGIN FLOW
        String pw = input.readString("Enter Password: ");
        try {
            User u = authSvc.loginVerification(uid, pw);
            
            if (u == null) {
                return null;
            }
            
            System.out.println("Welcome, " + u.getUserName() + "!");

            List<String> notifs = NotificationService.getNotifications(u, appRepo, oppRepo, reqRepo);
            
            // DISPLAY NOTIFICATIONS
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

            u.setLastNotifCheck(LocalDateTime.now());
            userRepo.save(u);
            
            // Return user to ConsoleUI to handle routing
            return u;

        } catch (Exception e) {
            // This catches failed logins or other errors
            System.out.println("Invalid username or password! Please try again.");
            return null;
        }
    }

    public void handleRegistration() {
        input.printHeader("Company Representative Registration");
        String email = input.readString("Company Email (User ID): ");
        
        // Check if trying to register as student/staff
        if(authSvc.isStudentOrStaff(email)){
            System.out.println("\nFor first-time Student and Career Center Staff, Login with the default password.");
            return;
        }

        // Validation
        User existingUser = userRepo.findById(email);
        if (existingUser != null) {
            System.out.println("\n<<An existing User with that User ID exists.>>");
            return;
        }
        if(!validator.isValidCompanyEmail(email)){
            System.out.println("\n<<Enter a valid company email.>>");
            return;
        }

        // Collect Details
        String comp_name = input.readString("Enter Company Name: ");
        String name = input.readString("Enter Full Name: ");
        String dept = input.readString("Enter Department: ");
        String pos = input.readString("Enter Position: ");

        // Password Setup
        String p1, p2;
        do {
            p1 = input.readString("Enter Password: ");
            p2 = input.readString("Confirm Password: ");
            
            if(!validator.isNotBlank(p1)){
                System.out.println("\n<<Error: Enter a valid password.>>");
            }
            if ((!p1.equals(p2)) && (!p1.equals(""))) {
                System.out.println("\n<<Error: Passwords do not match.>>");
            }
        } while((!p1.equals(p2)) || (p1.equals("")));

        try {
            // Create logic
            // Note: Ideally move creation to Service, keeping here for strict adherence to your logic
            CompanyRepresentative r = new CompanyRepresentative(email, name, p2, comp_name, dept, pos);
            userRepo.save(r);
            
            RegistrationRequest req = new RegistrationRequest(r);
            reqRepo.save(req);
            
            System.out.println("\nAccount created Successfully. You can now Login.");
        } catch (Exception e) {
            System.out.println("\n<<Setup failed: %s >>" + e.getMessage());
        }
    }
}




/**package boundary;

import java.time.LocalDateTime;
import java.util.List;

import control.AuthService;
import control.NotificationService;
import entity.domain.*;
import repositories.*;
import util.InputHelper;
import util.Validator;

public class AuthUI {
    private final AuthService authSvc;
    private final UserRepository userRepo;
    private final RequestRepository reqRepo;
    
    // Added these repositories because NotificationService needs them
    private final ApplicationRepository appRepo;
    private final OpportunityRepository oppRepo;
    
    private final Validator validator;
    private final InputHelper input;

    public AuthUI(AuthService authSvc, UserRepository userRepo, RequestRepository reqRepo, 
                  ApplicationRepository appRepo, OpportunityRepository oppRepo,
                  Validator validator, InputHelper input) {
        this.authSvc = authSvc;
        this.userRepo = userRepo;
        this.reqRepo = reqRepo;
        this.appRepo = appRepo;
        this.oppRepo = oppRepo;
        this.validator = validator;
        this.input = input;
    }

    public User handleLogin() {
        String uid = input.readString("\nEnter User ID: ");

        // 1. Validate ID Format
        if (!validator.isValidNtuId(uid) && 
            !validator.isValidCompanyEmail(uid) && 
            !validator.isValidStudentId(uid)) {
            System.out.println("\n<<Enter a valid User ID>>");
            return null;
        }

        // 2. Check if User Exists
        User temp = userRepo.findById(uid);
        if (temp == null) {
            System.out.println("\n<<User not found!>>");
            return null;
        }

        // 3. First-Time Login Logic (Student/Staff with default password)
        // Note: Assuming "password" is the default. 
        // We use equals() for string comparison.
        if (authSvc.isStudentOrStaff(uid) && "password".equals(temp.getPassword())) {
            return handleFirstTimeSetup(temp, uid);
        }

        // 4. Standard Login Logic
        return handleStandardLogin(uid);
    }

    private User handleFirstTimeSetup(User temp, String uid) {
        String name = input.readString("Enter Full Name: ").toLowerCase();
        
        if (!temp.getUserName().toLowerCase().equals(name)) {
            System.out.println("\n<<Incorrect User ID or Full Name!>>");
            System.out.println("<<Type your Full Name as in your Matriculation Card.>>");
            return null;
        }

        String defaultPw = input.readString("Enter Default Password: ");
        if (!defaultPw.equals("password")) {
            System.out.println("\n<<Incorrect default password! Try again later!>>");
            return null;
        }

        // Change Password Loop
        String newPass, cfmPass;
        do {
            System.out.println("\n=== Change password ===");
            System.out.println("Note: Use a different password from default password.");
            
            newPass = input.readString("Enter New Password: ");
            cfmPass = input.readString("Confirm Password: ");

            if (newPass.equals("password")) {
                System.out.println("\n<<Error: Enter a password different from default password.>>");
            }
            if (!validator.isNotBlank(newPass)) {
                System.out.println("\n<<Error: Enter a valid password.>>");
            }
            if (!newPass.equals(cfmPass) && validator.isNotBlank(newPass)) {
                System.out.println("\n<<Error: Passwords do not match. Please try again.>>");
            }
        } while (!newPass.equals(cfmPass) || newPass.isEmpty() || newPass.equals("password"));

        // Update Password
        authSvc.setupPasswordFirstTime(uid, cfmPass);
        
        // Auto-Login after update
        User loggedInUser = authSvc.loginVerification(uid, cfmPass);
        
        if (loggedInUser != null) {
            System.out.println("Welcome, " + loggedInUser.getUserName() + "!");
            System.out.println("---------------------------------------");
            System.out.println("Notifications: No Notifications"); // Fresh account has no notifs
            System.out.println("---------------------------------------");
        }
        
        return loggedInUser;
    }

    private User handleStandardLogin(String uid) {
        String pw = input.readString("Enter Password: ");
        
        try {
            User user = authSvc.loginVerification(uid, pw);
    
            if (user != null) {
                System.out.println("Welcome, " + user.getUserName() + "!");
    
                // Fetch Notifications
                List<String> notifs = NotificationService.getNotifications(user, appRepo, oppRepo, reqRepo);
    
                // Display Notifications
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
    
                // Update Last Check Time
                user.setLastNotifCheck(LocalDateTime.now());
                userRepo.save(user);
                
                return user;
            } else {
                // Fix: Explicitly print the error message if user is null
                System.out.println("Invalid username or password! Invalid credentials. Please try again.");
            }
        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
        }
        return null;
    }

    public void handleRegistration() {
        input.printHeader("Company Representative Registration");
        String email = input.readString("Company Email (User ID): ");
        
        if(authSvc.isStudentOrStaff(email)) {
            System.out.println("\nFor first-time Student and Career Center Staff, Login with the default password.");
            return;
        }
        if (!validator.isValidCompanyEmail(email)) {
            System.out.println("Invalid email format.");
            return;
        }
        if (userRepo.findById(email) != null) {
            System.out.println("User ID already exists.");
            return;
        }

        String name = input.readString("Full Name: ");
        String company = input.readString("Company Name: ");
        String pwd = input.readString("Password: ");
        String dept = input.readString("Department: ");
        String pos = input.readString("Position: ");
        
        // Note: In strict logic, Service should create the object. 
        // For now, we create it here to match your flow.
        CompanyRepresentative rep = new CompanyRepresentative(email, name, pwd, company, dept, pos);
        
        // Save logic
        userRepo.save(rep);
        reqRepo.save(new RegistrationRequest(rep));
        
        System.out.println("\nAccount created Successfully. You can now Login.");
    }
}


**/