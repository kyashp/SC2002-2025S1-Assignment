package repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import entity.domain.User;
import entity.domain.Student;
import entity.domain.CompanyRepresentative;

/*
 * <<<repository>> UserRepository
 * Stores and retrieves the User entities such as Student, CompanyRepresentative, and CareerCenterStaff
 * Acts as an in-memory data access layer for the application
 */

public class UserRepository {

	// ===== Attributes =====
	private final List<User> users = new ArrayList<>();
	
	// ===== Methods =====
	
	/* 
	 * Saves of updates a user in the repository
	 * If a user with the same ID already exists, it will be replaced.
	 */
	
	public void save(User user) {
		Objects.requireNonNull(user, "User required");
		
		// Check if user already exists
		User existing = findById(user.getUserId());
		if (existing != null) {
			users.remove(existing);
		}
		users.add(user);
	}
	
	/* Finds a user by their unique ID
	 * input would be the user ID
	 * return the matching user, or null if not found
	 */
	
	public User findById(String id) {
		if (id == null) {
			return null;
		}
		for (User user : users) {
			if (user.getUserId().equalsIgnoreCase(id)) {
				return user;
			}
		}
		return null;
	}
	
	/* 
	 * Return all students in the repository
	 */
	
	 public List<Student> findAllStudents() {
	        List<Student> result = new ArrayList<>();
	        for (User user : users) {
	            if (user instanceof Student) {
	                result.add((Student) user);
	            }
	        }
	        return result;
	 }
	 
	 /* 
	  * Returns all company representatives whose accounts are not yet approved
	  * 
	  */
	 
	 public List<CompanyRepresentative> findAllCompanyRepsPending(){
		 List<CompanyRepresentative> result = new ArrayList<>();
		 for (User user : users) {
			 if (user instanceof CompanyRepresentative rep && !rep.isApproved()) {
				 result.add(rep);
			 }
		 }
		 return result;
	 }
	 
	  /**
	     * Clears the repository (for testing or reset).
	     */
	 public void clear() {
		 users.clear();
	 }
	
	
}
