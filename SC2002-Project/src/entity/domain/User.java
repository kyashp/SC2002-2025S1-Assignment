package entity.domain;

public abstract class User {
	
	private String userId;
	private String userName;
	private String password;
	private boolean isLoggedIn;
	
	protected User(String userId, String userName, String password) {
		this.userId = userId;
		this.userName = userName;
		this.password = password;
		this.isLoggedIn = false;
		
	}
	
	//===== Getters =====
    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }
    
    
    
    public boolean isLoggedIn() {
    	return isLoggedIn;
    }
 // ===== Setters =====
    public String setUserName(String name) {
        this.userName = name;
        return this.userName;
    }
    
    public void setPassword(String password) {
    	this.password = password;
    }
    
    public void setLoggedIn(boolean isLoggedIn) {
    	this.isLoggedIn = isLoggedIn;
    }
	
 // ===== Methods =====

    /**
     * Simulates user login.
     * Checks whether the entered password matches the stored password.
     */
    
	public void login(String inputPassword) {
		System.out.println("");	
		 if (this.password.equals(inputPassword)) {
	            this.isLoggedIn = true;
	            System.out.println(userName + " logged in successfully.");
	        } else {
	            System.out.println("Invalid password for " + userName + ".");
	        }
	}
	
	
	public void logout() {
		if (this.isLoggedIn) {
            this.isLoggedIn = false;
            System.out.println(userName + " logged out. Returning to login page...\n");
        } else {
            System.out.println("User is already logged out.");
        }
	}
	
	public void changePassword(String newPwd) {
		
	}
}
