package util;

import org.mindrot.jbcrypt.*;
/**
 * Utility class for hashing and verifying passwords using the BCrypt algorithm.
 * Requires the jBCrypt library to be included in the project's classpath.
 */
public class PasswordHasher {

    /** 
     * Define the work factor (cost). Higher is slower and more secure.
     * 10 is usually the minimum recommended for good security.
    */
    private static final int WORK_FACTOR = 12;

    /**
     * Hashes a raw password. BCrypt automatically handles salting internally.
     * @param rawPassword The plaintext password.
     * @return The resulting hash string, which contains the algorithm, cost, and salt.
     */
    public static String hashPassword(String rawPassword){
        // BCrypt.hashpw generates a salt internally and combines it with the hash
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(WORK_FACTOR));
    }

    /**
     * Verifies a raw password against a stored hash.
     * @param rawPassword The plaintext password entered during login.
     * @param storedHash The hash string retrieved from storage.
     * @return true if the passwords match, false otherwise.
     */
    public static boolean verifyPassword(String rawPassword, String storedHash){
        // BCrypt.checkpw extracts the salt from the storedHash and compares
        return BCrypt.checkpw(rawPassword, storedHash);
    }
}