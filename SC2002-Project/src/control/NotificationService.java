package control;

import entity.domain.*;
import entity.domain.enums.*;
import repositories.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationService{

    public static List<String> getNotifications(User user, ApplicationRepository appRepo, OpportunityRepository oppRepo, RequestRepository reqRepo) {

        List<String> notes = new ArrayList<>();

        LocalDateTime lastCheckTime = user.getLastNotifCheck(); 

        //STUDENT NOTIFICATIONS
        if (user instanceof Student student) {
            // 1. New Approved Opportunities
            for (InternshipOpportunity opp : oppRepo.findAll()) {
                if (opp.getStatus() == OpportunityStatus.APPROVED && opp.getLastUpdated().isAfter(lastCheckTime) && opp.isOpenFor(student)) {
                    notes.add("- New Internship Opportunity available");
                    break;
                }
            }
            // 2. Application Status Updates
            for (Application app : appRepo.findByStudent(student)) {
                if (app.getLastUpdated().isAfter(lastCheckTime)) {
                    notes.add("- Internship Application Update");
                    break;
                }
            }
            // 3. Internship Withdrawal updates (Using requestedAt from WithdrawalRequest)
            for (WithdrawalRequest req : reqRepo.findByStudent(student)) {
                if (req.getLastUpdated().isAfter(lastCheckTime) && req.getStatus() != RequestStatus.PENDING) {
                    notes.add("- Internship Withdrawal Update");
                    break;
                }
            }
        }

        // --- STAFF NOTIFICATIONS (CareerCenterStaff) ---
        else if (user instanceof CareerCenterStaff) {
            // 1. New Pending Opportunities
            for (InternshipOpportunity opp : oppRepo.findAll()) {
                // Check if the opportunity is PENDING and was updated/created since the last check
                if (opp.getStatus() == OpportunityStatus.PENDING && opp.getLastUpdated().isAfter(lastCheckTime)) {
                    notes.add("- New Internship Opportunity Submissions");
                    break;
                }
            }
            //2. New Registration Requests (Assuming RegistrationRequest has getRequestedAt())
            for (RegistrationRequest req : reqRepo.findPendingRepRegistrations()) {
                if (req.getRequestedAt().isAfter(lastCheckTime)) {
                    notes.add("- New Registration Requests");
                    break;
                }
            }
            // 3. Pending Withdrawal Requests
            for (WithdrawalRequest req : reqRepo.findPendingWithdrawals()) {
                if (req.getRequestedAt().isAfter(lastCheckTime)) {
                    notes.add("- Pending Withdrawal Requests");
                    break;
                 }
            }
        }

        // --- COMPANY REP NOTIFICATIONS ---
        else if (user instanceof CompanyRepresentative rep) {
            // 1. New Applications for their opportunities
            List<InternshipOpportunity> myOpps = oppRepo.findByCompany(rep.getCompanyName());
            for(InternshipOpportunity opp : myOpps) {
                for(Application app : appRepo.findByOpportunity(opp)) {
                    // Use appliedAt since applications start as PENDING on creation
                    if(app.getAppliedAt().isAfter(lastCheckTime)) {
                    notes.add("- New Applications to Review");
                    break;
                    }
                }
            }
            // 2. Opportunity Approval/Rejection (Check if their opportunities were updated)
            for (InternshipOpportunity opp : myOpps) {
                if (opp.getLastUpdated().isAfter(lastCheckTime) && opp.getStatus() != OpportunityStatus.PENDING) {
                    notes.add("- Opportunity Status Update");
                }
            }
            // 3. Application Withdrawal Updates
            for (WithdrawalRequest withdrawalReq : reqRepo.findAllWithdrawals()) { // Assuming this method exists
                // Check if the request is new and involves an application for the Rep's company
                if (withdrawalReq.getRequestedAt().isAfter(lastCheckTime)) {
                    
                    // Find the opportunity linked to the application being withdrawn
                    InternshipOpportunity linkedOpp = withdrawalReq.getApplication().getOpportunity(); 
                    String studentId = withdrawalReq.getRequestedBy().getUserId();
                    // Check if this opportunity belongs to the current Company Representative's list
                    if (myOpps.contains(linkedOpp)) {
                        // Check if the request is still pending (meaning it needs the rep's attention)
                        if (withdrawalReq.getStatus() != RequestStatus.REJECTED) {
                            notes.add("Application Withdrawal: Student: "+ studentId + "is withdrawing" + " for " + linkedOpp.getTitle() + "Status: " + withdrawalReq.getStatus().toString());
                        }
                    }
                }
            }
        }
        return notes;
    }
}