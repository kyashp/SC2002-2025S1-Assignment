package boundary;

import control.*;
import entity.domain.*;
import repositories.*;
import util.IdGenerator;
import util.InputHelper;
import util.FileImporter;

/**
 * Responsible for creating the correct UI implementation based on the User type.
 */
public class UIFactory {
    
    // Dependencies required by the various UIs
    private final ApplicationService appSvc;
    private final OpportunityService oppSvc;
    private final AuthService authSvc;
    private final UserService userSvc;
    private final ReportService reportSvc; // Added for Staff
    
    private final ApplicationRepository appRepo;
    private final OpportunityRepository oppRepo;
    private final RequestRepository reqRepo;
    private final InputHelper input;
    private final IdGenerator ids;
    private final FileImporter importer;
    private final UserRepository userRepo;

    public UIFactory(ApplicationService appSvc, OpportunityService oppSvc, UserService userSvc,
                     ReportService reportSvc, AuthService authSvc, ApplicationRepository appRepo, 
                     OpportunityRepository oppRepo, RequestRepository reqRepo, InputHelper input, IdGenerator ids, FileImporter importer, UserRepository userRepo) {
        this.appSvc = appSvc;
        this.oppSvc = oppSvc;
        this.userSvc = userSvc;
        this.reportSvc = reportSvc;
        this.authSvc = authSvc;
        this.appRepo = appRepo;
        this.oppRepo = oppRepo;
        this.reqRepo = reqRepo;
        this.input = input;
        this.ids = ids;
        this.importer = importer;
        this.userRepo = userRepo;
    }

    public UserInterface getUI(User user) {
        if (user instanceof Student s) {
            return new StudentUI(s, appSvc, oppSvc, authSvc, appRepo, oppRepo, reqRepo, userRepo, importer, input);
        } 
        else if (user instanceof CompanyRepresentative r) {
            return new CompanyUI(r, oppSvc, appSvc, authSvc, oppRepo, appRepo, reqRepo, userRepo, importer, input, ids);
        } 
        else if (user instanceof CareerCenterStaff c) {
            return new StaffUI(c, oppSvc,userSvc, appSvc, authSvc, reportSvc, reqRepo, oppRepo, appRepo, userRepo, importer, input);
        }
        throw new IllegalArgumentException("No UI defined for user type: " + user.getClass().getSimpleName());
    }
}
