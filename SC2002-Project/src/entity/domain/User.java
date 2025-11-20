package entity.domain;
import java.time.LocalDateTime;

import util.*;
/**
 * Represents the abstract parent class for the 3 distinct users: Student, Company Representative and Career Centre Staff
 */
public abstract class User {
    private String userId;
    private String userName;
    private String password;
    private boolean isLoggedIn;
    private LocalDateTime lastNotifCheck;

    /**
     * Contructs a new user object with distinct userId; default password is "password".
     *
     * @param userId unique identifier
     * @param userName display name
     */
    protected User(String userId, String userName){
        this.userId = userId;
        this.userName = userName;
        this.password = "password";
        this.isLoggedIn = false;
        this.lastNotifCheck = LocalDateTime.now();
    }

    /**
     * Retrieves String userId
     * @return String userId
     */
    public String getUserId(){
        return this.userId;
    }
    /**
     * Retrieves String userName
     * @return String userName
     */
    public String getUserName(){
        return this.userName;
    }

    /**
     * Retrieves boolean isLoggedIn
     * @return Boolean isLoggedIn (login state) 
     */
    public boolean isLoggedIn(){
        return this.isLoggedIn;
    }

    /**
     * Retrieves String password
     * @return String password
     */
    public String getPassword(){
        return this.password;
    }

    /**
     * Sets a new password with jBcrypt hashing
     * @param password The plaintext password entered by user
     */
    public void setPassword(String password){
        try {
            this.password = PasswordHasher.hashPassword(password);
        } catch (Exception e) {
            System.out.println("Hashing function failed!");
        }
    }

    /**
     * Sets the login state
     * @param isLoggedIn A boolean reflecting login state
     */
    public void setLoggedIn(boolean isLoggedIn){
        this.isLoggedIn = isLoggedIn;
    }
    /**
     * Retrieves the last time notification was retrieved
     * @return
     */
    public LocalDateTime getLastNotifCheck() {
        return lastNotifCheck;
    }
    /**
     * Sets the lastNotifCheck
     * @param lastNotifCheck long lastNotifCheck
     */
    public void setLastNotifCheck(LocalDateTime lastNotifCheck) {
        this.lastNotifCheck = lastNotifCheck;
    }
    /**
     * Simulates login for a user by verifying the supplied plaintext password.
     *
     * @param inputPassword plaintext password entered by user
     */
    public void login(String inputPassword){
        System.out.println("");
        try {
            Boolean verification = PasswordHasher.verifyPassword(inputPassword, this.password);
            if(verification){
                this.isLoggedIn = true;
                System.out.println(userName + " logged in successfully.");
            } else {
                System.out.println("Invalid username or password!");
            }
        } catch (Exception e) {
            System.out.println("Password verification failed!");
        }
        
    }

    /** Simulates logout for a user. */
    public void logout(){
        if(this.isLoggedIn){
            this.isLoggedIn = false;
        } 
    }
    
}
