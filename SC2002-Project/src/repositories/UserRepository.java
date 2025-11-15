package repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import entity.domain.User;
import entity.domain.CompanyRepresentative;
import entity.domain.Student;

/**
 * <<<Repository>> UserRepository
 * Stores and retrieves the User entities such as Student, CompanyRepresentative, and CareerCenterStaff
 * Acts as an in-memory data access layer of the Users in the system
 */

public class UserRepository {
	private final List<User> users = new ArrayList<>();
	
	/**
	 * Saves or updates a user in the repository
	 * If a user with the same ID already exists, it will be replaced
	 * @param user Any of the 3 users
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

	/**
	 * Finds a user by their unique ID 
	 * @param id userId
	 * @return The matching user, or null if not found
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

	/**
	 * Returns all the Students in the list
	 * @return List of all Students
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
	 
	/**
	 * Returns all pending Company representatives in the list
	* @return List of pending Company Representatives
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
	* Clears the repository (for testing or reset)
	*/
	public void clear() {
		users.clear();
	}
}
