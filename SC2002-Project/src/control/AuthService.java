package control;

import entity.domain.CareerCenterStaff;
import entity.domain.CompanyRepresentative;
import entity.domain.Student;
import entity.domain.User;
import repositories.UserRepository;


/**
 * AuthService is a service (control) class responsible for
 * handling user authentication logic such as login, logout,
 * and password changes.
 *
 * <<service>>
 */




public class AuthService {
	// HI
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
	     * Logs out the given user.
	     * Simply calls the User entity's logout method.
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
	    
	 // services/AuthService.java
	    public void setupPasswordFirstTime(String userId, String newPwd) {
	        User u = userRepository.findById(userId);
	        u.setPassword(newPwd);
	        userRepository.save(u);
	    }
		/**
		 * Returns if an Id is associated with Student/Staff
		 * @param uid userId
		 * @return boolean true/false
		 */
		public boolean isStudentOrStaff(String uid){
			User temp = userRepository.findById(uid);
			if((temp instanceof Student) || (temp instanceof CareerCenterStaff)){
				return true;
			}
			return false;
		}
		public CompanyRepresentative setupCompanyRepAccount(String userId, String username, String password,String companyName, String department, String position){
			CompanyRepresentative r = new CompanyRepresentative(userId, username, password, companyName, department, position);
			userRepository.save(r);
			return r;
		}
}

