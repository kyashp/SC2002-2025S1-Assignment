package control;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import entity.domain.User;
import entity.domain.Student;
import entity.domain.CompanyRepresentative;
import entity.domain.CareerCenterStaff;
import entity.domain.RegistrationRequest;
import entity.domain.enums.RequestStatus;
import repositories.UserRepository;
import repositories.RequestRepository;
import util.FileImporter;

/**
 * UserService handles user retrieval, company-rep registration workflow,
 * and loading users from file via {@link util.FileImporter}.
 */


public class UserService {
	
	// ===== Dependences =====
	
	private final UserRepository userRepository;
	private final RequestRepository requestRepository;
	private final FileImporter fileImporter;
	
	// ===== Constructor =====
	
	public UserService(UserRepository userRepository, RequestRepository requestRepository, FileImporter fileImporter) {
		  
	    this.userRepository = Objects.requireNonNull(userRepository, "userRepository required");
	    this.requestRepository = Objects.requireNonNull(requestRepository, "requestRepository required");
	    this.fileImporter = Objects.requireNonNull(fileImporter, "fileImporter required");

	}
	
	// ===== API (per UML) =====

    /**
     * Returns the user with the given id, or null if not found.
     *
     * @param id the user ID to look up
     * @return matching {@link User} or null if none exists
     */
	public User getUserById(String id) {
		if (id == null || id.isEmpty()) {
			return null;
		}
		return userRepository.findById(id);
	}
	
	/**
     * Registers a new Company Representative by creating a RegistrationRequest
     * with PENDING status. The rep is saved but not approved yet.
     *
     * @param rep the representative being registered
     * @return the created registration request
     */
	 public RegistrationRequest registerCompanyRep(CompanyRepresentative rep) {
	        Objects.requireNonNull(rep, "rep required");

	        // ensure reps start unapproved
	        rep.setApproved(RequestStatus.PENDING);
	        userRepository.save(rep);

	        RegistrationRequest req = new RegistrationRequest();
	        req.setRep(rep);
	        req.setStatus(RequestStatus.PENDING);
	        req.setRequestedAt(LocalDateTime.now());
	        requestRepository.save(req);

	        return req;
	    }

	 /** Approves a pending Company Rep request and marks the rep as approved.
	  *
	  * @param req the registration request to approve
	  */
	 public void approveCompanyRep(RegistrationRequest req) {
		 Objects.requireNonNull(req, "request required");
		 CompanyRepresentative rep = Objects.requireNonNull(req.getRep(),"request missing rep");
		 
		 
		 req.setStatus(RequestStatus.APPROVED);
		 requestRepository.save(req);
		 
		 rep.setApproved(RequestStatus.APPROVED);
		 userRepository.save(rep);
		 
	 }
	 
	 /* Rejects a Company Rep Registration request (rep remains unapproved).
	  *
	  * @param req the registration request to reject
	  */
	 public void rejectCompanyRep(RegistrationRequest req) {
		 Objects.requireNonNull(req, "Request Required");
		 req.setStatus(RequestStatus.REJECTED);
		 requestRepository.save(req);
		 //rep stays unapproved; no further action needed
		 
	 }
	 /**
	  * Loads users from a file. This delegates to FileImporter to parse the file,
	  * then saves imported users into UserRepository.
	  *
	  * Depending on your CSVs, this method may import students, staff, or both.
	  * If your assignment uses separate files, just call this twice with each file.
	  *
	  * @param file CSV file containing users to import
	  */
	    
	  public void loadUsersFromFile(File file) {
		  Objects.requireNonNull(file, "file required");
		  
		  // Import students
		  List<Student> students = fileImporter.importStudents(file);
		  if (students != null) {
			  for (entity.domain.Student s : students) {
				  userRepository.save(s);
			  }
		  }
		  
		  // Import staff
		  List<CareerCenterStaff> staff = fileImporter.importStaff(file);
		  if (staff != null) {
			  for (CareerCenterStaff st : staff) {
				  userRepository.save(st);
			  }
		  }
	  }
	 
}
