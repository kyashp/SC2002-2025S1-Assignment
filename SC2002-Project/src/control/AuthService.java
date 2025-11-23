package control;

import entity.domain.CareerCenterStaff;
import entity.domain.CompanyRepresentative;
import entity.domain.Student;
import entity.domain.User;
import repositories.UserRepository;
import util.CSVFileWriter;

/**
 * AuthService is a service (control) class responsible for
 * handling user authentication logic such as login, logout,
 * and password changes.
 */




public class AuthService {
	//Attributes
	private UserRepository userRepository;
	
	//constructor
	public AuthService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	
	// ===== Methods =====

    /**
     * Logs in a user by verifying their ID and password.
     * If successful, marks the user as logged in and returns the User object.
     * Otherwise, returns null.
     *
     * @param userId the ID provided during login
     * @param password the password provided during login
     * @return the authenticated {@link User}, or {@code null} if authentication fails
     */
	
	public User loginVerification(String userId, String password) {
		// Step 1: Retrieve user from repository
		User user = userRepository.findById(userId);
		
		// Step 2: Check if user exists
		if (user == null) {
			System.out.println("\nNo user found with ID: " + userId);
			System.out.println("Please enter valid user ID!");
			return null;
		}
		
		// Step 3: Delegate authentication to the User entity
		user.login(password);
		
		// Step 4: Return user if login successful
		if (user.isLoggedIn()) {
			System.out.println("Login Successful! Welcome, " + user.getUserName() + ".");
			return user;
			
		}
		
		// If login fails, return null
		System.out.println("Invalid Credentials. Please try again.");
		return null;
	}
		 /**
	     * Logs out the given user by delegating to the User entity's logout method.
	     *
	     * @param user the user to log out
	     */
		
		public void logout(User user) {
			if (user != null && user.isLoggedIn()) {
				user.logout();
			}
			else {
				System.out.println("User is not logged in. ");
			}
		}
		
		/**
	     * Allows a user to change their password.
	     * Delegates the password update to the User entity
	     * and then saves the change via the repository.
	     *
	     * @param user the logged-in user changing their password
	     * @param newPwd the new password to set
	     */
	    public void changePassword(User user, String newPwd) {
	        if (user == null) {
	            System.out.println("No user selected.");
	            return;
	        }

	        if (!user.isLoggedIn()) {
	            System.out.println("You must be logged in to change your password.");
	            return;
	        }

	        // Delegate password change to User class
	        user.setPassword(newPwd);

	        // Save updated user info
	        userRepository.save(user);
	    }
	    
	 /**
	  * Sets up a user's password during first-time login.
	  *
	  * @param userId ID of the user whose password is being initialized
	  * @param newPwd new password to persist
	  */
	    public void setupPasswordFirstTime(String userId, String newPwd) {
	        User u = userRepository.findById(userId);
	        u.setPassword(newPwd);
	        userRepository.save(u);
	    }
		/**
		 * Returns if an Id is associated with Student/Staff
		 * @param uid userId
		 * @return {@code true} if the ID belongs to a student or staff, else {@code false}
		 */
		public boolean isStudentOrStaff(String uid){
			User temp = userRepository.findById(uid);
			if((temp instanceof Student) || (temp instanceof CareerCenterStaff)){
				return true;
			}
			return false;
		}
		/**
		 * Creates a new company representative account and persists it to both in-memory
		 * storage and the backing CSV file.
		 *
		 * @param userId representative email/ID
		 * @param username representative name
		 * @param password initial password
		 * @param companyName company name
		 * @param department department name
		 * @param position job position
		 * @return the created {@link CompanyRepresentative}
		 */
		public CompanyRepresentative setupCompanyRepAccount(String userId, String username, String password,String companyName, String department, String position){
			CompanyRepresentative r = new CompanyRepresentative(userId, username, password, companyName, department, position);
			userRepository.save(r);
			String message = CSVFileWriter.repToWriteString(r);
			CSVFileWriter.writeToFile("data/sample_company_representative_list.csv", message); //change this depending on your file path
			return r;
		}
}

